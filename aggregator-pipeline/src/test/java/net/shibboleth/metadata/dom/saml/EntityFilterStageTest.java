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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** Unit test for {@link EntityFilterStage}. */
public class EntityFilterStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public EntityFilterStageTest() {
        super(EntityFilterStage.class);
    }

    /** Test whitelisted entity is retained and ensure everything else is removed. */
    @Test public void testEntityWhitelist() throws Exception {
        final EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(Collections.singletonList("https://idp.shibboleth.net/idp/shibboleth"));
        stage.setWhitelistingEntities(true);
        stage.initialize();

        Collection<Item<Element>> metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 1);
    }

    /** Test blacklisted entity is remove and ensure everything else is retained. */
    @Test public void testEntityBlacklist() throws Exception {
        final EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(Collections.singletonList("https://idp.shibboleth.net/idp/shibboleth"));
        stage.setWhitelistingEntities(false);
        stage.initialize();

        Collection<Item<Element>> metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 2);
    }

    /** Test that filtering logic descends in to EntitiesDescriptors. */
    @Test public void testEntitiesDescriptorFiltering() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXMLData("in.xml")));

        final EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(Collections.singletonList("https://idp.shibboleth.net/idp/shibboleth"));
        stage.setWhitelistingEntities(false);
        stage.initialize();
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.iterator().next().unwrap();
        Assert.assertEquals(ElementSupport.getChildElements(entitiesDescriptor).size(), 2);
    }

    /**
     * Test that EntitiesDescriptors that have had all their EntityDescriptor children remove are themselves removed.
     */
    @Test public void testRemoveEntitylessEntitiesDescriptor() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXMLData("in.xml")));

        final EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(Arrays.asList(new String[]{"https://idp.shibboleth.net/idp/shibboleth",
                "https://issues.shibboleth.net/shibboleth", "https://wiki.shibboleth.net/shibboleth"}));
        stage.setWhitelistingEntities(false);
        stage.initialize();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 0);
    }

    /**
     * Test that EntitiesDescriptors that have had all their EntityDescriptor children remove are not themselves removed
     * when {@link EntityFilterStage#isRemovingEntitylessEntitiesDescriptor()} is false.
     */
    @Test public void testDontRemoveEntitylessEntitiesDescriptor() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXMLData("in.xml")));

        final EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setRemovingEntitylessEntitiesDescriptor(false);
        stage.setDesignatedEntities(Arrays.asList(new String[]{"https://idp.shibboleth.net/idp/shibboleth",
                "https://issues.shibboleth.net/shibboleth", "https://wiki.shibboleth.net/shibboleth"}));
        stage.setWhitelistingEntities(false);
        stage.initialize();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 1);
    }

    /**
     * Test that whitelisting an empty set of IDs removes everything from the collection.
     */
    @Test public void testWhitelistEmptySet() throws Exception {
        final var metadataCollection = buildMetadataCollection();

        final var stage = new EntityFilterStage();
        stage.setId("test");
        stage.setWhitelistingEntities(true);
        stage.setDesignatedEntities(Collections.emptySet());
        stage.initialize();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 0);
    }

    /**
     * Test that blacklisting an empty set of IDs leaves everything in the collection.
     */
    @Test public void testBlacklistEmptySet() throws Exception {
        final var metadataCollection = buildMetadataCollection();

        final var stage = new EntityFilterStage();
        stage.setId("test");
        stage.setWhitelistingEntities(false);
        stage.setDesignatedEntities(Collections.emptySet());
        stage.initialize();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 3);
    }
    
    /** Build up a metadata collection containing 3 EntityDescriptors. */
    private Collection<Item<Element>> buildMetadataCollection() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();

        List<Element> descriptors =
                ElementSupport.getChildElements(readXMLData("in.xml"));
        for (Element descriptor : descriptors) {
            metadataCollection.add(new DOMElementItem(descriptor));
        }

        return metadataCollection;
    }
}