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
import java.util.Collection;
import java.util.List;

import net.shibboleth.metadata.dom.BaseDomTest;
import net.shibboleth.metadata.dom.DomMetadata;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.xml.ElementSupport;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** Unit test for {@link EntityFilterStage}. */
public class EntityFilterStageTest extends BaseDomTest {

    /** Test whitelisted entity is retained and ensure everything else is removed. */
    @Test
    public void testEntityWhitelist() throws Exception {
        EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(CollectionSupport.toList("https://idp.shibboleth.net/idp/shibboleth"));
        stage.setWhitelistingEntities(true);

        Collection<DomMetadata> metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 1);
    }

    /** Test blacklisted entity is remove and ensure everything else is retained. */
    @Test
    public void testEntityBlacklist() throws Exception {
        EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(CollectionSupport.toList("https://idp.shibboleth.net/idp/shibboleth"));
        stage.setWhitelistingEntities(false);

        Collection<DomMetadata> metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 2);
    }

    /** Test that filtering logic descends in to EntitiesDescriptors. */
    @Test
    public void testEntitiesDescriptorFiltering() throws Exception {
        ArrayList<DomMetadata> metadataCollection = new ArrayList<DomMetadata>();
        metadataCollection.add(new DomMetadata(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(CollectionSupport.toList("https://idp.shibboleth.net/idp/shibboleth"));
        stage.setWhitelistingEntities(false);
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.iterator().next().getMetadata();
        Assert.assertEquals(ElementSupport.getChildElements(entitiesDescriptor).size(), 2);
    }

    /**
     * Test that EntitiesDescriptors that have had all their EntityDescriptor children remove are themselves removed.
     */
    @Test
    public void testRemoveEntitylessEntitiesDescriptor() throws Exception {
        ArrayList<DomMetadata> metadataCollection = new ArrayList<DomMetadata>();
        metadataCollection.add(new DomMetadata(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setDesignatedEntities(CollectionSupport.toList("https://idp.shibboleth.net/idp/shibboleth",
                "https://issues.shibboleth.net/shibboleth", "https://wiki.shibboleth.net/shibboleth"));
        stage.setWhitelistingEntities(false);
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 0);
    }

    /**
     * Test that EntitiesDescriptors that have had all their EntityDescriptor children remove are not themselves removed
     * when {@link EntityFilterStage#isRemovingEntitylessEntitiesDescriptor()} is false.
     */
    @Test
    public void testDontRemoveEntitylessEntitiesDescriptor() throws Exception {
        ArrayList<DomMetadata> metadataCollection = new ArrayList<DomMetadata>();
        metadataCollection.add(new DomMetadata(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        EntityFilterStage stage = new EntityFilterStage();
        stage.setId("test");
        stage.setRemovingEntitylessEntitiesDescriptor(false);
        stage.setDesignatedEntities(CollectionSupport.toList("https://idp.shibboleth.net/idp/shibboleth",
                "https://issues.shibboleth.net/shibboleth", "https://wiki.shibboleth.net/shibboleth"));
        stage.setWhitelistingEntities(false);
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 1);
    }

    /** Build up a metadata collection containing 3 EntityDescriptors. */
    private Collection<DomMetadata> buildMetadataCollection() throws Exception {
        ArrayList<DomMetadata> metadataCollection = new ArrayList<DomMetadata>();

        List<Element> descriptors = ElementSupport
                .getChildElements(readXmlData("samlMetadata/entitiesDescriptor1.xml"));
        for (Element descriptor : descriptors) {
            metadataCollection.add(new DomMetadata(descriptor));
        }

        return metadataCollection;
    }
}