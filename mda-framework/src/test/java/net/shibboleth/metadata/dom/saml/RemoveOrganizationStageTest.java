/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;
import net.shibboleth.shared.xml.ElementSupport;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** Unit test for {@link RemoveOrganizationStage}. */
public class RemoveOrganizationStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public RemoveOrganizationStageTest() {
        super(RemoveOrganizationStage.class);
    }

    /**
     * Test the organization elements are removed from top level metadata elements.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testRemoveOrganization() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        List<Element> descriptors = ElementSupport
                .getChildElements(readXMLData("in.xml"));
        for (Element descriptor : descriptors) {
            assert descriptor != null;
            metadataCollection.add(new DOMElementItem(descriptor));
        }

        for (Item<Element> metadata : metadataCollection) {
            Assert.assertFalse(ElementSupport.getChildElementsByTagNameNS(metadata.unwrap(), SAMLMetadataSupport.MD_NS,
                    "Organization").isEmpty());
        }

        RemoveOrganizationStage stage = new RemoveOrganizationStage();
        stage.setId("test");
        stage.initialize();
        
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 3);

        for (Item<Element> metadata : metadataCollection) {
            Assert.assertTrue(ElementSupport.getChildElementsByTagNameNS(metadata.unwrap(), SAMLMetadataSupport.MD_NS,
                    "Organization").isEmpty());
        }
    }

    /**
     * Test that contact person elements are removed from children of top level metadata elements.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testRemoveOrganizationFromNestedElements() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXMLData("in.xml")));

        Element entitiesDescriptor = metadataCollection.get(0).unwrap();
        List<Element> entityDescriptors = ElementSupport.getChildElements(entitiesDescriptor);
        for (Element entityDescriptor : entityDescriptors) {
            assert entityDescriptor != null;
            Assert.assertFalse(ElementSupport.getChildElementsByTagNameNS(entityDescriptor, SAMLMetadataSupport.MD_NS,
                    "Organization").isEmpty());
        }

        RemoveOrganizationStage stage = new RemoveOrganizationStage();
        stage.setId("test");
        stage.initialize();
        
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 1);

        for (Element entityDescriptor : entityDescriptors) {
            assert entityDescriptor != null;
            Assert.assertTrue(ElementSupport.getChildElementsByTagNameNS(entityDescriptor, SAMLMetadataSupport.MD_NS,
                    "Organization").isEmpty());
        }
    }
}
