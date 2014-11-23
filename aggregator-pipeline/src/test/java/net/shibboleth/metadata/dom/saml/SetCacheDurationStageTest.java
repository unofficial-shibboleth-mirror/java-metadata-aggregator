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
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Unit test for {@link SetCacheDurationStage}. */
public class SetCacheDurationStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public SetCacheDurationStageTest() {
        super(SetCacheDurationStage.class);
    }

    /**
     * Helper method to extract the value of a descriptor's XML duration attribute in milliseconds.
     * 
     * @param descriptor EntitiesDescriptor or EntityDescriptor to pull the attribute from
     * @return the cache duration attribute value converted to milliseconds
     * @throws DatatypeConfigurationException if a {@link DatatypeFactory} can't be constructed
     */
    private long fetchDuration(Element descriptor) throws DatatypeConfigurationException {
        final Date baseDate = new Date(0);
        final DatatypeFactory dtf = DatatypeFactory.newInstance();
        final Attr cacheDurationAttr = AttributeSupport.getAttribute(descriptor,
                SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME);
        Assert.assertNotNull(cacheDurationAttr);
        return dtf.newDuration(cacheDurationAttr.getValue()).getTimeInMillis(baseDate);
    }
    
    /**
     * Tests that the duration is properly set on an element when it doesn't already contain a duration.
     * 
     * @throws Exception thrown if there is an error
     */
    @Test
    public void testWithoutExistingCacheDuration() throws Exception {
        final Element entitiesDescriptor = readXMLData("in.xml");
        final Item<Element> item = new DOMElementItem(entitiesDescriptor);

        Assert.assertTrue(AttributeSupport.getAttribute(entitiesDescriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME) == null);

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(item);

        long duration = 123456;
        SetCacheDurationStage stage = new SetCacheDurationStage();
        stage.setId("test");
        stage.setCacheDuration(duration);
        stage.initialize();

        stage.execute(metadataCollection);

        Assert.assertEquals(fetchDuration(item.unwrap()), duration);
    }

    /**
     * Tests that the duration is properly set on an element when it already contains a duration.
     * 
     * @throws Exception thrown if there is an error
     */
    @Test
    public void testWithExistingCacheDuration() throws Exception {
        final Element entitiesDescriptor = readXMLData("in.xml");
        final Item<Element> item = new DOMElementItem(entitiesDescriptor);
        
        final long originalDuration = 987654;
        AttributeSupport.appendDurationAttribute(entitiesDescriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME,
                originalDuration);

        Assert.assertTrue(AttributeSupport.getAttribute(entitiesDescriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME) != null);
        Assert.assertEquals(fetchDuration(entitiesDescriptor), originalDuration);

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(item);

        long duration = 123456;
        SetCacheDurationStage stage = new SetCacheDurationStage();
        stage.setId("test");
        stage.setCacheDuration(duration);
        stage.initialize();

        stage.execute(metadataCollection);

        Assert.assertEquals(fetchDuration(item.unwrap()), duration);
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

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(root));

        long duration = 123456;
        SetCacheDurationStage stage = new SetCacheDurationStage();
        stage.setId("test");
        stage.setCacheDuration(duration);
        stage.initialize();

        stage.execute(metadataCollection);

        Attr cacheDurationAttr = AttributeSupport.getAttribute(root, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME);
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
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }
}