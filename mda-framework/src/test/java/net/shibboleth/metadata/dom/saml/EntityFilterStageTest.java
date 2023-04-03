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

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.xml.ElementSupport;

/** Unit test for {@link EntityFilterStage}. */
public class EntityFilterStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public EntityFilterStageTest() {
        super(EntityFilterStage.class);
    }

    /**
     * Test whitelisted entity is retained and ensure everything else is removed.
     * 
     * @throws Exception if something bad happens
     */
    @Test public void testEntityWhitelist() throws Exception {
        final EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(CollectionSupport.listOf("https://idp.shibboleth.net/idp/shibboleth"));
        stage.setWhitelistingEntities(true);
        stage.initialize();

        final var metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);
        stage.destroy();

        Assert.assertEquals(metadataCollection.size(), 1);
    }

    /**
     * Test blacklisted entity is remove and ensure everything else is retained.
     * 
     * @throws Exception if something bad happens
     */
    @Test public void testEntityBlacklist() throws Exception {
        final EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(CollectionSupport.listOf("https://idp.shibboleth.net/idp/shibboleth"));
        stage.setWhitelistingEntities(false);
        stage.initialize();

        final var metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);
        stage.destroy();

        Assert.assertEquals(metadataCollection.size(), 2);
    }

    /**
     * Test that filtering logic descends in to EntitiesDescriptors.
     * 
     * @throws Exception if something bad happens
     */
    @Test public void testEntitiesDescriptorFiltering() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXMLData("in.xml")));

        final EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(CollectionSupport.listOf("https://idp.shibboleth.net/idp/shibboleth"));
        stage.setWhitelistingEntities(false);
        stage.initialize();
        stage.execute(metadataCollection);
        stage.destroy();

        Element entitiesDescriptor = metadataCollection.iterator().next().unwrap();
        Assert.assertEquals(ElementSupport.getChildElements(entitiesDescriptor).size(), 2);
    }

    /**
     * Test that EntitiesDescriptors that have had all their EntityDescriptor children remove are themselves removed.
     * 
     * @throws Exception if something bad happens
     */
    @Test public void testRemoveEntitylessEntitiesDescriptor() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXMLData("in.xml")));

        final EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(CollectionSupport.listOf("https://idp.shibboleth.net/idp/shibboleth",
                "https://issues.shibboleth.net/shibboleth", "https://wiki.shibboleth.net/shibboleth"));
        stage.setWhitelistingEntities(false);
        stage.initialize();
        stage.execute(metadataCollection);
        stage.destroy();

        Assert.assertEquals(metadataCollection.size(), 0);
    }

    /**
     * Test that EntitiesDescriptors that have had all their EntityDescriptor children remove are not themselves removed
     * when {@link EntityFilterStage#isRemovingEntitylessEntitiesDescriptor()} is false.
     * 
     * @throws Exception if something bad happens
     */
    @Test public void testDontRemoveEntitylessEntitiesDescriptor() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXMLData("in.xml")));

        final EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setRemovingEntitylessEntitiesDescriptor(false);
        stage.setDesignatedEntities(CollectionSupport.listOf("https://idp.shibboleth.net/idp/shibboleth",
                "https://issues.shibboleth.net/shibboleth", "https://wiki.shibboleth.net/shibboleth"));
        stage.setWhitelistingEntities(false);
        stage.initialize();
        stage.execute(metadataCollection);
        stage.destroy();

        Assert.assertEquals(metadataCollection.size(), 1);
    }

    /**
     * Test that whitelisting an empty set of IDs removes everything from the collection.
     * 
     * @throws Exception if something bad happens
     */
    @Test public void testWhitelistEmptySet() throws Exception {
        final var metadataCollection = buildMetadataCollection();

        final var stage = new EntityFilterStage();
        stage.setId("test");
        stage.setWhitelistingEntities(true);
        stage.setDesignatedEntities(CollectionSupport.emptySet());
        stage.initialize();
        stage.execute(metadataCollection);
        stage.destroy();

        Assert.assertEquals(metadataCollection.size(), 0);
    }

    /**
     * Test that blacklisting an empty set of IDs leaves everything in the collection.
     * 
     * @throws Exception if something bad happens
     */
    @Test public void testBlacklistEmptySet() throws Exception {
        final var metadataCollection = buildMetadataCollection();

        final var stage = new EntityFilterStage();
        stage.setId("test");
        stage.setWhitelistingEntities(false);
        stage.setDesignatedEntities(CollectionSupport.emptySet());
        stage.initialize();
        stage.execute(metadataCollection);
        stage.destroy();

        Assert.assertEquals(metadataCollection.size(), 3);
    }
    
    /**
     * Build up a metadata collection containing three EntityDescriptors.
     * 
     * @return metadata collection
     * 
     * @throws Exception if something bad happens
     */
    private @Nonnull List<Item<Element>> buildMetadataCollection() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();

        List<Element> descriptors =
                ElementSupport.getChildElements(readXMLData("in.xml"));
        for (Element descriptor : descriptors) {
            assert descriptor != null;
            metadataCollection.add(new DOMElementItem(descriptor));
        }

        return metadataCollection;
    }
}
