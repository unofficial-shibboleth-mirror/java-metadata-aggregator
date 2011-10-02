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

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.xml.ElementSupport;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** Unit test for {@link EntityRegistrationAuthorityFilterStage}. */
public class EntityRegistrationAuthorityFilterStageTest extends BaseDomTest {

    /** Tests filtering out Items based on an authority whitelist. */
    @Test
    public void testAuthorityWhitelist() throws Exception {
        EntityRegistrationAuthorityFilterStage stage = new EntityRegistrationAuthorityFilterStage();
        stage.setId("test");
        stage.setRequiringRegistrationInformation(false);
        stage.setWhitelistingRegistrationAuthorities(true);
        stage.setDesignatedRegistrationAuthorities(CollectionSupport.toList("urn:example.org:authority2"));
        stage.initialize();

        Collection<DomElementItem> mdCollection = buildMetadataCollection();
        Assert.assertEquals(mdCollection.size(), 3);

        stage.execute(mdCollection);
        Assert.assertEquals(mdCollection.size(), 2);
    }

    /** Tests filtering out Items based on an authority blacklist. */
    @Test
    public void testAuthorityBlacklist() throws Exception {
        EntityRegistrationAuthorityFilterStage stage = new EntityRegistrationAuthorityFilterStage();
        stage.setId("test");
        stage.setWhitelistingRegistrationAuthorities(false);
        stage.setRequiringRegistrationInformation(false);
        stage.setDesignatedRegistrationAuthorities(CollectionSupport.toList("urn:example.org:authority2"));
        stage.initialize();

        Collection<DomElementItem> mdCollection = buildMetadataCollection();
        Assert.assertEquals(mdCollection.size(), 3);

        stage.execute(mdCollection);
        Assert.assertEquals(mdCollection.size(), 1);
    }

    /** Tests filtering out Items that do not contain registration information. */
    @Test
    public void testRequireRegistrationInfo() throws Exception {
        EntityRegistrationAuthorityFilterStage stage = new EntityRegistrationAuthorityFilterStage();
        stage.setId("test");
        stage.setRequiringRegistrationInformation(true);
        stage.setWhitelistingRegistrationAuthorities(false);
        stage.setDesignatedRegistrationAuthorities(CollectionSupport.toList("urn:example.org:authority1",
                "urn:example.org:authority2"));
        stage.initialize();

        ArrayList<DomElementItem> mdCollection = new ArrayList<DomElementItem>();
        mdCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));
        Assert.assertEquals(mdCollection.size(), 1);

        stage.execute(mdCollection);
        Assert.assertEquals(mdCollection.size(), 0);
    }

    /** Tests removing EntitiesDescriptors that no longer contain any EntityDescriptors. */
    @Test
    public void testRemoveEntitylessEntitiesDescriptor() throws Exception {
        EntityRegistrationAuthorityFilterStage stage = new EntityRegistrationAuthorityFilterStage();
        stage.setId("test");
        stage.setRequiringRegistrationInformation(true);
        stage.setRemovingEntitylessEntitiesDescriptor(true);
        stage.setWhitelistingRegistrationAuthorities(false);
        stage.setDesignatedRegistrationAuthorities(CollectionSupport.toList("urn:example.org:authority1",
                "urn:example.org:authority2"));
        stage.initialize();

        ArrayList<DomElementItem> mdCollection = new ArrayList<DomElementItem>();
        mdCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));
        Assert.assertEquals(mdCollection.size(), 1);

        stage.execute(mdCollection);
        Assert.assertEquals(mdCollection.size(), 0);
    }

    /** Tests not removing EntitiesDescriptors that no longer contain any EntityDescriptors. */
    @Test
    public void testDontRemoveEntitylessEntitiesDescriptor() throws Exception {
        EntityRegistrationAuthorityFilterStage stage = new EntityRegistrationAuthorityFilterStage();
        stage.setId("test");
        stage.setRemovingEntitylessEntitiesDescriptor(false);
        stage.setRequiringRegistrationInformation(false);
        stage.setWhitelistingRegistrationAuthorities(false);
        stage.setDesignatedRegistrationAuthorities(CollectionSupport.toList("urn:example.org:authority1",
                "urn:example.org:authority2"));
        stage.initialize();

        ArrayList<DomElementItem> mdCollection = new ArrayList<DomElementItem>();
        mdCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));
        Assert.assertEquals(mdCollection.size(), 1);

        stage.execute(mdCollection);
        Assert.assertEquals(mdCollection.size(), 1);
        Assert.assertEquals(ElementSupport.getChildElements(mdCollection.get(0).unwrap()).size(), 0);
    }

    /** Build up a metadata collection containing 3 EntityDescriptors. */
    private Collection<DomElementItem> buildMetadataCollection() throws Exception {
        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();

        List<Element> descriptors =
                ElementSupport.getChildElements(readXmlData("samlMetadata/entitiesDescriptor1.xml"));
        for (Element descriptor : descriptors) {
            metadataCollection.add(new DomElementItem(descriptor));
        }

        return metadataCollection;
    }
}