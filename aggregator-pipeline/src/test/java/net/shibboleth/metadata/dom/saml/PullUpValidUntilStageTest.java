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

import org.opensaml.util.xml.AttributeSupport;
import org.opensaml.util.xml.ElementSupport;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.BaseDomTest;
import net.shibboleth.metadata.dom.DomMetadata;

/**
 *
 */
public class PullUpValidUntilStageTest extends BaseDomTest {

    /** Test that the nearest validUntil is pulled up to the EntitiesDescriptor. */
    @Test
    public void testPullCacheDuration() throws Exception {
        ArrayList<DomMetadata> metadataCollection = new ArrayList<DomMetadata>();
        metadataCollection.add(new DomMetadata(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        PullUpValidUntilStage stage = new PullUpValidUntilStage();
        stage.setId("test");
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.get(0).getMetadata();
        Attr validUntilAttr = AttributeSupport
                .getAttribute(entitiesDescriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME);
        Assert.assertNotNull(validUntilAttr);

        long validUntil = AttributeSupport.getDateTimeAttributeAsLong(validUntilAttr);
        Assert.assertEquals(validUntil, 2429913600000L);

        List<Element> entityDescriptors = ElementSupport.getChildElements(entitiesDescriptor,
                MetadataHelper.ENTITY_DESCRIPTOR_NAME);
        Assert.assertEquals(entityDescriptors.size(), 3);

        for (Element entityDescriptor : entityDescriptors) {
            Assert.assertFalse(AttributeSupport.hasAttribute(entityDescriptor,
                    MetadataHelper.VALID_UNTIL_ATTIB_NAME));
        }
    }

    /** Test that the minimum validUntil is used when the nearest validUntil is earlier than min duration� + now. */
    @Test
    public void testMinCacheDuration() throws Exception {
        ArrayList<DomMetadata> metadataCollection = new ArrayList<DomMetadata>();
        metadataCollection.add(new DomMetadata(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        long hundredYears = 1000L * 60 * 60 * 24 * 365 * 100;
        
        PullUpValidUntilStage stage = new PullUpValidUntilStage();
        stage.setId("test");
        stage.setMinimumValidityDuration(hundredYears);
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.get(0).getMetadata();
        Attr validUntilAttr = AttributeSupport
                .getAttribute(entitiesDescriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME);
        Assert.assertNotNull(validUntilAttr);

        long validUntil = AttributeSupport.getDateTimeAttributeAsLong(validUntilAttr);
        Assert.assertTrue(validUntil > System.currentTimeMillis() + hundredYears - 1000*60);
    }

    /** Test that the maximum validUntil is used when the nearest validUntil is later than max duration + now. */
    @Test
    public void testMaxCacheDuration() throws Exception {
        ArrayList<DomMetadata> metadataCollection = new ArrayList<DomMetadata>();
        metadataCollection.add(new DomMetadata(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        long twoYears = 1000L * 60 * 60 * 24 * 365 * 2;
        
        PullUpValidUntilStage stage = new PullUpValidUntilStage();
        stage.setId("test");
        stage.setMaximumValidityDuration(twoYears);
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.get(0).getMetadata();
        Attr validUntilAttr = AttributeSupport
                .getAttribute(entitiesDescriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME);
        Assert.assertNotNull(validUntilAttr);

        long validUntil = AttributeSupport.getDateTimeAttributeAsLong(validUntilAttr);
        Assert.assertTrue(validUntil < System.currentTimeMillis() + twoYears);
    }
}