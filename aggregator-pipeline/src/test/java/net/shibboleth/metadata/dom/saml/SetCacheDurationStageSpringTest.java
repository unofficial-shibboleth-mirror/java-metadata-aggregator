/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.dom.saml;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration({"SetCacheDurationStageSpringTest-config.xml"})
public class SetCacheDurationStageSpringTest extends AbstractTestNGSpringContextTests {
    
    @Autowired
    SetCacheDurationStage stage;
    
    @Test
    public void testDuration() throws Exception {
        // one day, 17 hours, 34 minutes and 19 seconds = P1DT17H34M19S
        final var expectedDuration = Duration.ofDays(1).plusHours(17).plusMinutes(34).plusSeconds(19);
        Assert.assertEquals(stage.getCacheDuration(), expectedDuration);
    }

}
