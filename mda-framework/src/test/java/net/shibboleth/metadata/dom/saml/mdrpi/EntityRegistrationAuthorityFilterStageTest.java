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

package net.shibboleth.metadata.dom.saml.mdrpi;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.xml.ElementSupport;

/** Unit test for {@link EntityRegistrationAuthorityFilterStage}. */
public class EntityRegistrationAuthorityFilterStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public EntityRegistrationAuthorityFilterStageTest() {
        super(EntityRegistrationAuthorityFilterStage.class);
    }

    /**
     * Tests filtering out Items based on an authority whitelist.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testAuthorityWhitelist() throws Exception {
        EntityRegistrationAuthorityFilterStage stage = new EntityRegistrationAuthorityFilterStage();
        stage.setId("test");
        stage.setRequiringRegistrationInformation(false);
        stage.setWhitelistingRegistrationAuthorities(true);
        stage.setDesignatedRegistrationAuthorities(CollectionSupport.listOf("urn:example.org:authority2"));
        stage.initialize();

        final var mdCollection = buildMetadataCollection();
        Assert.assertEquals(mdCollection.size(), 3);

        stage.execute(mdCollection);
        stage.destroy();
        Assert.assertEquals(mdCollection.size(), 2);
    }

    /**
     * Tests filtering out Items based on an authority blacklist.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testAuthorityBlacklist() throws Exception {
        EntityRegistrationAuthorityFilterStage stage = new EntityRegistrationAuthorityFilterStage();
        stage.setId("test");
        stage.setWhitelistingRegistrationAuthorities(false);
        stage.setRequiringRegistrationInformation(false);
        stage.setDesignatedRegistrationAuthorities(CollectionSupport.listOf("urn:example.org:authority2"));
        stage.initialize();

        final var mdCollection = buildMetadataCollection();
        Assert.assertEquals(mdCollection.size(), 3);

        stage.execute(mdCollection);
        stage.destroy();
        Assert.assertEquals(mdCollection.size(), 1);
    }

    /**
     * Tests filtering out Items that do not contain registration information.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testRequireRegistrationInfo() throws Exception {
        EntityRegistrationAuthorityFilterStage stage = new EntityRegistrationAuthorityFilterStage();
        stage.setId("test");
        stage.setRequiringRegistrationInformation(true);
        stage.setWhitelistingRegistrationAuthorities(false);
        stage.setDesignatedRegistrationAuthorities(CollectionSupport.listOf("urn:example.org:authority1",
                "urn:example.org:authority2"));
        stage.initialize();

        final ArrayList<Item<Element>> mdCollection = new ArrayList<>();
        mdCollection.add(new DOMElementItem(readXMLData("in.xml")));
        Assert.assertEquals(mdCollection.size(), 1);

        stage.execute(mdCollection);
        stage.destroy();
        Assert.assertEquals(mdCollection.size(), 0);
    }

    /**
     * Tests removing EntitiesDescriptors that no longer contain any EntityDescriptors.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testRemoveEntitylessEntitiesDescriptor() throws Exception {
        EntityRegistrationAuthorityFilterStage stage = new EntityRegistrationAuthorityFilterStage();
        stage.setId("test");
        stage.setRequiringRegistrationInformation(true);
        stage.setRemovingEntitylessEntitiesDescriptor(true);
        stage.setWhitelistingRegistrationAuthorities(false);
        stage.setDesignatedRegistrationAuthorities(CollectionSupport.listOf("urn:example.org:authority1",
                "urn:example.org:authority2"));
        stage.initialize();

        final ArrayList<Item<Element>> mdCollection = new ArrayList<>();
        mdCollection.add(new DOMElementItem(readXMLData("in.xml")));
        Assert.assertEquals(mdCollection.size(), 1);

        stage.execute(mdCollection);
        stage.destroy();
        Assert.assertEquals(mdCollection.size(), 0);
    }

    /**
     * Tests not removing EntitiesDescriptors that no longer contain any EntityDescriptors.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testDontRemoveEntitylessEntitiesDescriptor() throws Exception {
        EntityRegistrationAuthorityFilterStage stage = new EntityRegistrationAuthorityFilterStage();
        stage.setId("test");
        stage.setRemovingEntitylessEntitiesDescriptor(false);
        stage.setRequiringRegistrationInformation(false);
        stage.setWhitelistingRegistrationAuthorities(false);
        stage.setDesignatedRegistrationAuthorities(CollectionSupport.listOf("urn:example.org:authority1",
                "urn:example.org:authority2"));
        stage.initialize();

        final ArrayList<Item<Element>> mdCollection = new ArrayList<>();
        mdCollection.add(new DOMElementItem(readXMLData("in.xml")));
        Assert.assertEquals(mdCollection.size(), 1);

        stage.execute(mdCollection);
        stage.destroy();
        Assert.assertEquals(mdCollection.size(), 1);
        Assert.assertEquals(ElementSupport.getChildElements(mdCollection.get(0).unwrap()).size(), 0);
    }

    /**
     * Build up a metadata collection containing three EntityDescriptors.
     * 
     * @return metadata collection
     * 
     * @throws Exception if something bad happens
     */
    private List<Item<Element>> buildMetadataCollection() throws Exception {
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
