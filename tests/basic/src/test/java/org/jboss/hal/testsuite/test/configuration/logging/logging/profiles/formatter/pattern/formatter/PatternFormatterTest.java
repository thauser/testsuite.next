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
package org.jboss.hal.testsuite.test.configuration.logging.logging.profiles.formatter.pattern.formatter;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.page.configuration.LoggingConfigurationPage;
import org.jboss.hal.testsuite.page.configuration.LoggingProfileConfigurationPage;
import org.jboss.hal.testsuite.test.configuration.logging.LoggingFixtures;
import org.jboss.hal.testsuite.test.configuration.logging.PatternFormatterAbstractTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

import static org.jboss.hal.testsuite.test.configuration.logging.LoggingFixtures.LOGGING_PROFILE_FORMATTER_ITEM;
import static org.jboss.hal.testsuite.test.configuration.logging.LoggingFixtures.NAME;
import static org.jboss.hal.testsuite.test.configuration.logging.LoggingFixtures.PatternFormatter.PATTERN_FORMATTER_DELETE;
import static org.jboss.hal.testsuite.test.configuration.logging.LoggingFixtures.PatternFormatter.PATTERN_FORMATTER_UPDATE;

@RunWith(Arquillian.class)
public class PatternFormatterTest extends PatternFormatterAbstractTest {

    private static final String LOGGING_PROFILE = "logging-profile-" + Random.name();

    @Page
    private LoggingProfileConfigurationPage page;

    @BeforeClass
    public static void createResources() throws IOException {
        ops.add(LoggingFixtures.LoggingProfile.loggingProfileAddress(LOGGING_PROFILE)).assertSuccess();
        ops.add(LoggingFixtures.LoggingProfile.patternFormatterAddress(LOGGING_PROFILE, PATTERN_FORMATTER_UPDATE))
            .assertSuccess();
        ops.add(LoggingFixtures.LoggingProfile.patternFormatterAddress(LOGGING_PROFILE, PATTERN_FORMATTER_DELETE))
            .assertSuccess();
    }

    @AfterClass
    public static void removeResources() throws IOException, OperationException {
        ops.removeIfExists(LoggingFixtures.LoggingProfile.loggingProfileAddress(LOGGING_PROFILE));
    }

    @Override
    protected void navigateToPage() {
        page.navigate(NAME, LOGGING_PROFILE);
        console.verticalNavigation().selectSecondary(LOGGING_PROFILE_FORMATTER_ITEM,
            "logging-profile-formatter-pattern-item");
    }

    @Override
    protected LoggingConfigurationPage getPage() {
        return page;
    }

    @Override
    protected Address patternFormatterAddress(String name) {
        return LoggingFixtures.LoggingProfile.patternFormatterAddress(LOGGING_PROFILE, name);
    }
}
