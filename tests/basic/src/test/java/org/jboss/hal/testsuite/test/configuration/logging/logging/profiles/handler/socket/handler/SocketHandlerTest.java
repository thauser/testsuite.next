/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.testsuite.test.configuration.logging.logging.profiles.handler.socket.handler;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.page.configuration.LoggingConfigurationPage;
import org.jboss.hal.testsuite.page.configuration.LoggingProfileConfigurationPage;
import org.jboss.hal.testsuite.test.configuration.logging.LoggingFixtures;
import org.jboss.hal.testsuite.test.configuration.logging.SocketHandlerAbstractTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OUTBOUND_SOCKET_BINDING_REF;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATTERN_FORMATTER;
import static org.jboss.hal.testsuite.test.configuration.logging.LoggingFixtures.*;

@RunWith(Arquillian.class)
public class SocketHandlerTest extends SocketHandlerAbstractTest {

    private static final String LOGGING_PROFILE = "logging-profile-" + Random.name();

    @Page
    private LoggingProfileConfigurationPage page;

    @BeforeClass
    public static void createResources() throws IOException {
        ops.add(LoggingFixtures.LoggingProfile.loggingProfileAddress(LOGGING_PROFILE)).assertSuccess();
        ops.add(LoggingFixtures.LoggingProfile.loggingProfileAddress(LOGGING_PROFILE)
            .and(PATTERN_FORMATTER, PatternFormatter.PATTERN_FORMATTER_CREATE)).assertSuccess();
        Values params = Values.of(NAMED_FORMATTER, PatternFormatter.PATTERN_FORMATTER_CREATE)
            .and(OUTBOUND_SOCKET_BINDING_REF, "mail-smtp");
        ops.add(LoggingFixtures.LoggingProfile.socketHandlerAddress(LOGGING_PROFILE, SocketHandler.SOCKET_HANDLER_UPDATE), params).assertSuccess();
        ops.add(LoggingFixtures.LoggingProfile.socketHandlerAddress(LOGGING_PROFILE, SocketHandler.SOCKET_HANDLER_DELETE), params).assertSuccess();
        ops.add(LoggingFixtures.LoggingProfile.xmlFormatterAddress(LOGGING_PROFILE, XML_FORMATTER)).assertSuccess();
    }

    @Override
    protected String getPatternFormatter() {
        return PatternFormatter.PATTERN_FORMATTER_CREATE;
    }

    @AfterClass
    public static void removeResourcesAndReload() throws IOException, OperationException, InterruptedException, TimeoutException {
        ops.removeIfExists(LoggingFixtures.LoggingProfile.loggingProfileAddress(LOGGING_PROFILE));
        adminOps.reloadIfRequired();
    }

    @Override
    protected void navigateToPage() {
        page.navigate(NAME, LOGGING_PROFILE);
        console.verticalNavigation().selectSecondary(LOGGING_PROFILE_HANDLER_ITEM,
            "logging-profile-handler-socket-item");
    }

    @Override
    protected LoggingConfigurationPage getPage() {
        return page;
    }

    @Override
    protected Address socketHandlerAddress(String name) {
        return LoggingFixtures.LoggingProfile.socketHandlerAddress(LOGGING_PROFILE, name);
    }

    @Override
    protected TableFragment getHandlerTable() {
        return page.getSocketHandlerTable();
    }

    @Override
    protected FormFragment getHandlerForm() {
        return page.getSocketHandlerForm();
    }
}
