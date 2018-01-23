/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.testsuite.test.configuration.elytron;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.page.configuration.ElytronSecurityRealmsPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.testsuite.test.configuration.elytron.ElytronFixtures.*;

@RunWith(Arquillian.class)
public class SecurityRealmsTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void beforeTests() throws Exception {

        // a realm is required for aggregate-realm, so create it first
        operations.add(filesystemRealmAddress(FILESYS_RLM_UPDATE), Values.of(PATH, ANY_STRING));
        operations.add(filesystemRealmAddress(FILESYS_RLM_UPDATE2), Values.of(PATH, ANY_STRING));
        operations.add(filesystemRealmAddress(FILESYS_RLM_UPDATE3), Values.of(PATH, ANY_STRING));
        operations.add(filesystemRealmAddress(FILESYS_RLM_DELETE), Values.of(PATH, ANY_STRING));

        operations.add(aggregateRealmAddress(AGG_RLM_UPDATE),
                Values.of(AUTHENTICATION_REALM, FILESYS_RLM_UPDATE).and(AUTHORIZATION_REALM, FILESYS_RLM_UPDATE2));
        operations.add(aggregateRealmAddress(AGG_RLM_DELETE),
                Values.of(AUTHENTICATION_REALM, FILESYS_RLM_UPDATE).and(AUTHORIZATION_REALM, FILESYS_RLM_UPDATE2));

        operations.add(cachingRealmAddress(CAC_RLM_UPDATE), Values.of(REALM, FILESYS_RLM_UPDATE));
        operations.add(cachingRealmAddress(CAC_RLM_DELETE), Values.of(REALM, FILESYS_RLM_UPDATE));

        operations.add(identityRealmAddress(IDEN_RLM_UPDATE), Values.of(IDENTITY, ANY_STRING));
        operations.add(identityRealmAddress(IDEN_RLM_DELETE), Values.of(IDENTITY, ANY_STRING));

        ModelNode credRef = new ModelNode();
        credRef.get(CLEAR_TEXT).set(ANY_STRING);
        Values ksParams = Values.of(TYPE, JKS).and(CREDENTIAL_REFERENCE, credRef);
        operations.add(keyStoreAddress(KEY_ST_UPDATE), ksParams);
        operations.add(keyStoreAddress(KEY_ST_CREATE), ksParams);

        operations.add(keystoreRealmAddress(KS_RLM_UPDATE), Values.of(KEY_STORE, KEY_ST_UPDATE));
        operations.add(keystoreRealmAddress(KS_RLM_DELETE), Values.of(KEY_STORE, KEY_ST_UPDATE));

        operations.add(dirContextAddress(DIR_UPDATE), Values.of(URL, ANY_STRING));
        operations.add(dirContextAddress(DIR_CREATE), Values.of(URL, ANY_STRING));

        ModelNode rdnMap = new ModelNode();
        rdnMap.get(RDN_IDENTIFIER, ANY_STRING);
        operations.add(ldapRealmAddress(LDAP_RLM_UPDATE),
                Values.of(DIR_CONTEXT, DIR_UPDATE).and(IDENTITY_MAPPING, rdnMap));
        operations.add(ldapRealmAddress(LDAP_RLM_DELETE),
                Values.of(DIR_CONTEXT, DIR_UPDATE).and(IDENTITY_MAPPING, rdnMap));

    }

    @AfterClass
    public static void tearDown() throws Exception {

        operations.remove(aggregateRealmAddress(AGG_RLM_UPDATE));
        operations.remove(aggregateRealmAddress(AGG_RLM_DELETE));
        operations.remove(aggregateRealmAddress(AGG_RLM_CREATE));

        operations.remove(cachingRealmAddress(CAC_RLM_UPDATE));
        operations.remove(cachingRealmAddress(CAC_RLM_CREATE));
        operations.remove(cachingRealmAddress(CAC_RLM_DELETE));

        operations.remove(identityRealmAddress(IDEN_RLM_DELETE));
        operations.remove(identityRealmAddress(IDEN_RLM_UPDATE));
        operations.remove(identityRealmAddress(IDEN_RLM_CREATE));

        // remove the filesystem-realm last as it is required for other resources
        operations.remove(filesystemRealmAddress(FILESYS_RLM_UPDATE));
        operations.remove(filesystemRealmAddress(FILESYS_RLM_UPDATE2));
        operations.remove(filesystemRealmAddress(FILESYS_RLM_UPDATE3));
        operations.remove(filesystemRealmAddress(FILESYS_RLM_DELETE));
        operations.remove(filesystemRealmAddress(FILESYS_RLM_CREATE));


        operations.remove(keystoreRealmAddress(KS_RLM_UPDATE));
        operations.remove(keystoreRealmAddress(KS_RLM_DELETE));
        operations.remove(keystoreRealmAddress(KS_RLM_CREATE));

        operations.remove(keyStoreAddress(KEY_ST_UPDATE));
        operations.remove(keyStoreAddress(KEY_ST_CREATE));

        operations.remove(ldapRealmAddress(LDAP_RLM_DELETE));
        operations.remove(ldapRealmAddress(LDAP_RLM_CREATE));
        operations.remove(ldapRealmAddress(LDAP_RLM_UPDATE));

        operations.remove(dirContextAddress(DIR_UPDATE));
        operations.remove(dirContextAddress(DIR_CREATE));

    }

    @Page private ElytronSecurityRealmsPage page;
    @Inject private Console console;
    @Inject private CrudOperations crud;

    @Before
    public void setUp() throws Exception {
        page.navigate();
    }

    // --------------- aggregate-realm

    @Test
    public void aggregateRealmCreate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, AGGREGATE_REALM_ITEM);
        TableFragment table = page.getAggregateRealmTable();

        crud.create(aggregateRealmAddress(AGG_RLM_CREATE), table, f -> {
            f.text(NAME, AGG_RLM_CREATE);
            f.text(AUTHENTICATION_REALM, FILESYS_RLM_UPDATE);
            f.text(AUTHORIZATION_REALM, FILESYS_RLM_UPDATE2);
        });
    }

    @Test
    public void aggregateRealmTryCreate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, AGGREGATE_REALM_ITEM);
        TableFragment table = page.getAggregateRealmTable();
        crud.createWithErrorAndCancelDialog(table, f -> f.text(NAME, AGG_RLM_CREATE), AUTHENTICATION_REALM);
    }

    @Test
    public void aggregateRealmUpdate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, AGGREGATE_REALM_ITEM);
        TableFragment table = page.getAggregateRealmTable();
        FormFragment form = page.getAggregateRealmForm();
        table.bind(form);
        table.select(AGG_RLM_UPDATE);
        crud.update(aggregateRealmAddress(AGG_RLM_UPDATE), form, AUTHENTICATION_REALM, FILESYS_RLM_UPDATE3);
    }

    @Test
    public void aggregateRealmTryUpdate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, AGGREGATE_REALM_ITEM);
        TableFragment table = page.getAggregateRealmTable();
        FormFragment form = page.getAggregateRealmForm();
        table.bind(form);
        table.select(AGG_RLM_UPDATE);
        crud.updateWithError(form, f -> f.clear(AUTHENTICATION_REALM), AUTHENTICATION_REALM);
    }

    @Test
    public void aggregateRealmDelete() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, AGGREGATE_REALM_ITEM);
        TableFragment table = page.getAggregateRealmTable();
        crud.delete(aggregateRealmAddress(AGG_RLM_DELETE), table, AGG_RLM_DELETE);
    }

    // --------------- caching-realm

    @Test
    public void cachingRealmCreate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, CACHING_REALM_ITEM);
        TableFragment table = page.getCachingRealmTable();

        crud.create(cachingRealmAddress(CAC_RLM_CREATE), table, f -> {
            f.text(NAME, CAC_RLM_CREATE);
            f.text(REALM, FILESYS_RLM_UPDATE);
        });
    }

    @Test
    public void cachingRealmTryCreate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, CACHING_REALM_ITEM);
        TableFragment table = page.getCachingRealmTable();
        crud.createWithErrorAndCancelDialog(table, f -> f.text(NAME, CAC_RLM_CREATE), REALM);
    }

    @Test
    public void cachingRealmUpdate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, CACHING_REALM_ITEM);
        TableFragment table = page.getCachingRealmTable();
        FormFragment form = page.getCachingRealmForm();
        table.bind(form);
        table.select(CAC_RLM_UPDATE);
        crud.update(cachingRealmAddress(CAC_RLM_UPDATE), form, MAXIMUM_AGE, 123L);
    }

    @Test
    public void cachingRealmTryUpdate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, CACHING_REALM_ITEM);
        TableFragment table = page.getCachingRealmTable();
        FormFragment form = page.getCachingRealmForm();
        table.bind(form);
        table.select(CAC_RLM_UPDATE);
        crud.updateWithError(form, f -> f.clear(REALM), REALM);
    }

    @Test
    public void cachingRealmDelete() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, CACHING_REALM_ITEM);
        TableFragment table = page.getCachingRealmTable();
        crud.delete(cachingRealmAddress(CAC_RLM_DELETE), table, CAC_RLM_DELETE);
    }

    // --------------- filesystem-realm

    @Test
    public void filesystemRealmCreate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, FILESYSTEM_REALM_ITEM);
        TableFragment table = page.getFilesystemRealmTable();

        crud.create(filesystemRealmAddress(FILESYS_RLM_CREATE), table, f -> {
            f.text(NAME, FILESYS_RLM_CREATE);
            f.text(PATH, ANY_STRING);
        });
    }

    @Test
    public void filesystemRealmTryCreate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, FILESYSTEM_REALM_ITEM);
        TableFragment table = page.getFilesystemRealmTable();
        crud.createWithErrorAndCancelDialog(table, f -> f.text(NAME, FILESYS_RLM_CREATE), PATH);
    }

    @Test
    public void filesystemRealmUpdate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, FILESYSTEM_REALM_ITEM);
        TableFragment table = page.getFilesystemRealmTable();
        FormFragment form = page.getFilesystemRealmForm();
        table.bind(form);
        table.select(FILESYS_RLM_UPDATE);
        crud.update(filesystemRealmAddress(FILESYS_RLM_UPDATE), form, LEVELS, 123);
    }

    @Test
    public void filesystemRealmTryUpdate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, FILESYSTEM_REALM_ITEM);
        TableFragment table = page.getFilesystemRealmTable();
        FormFragment form = page.getFilesystemRealmForm();
        table.bind(form);
        table.select(FILESYS_RLM_UPDATE);
        crud.updateWithError(form, f -> f.clear(PATH), PATH);
    }

    @Test
    public void filesystemRealmDelete() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, FILESYSTEM_REALM_ITEM);
        TableFragment table = page.getFilesystemRealmTable();
        crud.delete(filesystemRealmAddress(FILESYS_RLM_DELETE), table, FILESYS_RLM_DELETE);
    }

    // --------------- identity-realm

    @Test
    public void identityRealmCreate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, IDENTITY_REALM_ITEM);
        TableFragment table = page.getIdentityRealmTable();

        crud.create(identityRealmAddress(IDEN_RLM_CREATE), table, f -> {
            f.text(NAME, IDEN_RLM_CREATE);
            f.text(IDENTITY, ANY_STRING);
        });
    }

    @Test
    public void identityRealmTryCreate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, IDENTITY_REALM_ITEM);
        TableFragment table = page.getIdentityRealmTable();
        crud.createWithErrorAndCancelDialog(table, f -> f.text(NAME, IDEN_RLM_CREATE), IDENTITY);
    }

    @Test
    public void identityRealmUpdate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, IDENTITY_REALM_ITEM);
        TableFragment table = page.getIdentityRealmTable();
        FormFragment form = page.getIdentityRealmForm();
        table.bind(form);
        table.select(IDEN_RLM_UPDATE);
        crud.update(identityRealmAddress(IDEN_RLM_UPDATE), form, ATTRIBUTE_NAME);
    }

    @Test
    public void identityRealmTryUpdate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, IDENTITY_REALM_ITEM);
        TableFragment table = page.getIdentityRealmTable();
        FormFragment form = page.getIdentityRealmForm();
        table.bind(form);
        table.select(IDEN_RLM_UPDATE);
        crud.updateWithError(form, f -> f.clear(IDENTITY), IDENTITY);
    }

    @Test
    public void identityRealmDelete() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, IDENTITY_REALM_ITEM);
        TableFragment table = page.getIdentityRealmTable();
        crud.delete(identityRealmAddress(IDEN_RLM_DELETE), table, IDEN_RLM_DELETE);
    }

    // --------------- keystore-realm

    @Test
    public void keystoreRealmCreate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, KEYSTORE_REALM_ITEM);
        TableFragment table = page.getKeyStoreRealmTable();

        crud.create(keystoreRealmAddress(KS_RLM_CREATE), table, f -> {
            f.text(NAME, KS_RLM_CREATE);
            f.text(KEY_STORE, KEY_ST_UPDATE);
        });
    }

    @Test
    public void keystoreRealmTryCreate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, KEYSTORE_REALM_ITEM);
        TableFragment table = page.getKeyStoreRealmTable();
        crud.createWithErrorAndCancelDialog(table, f -> f.text(NAME, KS_RLM_CREATE), KEY_STORE);
    }

    @Test
    public void keystoreRealmUpdate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, KEYSTORE_REALM_ITEM);
        TableFragment table = page.getKeyStoreRealmTable();
        FormFragment form = page.getKeyStoreRealmForm();
        table.bind(form);
        table.select(KS_RLM_UPDATE);
        crud.update(keystoreRealmAddress(KS_RLM_UPDATE), form, KEY_STORE, KEY_ST_CREATE);
    }

    @Test
    public void keystoreRealmTryUpdate() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, KEYSTORE_REALM_ITEM);
        TableFragment table = page.getKeyStoreRealmTable();
        FormFragment form = page.getKeyStoreRealmForm();
        table.bind(form);
        table.select(KS_RLM_UPDATE);
        crud.updateWithError(form, f -> f.clear(KEY_STORE), KEY_STORE);
    }

    @Test
    public void keystoreRealmDelete() throws Exception {
        console.verticalNavigation().selectSecondary(SECURITY_REALM_ITEM, KEYSTORE_REALM_ITEM);
        TableFragment table = page.getKeyStoreRealmTable();
        crud.delete(keystoreRealmAddress(KS_RLM_DELETE), table, KS_RLM_DELETE);
    }


}
