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
import java.util.Date;

import javax.annotation.Nonnull;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.shared.logic.ConstraintViolationException;
import net.shibboleth.shared.xml.AttributeSupport;
import net.shibboleth.shared.xml.impl.BasicParserPool;
import net.shibboleth.shared.xml.ElementSupport;

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
     * Helper method to extract the value of a descriptor's XML duration attribute.
     * 
     * @param descriptor EntitiesDescriptor or EntityDescriptor to pull the attribute from
     * @return the cache duration attribute value converted to a {@link Duration}
     * @throws DatatypeConfigurationException if a {@link DatatypeFactory} can't be constructed
     */
    private Duration fetchDuration(@Nonnull Element descriptor) throws DatatypeConfigurationException {
        final Date baseDate = new Date(0);
        final DatatypeFactory dtf = DatatypeFactory.newInstance();
        final Attr cacheDurationAttr = AttributeSupport.getAttribute(descriptor,
                SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME);
        assert cacheDurationAttr != null;
        return Duration.ofMillis(dtf.newDuration(cacheDurationAttr.getValue()).getTimeInMillis(baseDate));
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

        final var duration = Duration.ofMillis(123456);
        assert duration != null;
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
        
        final var originalDuration = Duration.ofMillis(987654);
        assert originalDuration != null;
        AttributeSupport.appendDurationAttribute(entitiesDescriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME,
                originalDuration);

        Assert.assertTrue(AttributeSupport.getAttribute(entitiesDescriptor, SAMLMetadataSupport.CACHE_DURATION_ATTRIB_NAME) != null);
        Assert.assertEquals(fetchDuration(entitiesDescriptor), originalDuration);

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(item);

        final var duration = Duration.ofMillis(123456);
        assert duration != null;
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
        assert root != null;
        ElementSupport.setDocumentElement(newDoc, root);

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(root));

        final var duration = Duration.ofMillis(123456);
        assert duration != null;
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
        SetCacheDurationStage stage = new SetCacheDurationStage();
        stage.setId("test");

        try {
            final var duration = Duration.ofMillis(-987654);
            assert duration != null;
            stage.setCacheDuration(duration);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

    /** Tests that the stage properly rejects zero durations. */
    @Test
    public void testZeroDuration() {
        final var stage = new SetCacheDurationStage();
        stage.setId("test");

        try {
            final var duration = Duration.ZERO;
            assert duration != null;
            stage.setCacheDuration(duration);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

    /** Tests that the stage properly rejects null durations. */
    @SuppressWarnings("null")
    @Test
    public void testNullDuration() {
        final var stage = new SetCacheDurationStage();
        stage.setId("test");

        try {
            stage.setCacheDuration(null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

}
