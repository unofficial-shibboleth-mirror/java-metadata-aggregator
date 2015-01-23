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

/** {@link SetValidUntilStage} unit test. */
public class SetValidUntilStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public SetValidUntilStageTest() {
        super(SetValidUntilStage.class);
    }

    /**
     * Tests that the validUntil attribute is properly set on an element when it doesn't already contain one.
     * 
     * @throws Exception thrown if there is an error
     */
    @Test
    public void testWithoutExistingValidUntil() throws Exception {
        final Element entitiesDescriptor = readXMLData("in.xml");

        entitiesDescriptor.removeAttributeNS(null, SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME.getLocalPart());

        Assert.assertTrue(AttributeSupport.getAttribute(entitiesDescriptor, SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME) == null);

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(entitiesDescriptor));

        long duration = 123456;
        long now = System.currentTimeMillis();
        SetValidUntilStage stage = new SetValidUntilStage();
        stage.setId("test");
        stage.setValidityDuration(duration);
        stage.initialize();

        stage.execute(metadataCollection);

        Attr validUntilAttr = AttributeSupport.getAttribute(metadataCollection.iterator().next().unwrap(), SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME);
        Assert.assertNotNull(validUntilAttr);

        long validUntil = AttributeSupport.getDateTimeAttributeAsLong(validUntilAttr);
        Assert.assertTrue(validUntil > now + duration - 100);
        Assert.assertTrue(validUntil < now + duration + 100);
    }

    /**
     * Tests that the duration is properly set on an element when it already contains a duration.
     * 
     * @throws Exception thrown if there is an error
     */
    @Test
    public void testWithExistingValidUntil() throws Exception {
        final Element entitiesDescriptor = readXMLData("in.xml");

        Assert.assertTrue(AttributeSupport.hasAttribute(entitiesDescriptor, SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME));

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(entitiesDescriptor));

        long duration = 123456;
        long now = System.currentTimeMillis();
        SetValidUntilStage stage = new SetValidUntilStage();
        stage.setId("test");
        stage.setValidityDuration(duration);
        stage.initialize();

        stage.execute(metadataCollection);

        Attr validUntilAttr = AttributeSupport.getAttribute(metadataCollection.iterator().next().unwrap(), SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME);
        Assert.assertNotNull(validUntilAttr);

        long validUntil = AttributeSupport.getDateTimeAttributeAsLong(validUntilAttr);
        Assert.assertTrue(validUntil > (now + duration - 100));
        Assert.assertTrue(validUntil < (now + duration + 100));
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
        SetValidUntilStage stage = new SetValidUntilStage();
        stage.setId("test");
        stage.setValidityDuration(duration);
        stage.initialize();

        stage.execute(metadataCollection);

        Attr validUntilAttr = AttributeSupport.getAttribute(root, SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME);
        Assert.assertNull(validUntilAttr);
    }

    /** Tests that the stage properly rejects negative durations. */
    @Test
    public void testNegativeDuration() {

        long duration = -987654;
        SetValidUntilStage stage = new SetValidUntilStage();
        stage.setId("test");

        try {
            stage.setValidityDuration(duration);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }
}