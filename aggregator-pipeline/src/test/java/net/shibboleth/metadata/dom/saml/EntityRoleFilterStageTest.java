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

import net.shibboleth.metadata.dom.BaseDomTest;
import net.shibboleth.metadata.dom.DomElementItem;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.xml.ElementSupport;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** Unit test for {@link EntityRoleFilterStage}. */
public class EntityRoleFilterStageTest extends BaseDomTest {

    /**
     * Test that whitelisted roles are retained and all other roles are removed. Also tests that roleless entities are
     * removed.
     */
    @Test
    public void testRoleWhitelist() throws Exception {
        EntityRoleFilterStage stage = new EntityRoleFilterStage();
        stage.setId("test");
        stage.setDesignatedRoles(CollectionSupport.toList(EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME));
        stage.setWhitelistingRoles(true);

        List<DomElementItem> metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 1);

        Element descriptor = metadataCollection.get(0).unwrap();
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME)
                .size(), 1);
    }

    /**
     * Test that blacklisted roles are removed and all other roles are retained. Also tests that roleless entities are
     * removed.
     */
    @Test
    public void testRoleBlacklist() throws Exception {
        EntityRoleFilterStage stage = new EntityRoleFilterStage();
        stage.setId("test");
        stage.setDesignatedRoles(CollectionSupport.toList(EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME));
        stage.setWhitelistingRoles(false);

        List<DomElementItem> metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 2);

        Element descriptor = metadataCollection.get(0).unwrap();
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME)
                .size(), 1);

        descriptor = metadataCollection.get(1).unwrap();
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME)
                .size(), 1);
    }

    /**
     * Test that EntityDescriptors that have had all their roles removed are not themselves removed if
     * {@link EntityRoleFilterStage#isRemovingRolelessEntities()} is false.
     */
    @Test
    public void testDontRemoveRolelessEntityDescriptor() throws Exception {
        EntityRoleFilterStage stage = new EntityRoleFilterStage();
        stage.setId("test");
        stage.setDesignatedRoles(CollectionSupport.toList(EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME));
        stage.setWhitelistingRoles(true);
        stage.setRemoveRolelessEntities(false);

        List<DomElementItem> metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 3);

        Element descriptor = metadataCollection.get(0).unwrap();
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME)
                .size(), 1);

        descriptor = metadataCollection.get(1).unwrap();
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME)
                .size(), 0);

        descriptor = metadataCollection.get(2).unwrap();
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME)
                .size(), 0);
    }
    
    /**
     * Test that role filtering is performed on descendant elements of metadata collection elements.
     */
    @Test
    public void testEntitiesDescriptorFiltering() throws Exception {
        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        metadataCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        EntityRoleFilterStage stage = new EntityRoleFilterStage();
        stage.setId("test");
        stage.setDesignatedRoles(CollectionSupport.toList(EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME));
        stage.setWhitelistingRoles(false);
        stage.execute(metadataCollection);

        List<Element> descriptors = ElementSupport.getChildElements(metadataCollection.iterator().next().unwrap());
        Assert.assertEquals(descriptors.size(), 2);

        Element descriptor = descriptors.get(0);
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME)
                .size(), 1);

        descriptor = descriptors.get(1);
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME)
                .size(), 1);
    }

    /**
     * Test that EntitiesDescriptors that have had all their EntityDescriptor children remove are themselves removed.
     */
    @Test
    public void testRemoveEntitylessEntitiesDescriptor() throws Exception {
        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        metadataCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        EntityRoleFilterStage stage = new EntityRoleFilterStage();
        stage.setId("test");
        stage.setDesignatedRoles(CollectionSupport.toList(EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME));
        stage.setWhitelistingRoles(false);
        stage.execute(metadataCollection);
        
        Assert.assertEquals(metadataCollection.size(), 0);
    }

    /**
     * Test that EntitiesDescriptors that have had all their EntityDescriptor children remove are not themselves removed
     * when {@link EntityRoleFilterStage#isRemovingEntitylessEntitiesDescriptor()} is false.
     */
    @Test
    public void testDontRemoveEntitylessEntitiesDescriptor() throws Exception {
        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        metadataCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        EntityRoleFilterStage stage = new EntityRoleFilterStage();
        stage.setId("test");
        stage.setDesignatedRoles(CollectionSupport.toList(EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME));
        stage.setWhitelistingRoles(false);
        stage.setRemovingEntitylessEntitiesDescriptor(false);
        stage.execute(metadataCollection);
        
        Assert.assertEquals(metadataCollection.size(), 1);
    }

    /** Build up a metadata collection containing 3 EntityDescriptors. */
    private List<DomElementItem> buildMetadataCollection() throws Exception {
        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();

        List<Element> descriptors = ElementSupport
                .getChildElements(readXmlData("samlMetadata/entitiesDescriptor1.xml"));
        for (Element descriptor : descriptors) {
            metadataCollection.add(new DomElementItem(descriptor));
        }

        return metadataCollection;
    }
}