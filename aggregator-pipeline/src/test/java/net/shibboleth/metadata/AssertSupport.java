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

package net.shibboleth.metadata;

import java.util.List;

import net.shibboleth.metadata.pipeline.ComponentInfo;
import net.shibboleth.utilities.java.support.component.Component;

import org.testng.Assert;

/** Helper class that offers additional assertion checks. */
public final class AssertSupport {

    /** Constructor. */
    private AssertSupport() {
    }

    /**
     * Checks that the given metadata element contains {@link ComponentInfo} information.
     * 
     * @param metadataElement element to check, never null
     * @param expectedComponentInfos number of {@link ComponentInfo}s expected on the element, must be greater than 0
     * @param expectedComponentType type of {@link Component} that created that {@link ComponentInfo}, never null
     * @param expectedComponentId ID of the {@link Component} that created that {@link ComponentInfo}, never null
     */
    public static void assertValidComponentInfo(Item<?> metadataElement, int expectedComponentInfos,
            Class<? extends Component> expectedComponentType, String expectedComponentId) {
        net.shibboleth.utilities.java.support.logic.Assert.isNotNull(metadataElement,
                "Metadata element must not be null");
        net.shibboleth.utilities.java.support.logic.Assert.isGreaterThan(0, expectedComponentInfos,
                "Expected ComponentInfos must be greater than 0");
        net.shibboleth.utilities.java.support.logic.Assert.isNotNull(expectedComponentType,
                "Expected Component type must not be null");
        net.shibboleth.utilities.java.support.logic.Assert.isNotNull(expectedComponentId,
                "Expected Component ID must not be null");

        List<ComponentInfo> compInfos = metadataElement.getItemMetadata().get(ComponentInfo.class);
        Assert.assertEquals(compInfos.size(), expectedComponentInfos);

        ComponentInfo compInfo;
        for (int i = 0; i < expectedComponentInfos; i++) {
            compInfo = compInfos.get(i);
            if (expectedComponentType.equals(compInfo.getComponentType())
                    && expectedComponentId.equals(compInfo.getComponentId())) {
                Assert.assertNotNull(compInfo.getCompleteInstant());
                return;
            }
        }

        Assert.fail("Metadata element does not contain a ComponentInfo from a component of type "
                + expectedComponentType.getCanonicalName() + " with an ID of " + expectedComponentId);
    }
}