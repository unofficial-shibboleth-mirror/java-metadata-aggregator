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
import net.shibboleth.metadata.dom.DomMetadata;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.xml.ElementSupport;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class EntityRoleFilterStageTest extends BaseDomTest {

    @Test
    public void testRoleWhitelist() throws Exception {
        EntityRoleFilterStage stage = new EntityRoleFilterStage();
        stage.setId("test");
        stage.setDesignatedRoles(CollectionSupport.toList(EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME));
        stage.setWhitelistingRoles(true);

        List<DomMetadata> metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);

        Element descriptor = metadataCollection.get(0).getMetadata();
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME)
                .size(), 1);

        descriptor = metadataCollection.get(1).getMetadata();
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME)
                .size(), 0);

        descriptor = metadataCollection.get(2).getMetadata();
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME)
                .size(), 0);
    }

    @Test
    public void testRoleBlacklist() throws Exception {
        EntityRoleFilterStage stage = new EntityRoleFilterStage();
        stage.setId("test");
        stage.setDesignatedRoles(CollectionSupport.toList(EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME));
        stage.setWhitelistingRoles(false);

        List<DomMetadata> metadataCollection = buildMetadataCollection();
        stage.execute(metadataCollection);

        Element descriptor = metadataCollection.get(0).getMetadata();
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME)
                .size(), 0);

        descriptor = metadataCollection.get(1).getMetadata();
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME)
                .size(), 1);

        descriptor = metadataCollection.get(2).getMetadata();
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME)
                .size(), 1);
    }

    @Test
    public void testEntitiesDescriptorFiltering() throws Exception {
        ArrayList<DomMetadata> metadataCollection = new ArrayList<DomMetadata>();
        metadataCollection.add(new DomMetadata(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        EntityRoleFilterStage stage = new EntityRoleFilterStage();
        stage.setId("test");
        stage.setDesignatedRoles(CollectionSupport.toList(EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME));
        stage.setWhitelistingRoles(false);
        stage.execute(metadataCollection);

        List<Element> descriptors = ElementSupport.getChildElements(metadataCollection.iterator().next().getMetadata());
        Element descriptor = descriptors.get(0);
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.IDP_SSO_DESCRIPTOR_NAME)
                .size(), 0);

        descriptor = descriptors.get(1);
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME)
                .size(), 1);

        descriptor = descriptors.get(2);
        Assert.assertEquals(ElementSupport.getChildElements(descriptor, EntityRoleFilterStage.SP_SSO_DESCRIPTOR_NAME)
                .size(), 1);
    }

    private List<DomMetadata> buildMetadataCollection() throws Exception {
        ArrayList<DomMetadata> metadataCollection = new ArrayList<DomMetadata>();

        List<Element> descriptors = ElementSupport
                .getChildElements(readXmlData("samlMetadata/entitiesDescriptor1.xml"));
        for (Element descriptor : descriptors) {
            metadataCollection.add(new DomMetadata(descriptor));
        }

        return metadataCollection;
    }
}