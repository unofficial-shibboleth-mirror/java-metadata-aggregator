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
import java.util.Collection;
import java.util.List;

import net.shibboleth.metadata.dom.BaseDomTest;
import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;

/** Unit test for {@link EntityFilterStage}. */
public class EntityFilterStageTest extends BaseDomTest {

    /** Test whitelisted entity is retained and ensure everything else is removed. */
    @Test public void testEntityWhitelist() throws Exception {
        EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(Lists.newArrayList("https://idp.shibboleth.net/idp/shibboleth"));
        stage.setWhitelistingEntities(true);
        stage.initialize();

        Collection<DomElementItem> metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 1);
    }

    /** Test blacklisted entity is remove and ensure everything else is retained. */
    @Test public void testEntityBlacklist() throws Exception {
        EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(Lists.newArrayList("https://idp.shibboleth.net/idp/shibboleth"));
        stage.setWhitelistingEntities(false);
        stage.initialize();

        Collection<DomElementItem> metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 2);
    }

    /** Test that filtering logic descends in to EntitiesDescriptors. */
    @Test public void testEntitiesDescriptorFiltering() throws Exception {
        final ArrayList<DomElementItem> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(Lists.newArrayList("https://idp.shibboleth.net/idp/shibboleth"));
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
        final ArrayList<DomElementItem> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(Lists.newArrayList("https://idp.shibboleth.net/idp/shibboleth",
                "https://issues.shibboleth.net/shibboleth", "https://wiki.shibboleth.net/shibboleth"));
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
        final ArrayList<DomElementItem> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setRemovingEntitylessEntitiesDescriptor(false);
        stage.setDesignatedEntities(Lists.newArrayList("https://idp.shibboleth.net/idp/shibboleth",
                "https://issues.shibboleth.net/shibboleth", "https://wiki.shibboleth.net/shibboleth"));
        stage.setWhitelistingEntities(false);
        stage.initialize();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 1);
    }

    /** Build up a metadata collection containing 3 EntityDescriptors. */
    private Collection<DomElementItem> buildMetadataCollection() throws Exception {
        final ArrayList<DomElementItem> metadataCollection = new ArrayList<>();

        List<Element> descriptors =
                ElementSupport.getChildElements(readXmlData("samlMetadata/entitiesDescriptor1.xml"));
        for (Element descriptor : descriptors) {
            metadataCollection.add(new DomElementItem(descriptor));
        }

        return metadataCollection;
    }
}