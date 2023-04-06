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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;
import net.shibboleth.shared.xml.AttributeSupport;
import net.shibboleth.shared.xml.ElementSupport;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/** Unit test for {@link PullUpCacheDurationStage}. */
public class PullUpCacheDurationStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public PullUpCacheDurationStageTest() {
        super(PullUpCacheDurationStage.class);
    }

    /**
     * Test that the shortest duration (1 hour) is pulled up to the EntitiesDescriptor.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testPullCacheDuration() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXMLData("in.xml")));

        PullUpCacheDurationStage stage = new PullUpCacheDurationStage();
        stage.setId("test");
        stage.initialize();
        
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.get(0).unwrap();
        Attr durationAttr = AttributeSupport
                .getAttribute(entitiesDescriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME);
        assert durationAttr != null;

        final var duration = AttributeSupport.getDurationAttributeValue(durationAttr);
        Assert.assertEquals(duration, Duration.ofHours(1));

        List<Element> entityDescriptors = ElementSupport.getChildElements(entitiesDescriptor,
                SAMLMetadataSupport.ENTITY_DESCRIPTOR_NAME);
        Assert.assertEquals(entityDescriptors.size(), 3);

        for (Element entityDescriptor : entityDescriptors) {
            assert entityDescriptor != null;
            Assert.assertFalse(AttributeSupport.hasAttribute(entityDescriptor,
                    SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME));
        }
    }

    /**
     * Test that the minimum cache duration is used when the shortest duration is less than it.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testMinCacheDuration() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXMLData("in.xml")));

        final var twoHours = Duration.ofHours(2);
        assert twoHours != null;

        PullUpCacheDurationStage stage = new PullUpCacheDurationStage();
        stage.setId("test");
        stage.setMinimumCacheDuration(twoHours);
        stage.initialize();
        
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.get(0).unwrap();
        Attr durationAttr = AttributeSupport
                .getAttribute(entitiesDescriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME);
        assert durationAttr != null;

        final var duration = AttributeSupport.getDurationAttributeValue(durationAttr);
        Assert.assertEquals(duration, Duration.ofHours(2));
    }

    /**
     * Test that the maximum cache duration is used when the shortest duration is greater than it.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testMaxCacheDuration() throws Exception {
        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(readXMLData("in.xml")));

        PullUpCacheDurationStage stage = new PullUpCacheDurationStage();
        stage.setId("test");
        stage.setMaximumCacheDuration(Duration.ofMinutes(30));
        stage.initialize();
        
        stage.execute(metadataCollection);

        Element entitiesDescriptor = metadataCollection.get(0).unwrap();
        Attr durationAttr = AttributeSupport
                .getAttribute(entitiesDescriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME);
        assert durationAttr != null;

        final var duration = AttributeSupport.getDurationAttributeValue(durationAttr);
        Assert.assertEquals(duration, Duration.ofMinutes(30));
    }
}
