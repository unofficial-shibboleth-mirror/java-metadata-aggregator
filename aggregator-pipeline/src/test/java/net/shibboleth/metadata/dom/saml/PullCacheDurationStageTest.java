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

import org.opensaml.util.xml.AttributeSupport;
import org.opensaml.util.xml.ElementSupport;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/** Unit test for {@link PullUpCacheDurationStage}. */
public class PullCacheDurationStageTest extends BaseDomTest {

    /** Test that the shortest duration (1 hour) is pulled up to the EntitiesDescriptor. */
    @Test
    public void testPullCacheDuration() throws Exception {
        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        metadataCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        PullUpCacheDurationStage stage = new PullUpCacheDurationStage();
        stage.setId("test");
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.get(0).unwrap();
        Attr durationAttr = AttributeSupport
                .getAttribute(entitiesDescriptor, SamlMetadataSupport.CACHE_DURATION_ATTRIB_NAME);
        Assert.assertNotNull(durationAttr);

        long duration = AttributeSupport.getDurationAttributeValueAsLong(durationAttr);
        Assert.assertEquals(duration, 1000 * 60 * 60);

        List<Element> entityDescriptors = ElementSupport.getChildElements(entitiesDescriptor,
                SamlMetadataSupport.ENTITY_DESCRIPTOR_NAME);
        Assert.assertEquals(entityDescriptors.size(), 3);

        for (Element entityDescriptor : entityDescriptors) {
            Assert.assertFalse(AttributeSupport.hasAttribute(entityDescriptor,
                    SamlMetadataSupport.CACHE_DURATION_ATTRIB_NAME));
        }
    }

    /** Test that the minimum cache duration is used when the shortest duration is than it. */
    @Test
    public void testMinCacheDuration() throws Exception {
        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        metadataCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        PullUpCacheDurationStage stage = new PullUpCacheDurationStage();
        stage.setId("test");
        stage.setMinimumCacheDuration(1000 * 60 * 60 * 2);
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.get(0).unwrap();
        Attr durationAttr = AttributeSupport
                .getAttribute(entitiesDescriptor, SamlMetadataSupport.CACHE_DURATION_ATTRIB_NAME);
        Assert.assertNotNull(durationAttr);

        long duration = AttributeSupport.getDurationAttributeValueAsLong(durationAttr);
        Assert.assertEquals(duration, 1000 * 60 * 60 * 2);
    }

    /** Test that the maximum cache duration is used when the shortest duration is greater than it. */
    @Test
    public void testMaxCacheDuration() throws Exception {
        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        metadataCollection.add(new DomElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        PullUpCacheDurationStage stage = new PullUpCacheDurationStage();
        stage.setId("test");
        stage.setMaximumCacheDuration(1000 * 60 * 30);
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.get(0).unwrap();
        Attr durationAttr = AttributeSupport
                .getAttribute(entitiesDescriptor, SamlMetadataSupport.CACHE_DURATION_ATTRIB_NAME);
        Assert.assertNotNull(durationAttr);

        long duration = AttributeSupport.getDurationAttributeValueAsLong(durationAttr);
        Assert.assertEquals(duration, 1000 * 60 * 30);
    }
}