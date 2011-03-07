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

import net.shibboleth.metadata.SimpleMetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;

import org.opensaml.util.xml.AttributeSupport;
import org.opensaml.util.xml.BasicParserPool;
import org.opensaml.util.xml.ElementSupport;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Unit test for {@link SetCacheDurationStage}. */
public class SetCacheDurationStageTest {

    /**
     * Tests that the duration is properly set on an element when it doesn't already contain a duration.
     * 
     * @throws Exception thrown if there is an error
     */
    @Test
    public void testWithoutExistingCacheDuration() throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        Element entitiesDescriptor = parserPool.parse(
                SetCacheDurationStageTest.class.getResourceAsStream("/data/samlMetadata.xml")).getDocumentElement();

        Assert.assertTrue(AttributeSupport.getAttribute(entitiesDescriptor, MetadataHelper.CACHE_DURATION_ATTRIB_NAME) == null);

        SimpleMetadataCollection<DomMetadata> metadataCollection = new SimpleMetadataCollection<DomMetadata>();
        metadataCollection.add(new DomMetadata(entitiesDescriptor));

        long duration = 123456;
        SetCacheDurationStage stage = new SetCacheDurationStage();
        stage.setId("test");
        stage.setCacheDuration(duration);
        stage.initialize();

        stage.execute(metadataCollection);

        Attr cacheDurationAttr = AttributeSupport.getAttribute(entitiesDescriptor,
                MetadataHelper.CACHE_DURATION_ATTRIB_NAME);
        Assert.assertNotNull(cacheDurationAttr);
        Assert.assertEquals(cacheDurationAttr.getValue(), "P0Y0M0DT0H2M3.456S");
    }

    /**
     * Tests that the duration is properly set on an element when it already contains a duration.
     * 
     * @throws Exception thrown if there is an error
     */
    @Test
    public void testWithExistingCacheDuration() throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        Element entitiesDescriptor = parserPool.parse(
                SetCacheDurationStageTest.class.getResourceAsStream("/data/samlMetadata.xml")).getDocumentElement();
        AttributeSupport.appendDurationAttribute(entitiesDescriptor, MetadataHelper.CACHE_DURATION_ATTRIB_NAME, 987654);

        Assert.assertTrue(AttributeSupport.getAttribute(entitiesDescriptor, MetadataHelper.CACHE_DURATION_ATTRIB_NAME) != null);

        SimpleMetadataCollection<DomMetadata> metadataCollection = new SimpleMetadataCollection<DomMetadata>();
        metadataCollection.add(new DomMetadata(entitiesDescriptor));

        long duration = 123456;
        SetCacheDurationStage stage = new SetCacheDurationStage();
        stage.setId("test");
        stage.setCacheDuration(duration);
        stage.initialize();

        stage.execute(metadataCollection);

        Attr cacheDurationAttr = AttributeSupport.getAttribute(entitiesDescriptor,
                MetadataHelper.CACHE_DURATION_ATTRIB_NAME);
        Assert.assertNotNull(cacheDurationAttr);
        Assert.assertEquals(cacheDurationAttr.getValue(), "P0Y0M0DT0H2M3.456S");
    }

    /**
     * Tests that the stage ignores elements which are not EntityDescriptors or EntitiesDescriptors.
     * 
     * @throws Exception thrown if there is an error
     */
    @Test
    public void testWithNonDescriptorMetadataElement() throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        Document newDoc = parserPool.newDocument();
        Element root = newDoc.createElementNS("http://example.org", "foo");
        ElementSupport.setDocumentElement(newDoc, root);

        SimpleMetadataCollection<DomMetadata> metadataCollection = new SimpleMetadataCollection<DomMetadata>();
        metadataCollection.add(new DomMetadata(root));

        long duration = 123456;
        SetCacheDurationStage stage = new SetCacheDurationStage();
        stage.setId("test");
        stage.setCacheDuration(duration);
        stage.initialize();

        stage.execute(metadataCollection);

        Attr cacheDurationAttr = AttributeSupport.getAttribute(root, MetadataHelper.CACHE_DURATION_ATTRIB_NAME);
        Assert.assertNull(cacheDurationAttr);
    }

    /** Tests that the stage properly rejects negative durations. */
    @Test
    public void testNegativeDuration() {

        long duration = -987654;
        SetCacheDurationStage stage = new SetCacheDurationStage();
        stage.setId("test");

        try {
            stage.setCacheDuration(duration);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected this
        }
    }
}