/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
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

package net.shibboleth.metadata.dom.saml;

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.dom.BaseDomTest;
import net.shibboleth.metadata.dom.DomElementItem;

import org.opensaml.util.xml.ElementSupport;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** Unit test for {@link RemoveOrganizationStage}. */
public class RemoveOrganizationStageTest extends BaseDomTest {

    /** Test the organization elements are removed from top level metadata elements. */
    @Test
    public void testRemoveOrganization() throws Exception {
        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        List<Element> descriptors = ElementSupport
                .getChildElements(readXmlData("samlMetadata/entitiesDescriptor1.xml"));
        for (Element descriptor : descriptors) {
            metadataCollection.add(new DomElementItem(descriptor));
        }

        for (DomElementItem metadata : metadataCollection) {
            Assert.assertFalse(ElementSupport.getChildElementsByTagNameNS(metadata.unwrap(), MetadataHelper.MD_NS,
                    "Organization").isEmpty());
        }

        RemoveOrganizationStage stage = new RemoveOrganizationStage();
        stage.setId("test");
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 3);

        for (DomElementItem metadata : metadataCollection) {
            Assert.assertTrue(ElementSupport.getChildElementsByTagNameNS(metadata.unwrap(), MetadataHelper.MD_NS,
                    "Organization").isEmpty());
        }
    }

    /** Test that contact person elements are removed from children of top level metadata elements. */
    @Test
    public void testRemoveOrganizationFromNestedElements() throws Exception {
        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        metadataCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        Element entitiesDescriptor = metadataCollection.get(0).unwrap();
        List<Element> entityDescriptors = ElementSupport.getChildElements(entitiesDescriptor);
        for (Element entityDescriptor : entityDescriptors) {
            Assert.assertFalse(ElementSupport.getChildElementsByTagNameNS(entityDescriptor, MetadataHelper.MD_NS,
                    "Organization").isEmpty());
        }

        RemoveOrganizationStage stage = new RemoveOrganizationStage();
        stage.setId("test");
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 1);

        for (Element entityDescriptor : entityDescriptors) {
            Assert.assertTrue(ElementSupport.getChildElementsByTagNameNS(entityDescriptor, MetadataHelper.MD_NS,
                    "Organization").isEmpty());
        }
    }
}