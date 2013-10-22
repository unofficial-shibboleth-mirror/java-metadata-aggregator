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

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/** Unit test for {@link PullUpValidUntilStage}. */
public class PullUpValidUntilStageTest extends BaseDOMTest {

    /** Test that the nearest validUntil is pulled up to the EntitiesDescriptor. */
    @Test
    public void testPullCacheDuration() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        PullUpValidUntilStage stage = new PullUpValidUntilStage();
        stage.setId("test");
        stage.initialize();
        
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.get(0).unwrap();
        Attr validUntilAttr = AttributeSupport
                .getAttribute(entitiesDescriptor, SAMLMetadataSupport.VALID_UNTIL_ATTIB_NAME);
        Assert.assertNotNull(validUntilAttr);

        long validUntil = AttributeSupport.getDateTimeAttributeAsLong(validUntilAttr);
        Assert.assertEquals(validUntil, 2429913600000L);

        List<Element> entityDescriptors = ElementSupport.getChildElements(entitiesDescriptor,
                SAMLMetadataSupport.ENTITY_DESCRIPTOR_NAME);
        Assert.assertEquals(entityDescriptors.size(), 3);

        for (Element entityDescriptor : entityDescriptors) {
            Assert.assertFalse(AttributeSupport.hasAttribute(entityDescriptor,
                    SAMLMetadataSupport.VALID_UNTIL_ATTIB_NAME));
        }
    }

    /** Test that the minimum validUntil is used when the nearest validUntil is earlier than min duration + now. */
    @Test
    public void testMinCacheDuration() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        long hundredYears = 1000L * 60 * 60 * 24 * 365 * 100;
        
        PullUpValidUntilStage stage = new PullUpValidUntilStage();
        stage.setId("test");
        stage.setMinimumValidityDuration(hundredYears);
        stage.initialize();
        
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.get(0).unwrap();
        Attr validUntilAttr = AttributeSupport
                .getAttribute(entitiesDescriptor, SAMLMetadataSupport.VALID_UNTIL_ATTIB_NAME);
        Assert.assertNotNull(validUntilAttr);

        long validUntil = AttributeSupport.getDateTimeAttributeAsLong(validUntilAttr);
        Assert.assertTrue(validUntil > System.currentTimeMillis() + hundredYears - 1000*60);
    }

    /** Test that the maximum validUntil is used when the nearest validUntil is later than max duration + now. */
    @Test
    public void testMaxCacheDuration() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXmlData("samlMetadata/entitiesDescriptor1.xml")));

        long twoYears = 1000L * 60 * 60 * 24 * 365 * 2;
        long twoYearsFromNow = twoYears + System.currentTimeMillis();
        
        PullUpValidUntilStage stage = new PullUpValidUntilStage();
        stage.setId("test");
        stage.setMaximumValidityDuration(twoYears);
        stage.initialize();
        
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.get(0).unwrap();
        Attr validUntilAttr = AttributeSupport
                .getAttribute(entitiesDescriptor, SAMLMetadataSupport.VALID_UNTIL_ATTIB_NAME);
        Assert.assertNotNull(validUntilAttr);

        long validUntil = AttributeSupport.getDateTimeAttributeAsLong(validUntilAttr);

        long delta = 1000 * 10; // ten seconds permitted either side to allow for test execution time
        Assert.assertTrue(validUntil < twoYearsFromNow + delta);
        Assert.assertTrue(validUntil > twoYearsFromNow - delta);
    }
}