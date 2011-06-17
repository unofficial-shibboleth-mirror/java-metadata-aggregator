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

import net.shibboleth.metadata.dom.DomElementItem;

import org.opensaml.util.xml.AttributeSupport;
import org.opensaml.util.xml.BasicParserPool;
import org.opensaml.util.xml.ElementSupport;
import org.opensaml.util.xml.SerializeSupport;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** {@link SetValidUntilStage} unit test. */
public class SetValidUntilStageTest {

    /**
     * Tests that the validUntil attribute is properly set on an element when it doesn't already contain one.
     * 
     * @throws Exception thrown if there is an error
     */
    @Test
    public void testWithoutExistingValidUntil() throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        Element entitiesDescriptor = parserPool.parse(
                SetValidUntilStageTest.class.getResourceAsStream("/data/samlMetadata.xml")).getDocumentElement();
        entitiesDescriptor.removeAttributeNS(null, MetadataHelper.VALID_UNTIL_ATTIB_NAME.getLocalPart());

        Assert.assertTrue(AttributeSupport.getAttribute(entitiesDescriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME) == null);

        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        metadataCollection.add(new DomElementItem(entitiesDescriptor));

        long duration = 123456;
        long now = System.currentTimeMillis();
        SetValidUntilStage stage = new SetValidUntilStage();
        stage.setId("test");
        stage.setValidityDuration(duration);
        stage.initialize();

        stage.execute(metadataCollection);

        Attr validUntilAttr = AttributeSupport.getAttribute(entitiesDescriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME);
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
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        Element entitiesDescriptor = parserPool.parse(
                SetValidUntilStageTest.class.getResourceAsStream("/data/samlMetadata.xml")).getDocumentElement();

        Assert.assertTrue(AttributeSupport.hasAttribute(entitiesDescriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME));

        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        metadataCollection.add(new DomElementItem(entitiesDescriptor));

        long duration = 123456;
        long now = System.currentTimeMillis();
        SetValidUntilStage stage = new SetValidUntilStage();
        stage.setId("test");
        stage.setValidityDuration(duration);
        stage.initialize();

        stage.execute(metadataCollection);

        System.out.println(SerializeSupport.prettyPrintXML(entitiesDescriptor));

        Attr validUntilAttr = AttributeSupport.getAttribute(entitiesDescriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME);
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

        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        metadataCollection.add(new DomElementItem(root));

        long duration = 123456;
        SetValidUntilStage stage = new SetValidUntilStage();
        stage.setId("test");
        stage.setValidityDuration(duration);
        stage.initialize();

        stage.execute(metadataCollection);

        Attr validUntilAttr = AttributeSupport.getAttribute(root, MetadataHelper.VALID_UNTIL_ATTIB_NAME);
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
        } catch (IllegalArgumentException e) {
            // expected this
        }
    }
}