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

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** Unit test for {@link EntityDescriptorItemIdPopulationStage}. */
public class EntityDescriptorItemIdPopulationStageTest extends BaseDOMTest {

    /** Tests running the stage on an empty collection. */
    @Test
    public void testEmptyCollection() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();

        EntityDescriptorItemIdPopulationStage stage = new EntityDescriptorItemIdPopulationStage();
        stage.setId("foo");
        stage.initialize();

        stage.execute(metadataCollection);
        Assert.assertTrue(metadataCollection.isEmpty());
    }

    /** Tests running the stage on a collection that does not contain EntityDescriptors. */
    @Test
    public void testDifferentElement() throws Exception {
        final ArrayList<DOMElementItem> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        EntityDescriptorItemIdPopulationStage stage = new EntityDescriptorItemIdPopulationStage();
        stage.setId("foo");
        stage.initialize();

        List<ItemId> itemIds = metadataCollection.get(0).getItemMetadata().get(ItemId.class);
        Assert.assertEquals(itemIds.size(), 0);
    }

    /** Tests running the stage on a collection that contains a single EntityDescriptor. */
    @Test
    public void testSingleRecord() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXmlData("samlMetadata/entityDescriptor1.xml")));

        EntityDescriptorItemIdPopulationStage stage = new EntityDescriptorItemIdPopulationStage();
        stage.setId("foo");
        stage.initialize();

        stage.execute(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 1);

        Item<Element> item = metadataCollection.get(0);
        Element entityDescriptor = item.unwrap();

        List<ItemId> itemIds = item.getItemMetadata().get(ItemId.class);
        Assert.assertEquals(itemIds.size(), 1);

        ItemId itemId = itemIds.get(0);
        Assert.assertEquals(itemId.getId(), entityDescriptor.getAttributeNS(null, "entityID"));
    }

    /** Tests running the stage on a collection that contains multiple EntityDescriptors. */
    @Test
    public void testMultipleRecords() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXmlData("samlMetadata/entityDescriptor1.xml")));
        metadataCollection.add(new DOMElementItem(readXmlData("samlMetadata/entityDescriptor2.xml")));

        EntityDescriptorItemIdPopulationStage stage = new EntityDescriptorItemIdPopulationStage();
        stage.setId("foo");
        stage.initialize();

        stage.execute(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 2);

        Element entityDescriptor;
        List<ItemId> itemIds;
        ItemId itemId;
        for (Item<Element> item : metadataCollection) {
            entityDescriptor = item.unwrap();

            itemIds = item.getItemMetadata().get(ItemId.class);
            Assert.assertEquals(itemIds.size(), 1);

            itemId = itemIds.get(0);
            Assert.assertEquals(itemId.getId(), entityDescriptor.getAttributeNS(null, "entityID"));
        }
    }
}
