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

package net.shibboleth.metadata.dom;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;

/** {@link ElementFormattingStage} unit test. */
public class ElementFormattingStageTest extends BaseDomTest {

    private Element unformattedElement;

    private Element singleLineElement;
    
    private Element prettyPrintElement;

    @BeforeClass public void setup() throws Exception {        
        URL sourceUrl =
                ElementFormattingStageTest.class
                        .getResource("/net/shibboleth/metadata/dom/ElementFormattingStage/unformatted.xml");
        unformattedElement = getParserPool().parse(sourceUrl.openStream()).getDocumentElement();

        sourceUrl =
                ElementFormattingStageTest.class
                        .getResource("/net/shibboleth/metadata/dom/ElementFormattingStage/singleLine.xml");
        singleLineElement = getParserPool().parse(sourceUrl.openStream()).getDocumentElement();
        
        sourceUrl =
                ElementFormattingStageTest.class
                        .getResource("/net/shibboleth/metadata/dom/ElementFormattingStage/prettyPrint.xml");
        prettyPrintElement = getParserPool().parse(sourceUrl.openStream()).getDocumentElement();
    }

    @Test public void testLineSeperator() throws ComponentInitializationException {
        ElementFormattingStage stage = new ElementFormattingStage();
        stage.setId("foo");
        Assert.assertEquals(stage.getLineSeperator(), "\n");

        stage.setLineSeperator("\r\n");
        Assert.assertEquals(stage.getLineSeperator(), "\r\n");

        stage.setLineSeperator("");
        Assert.assertEquals(stage.getLineSeperator(), "");

        stage.setLineSeperator(null);
        Assert.assertEquals(stage.getLineSeperator(), "");

        stage.initialize();
        try {
            stage.setLineSeperator("\n");
            Assert.fail();
        } catch (UnmodifiableComponentException e) {
            Assert.assertEquals(stage.getLineSeperator(), "");
        }

        stage = new ElementFormattingStage();
        stage.destroy();
        try {
            stage.setLineSeperator(null);
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // expected this
        }
    }

    @Test public void testIndented() throws ComponentInitializationException {
        ElementFormattingStage stage = new ElementFormattingStage();
        stage.setId("foo");
        Assert.assertTrue(stage.isIndented());

        stage.setIndented(false);
        Assert.assertFalse(stage.isIndented());

        stage.initialize();
        try {
            stage.setIndented(true);
            Assert.fail();
        } catch (UnmodifiableComponentException e) {
            Assert.assertFalse(stage.isIndented());
        }

        stage = new ElementFormattingStage();
        stage.destroy();
        try {
            stage.setIndented(true);
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // expected this
        }
    }

    @Test public void testIndentSize() throws ComponentInitializationException {
        ElementFormattingStage stage = new ElementFormattingStage();
        stage.setId("foo");
        Assert.assertEquals(stage.getIndentSize(), 4);

        stage.setIndentSize(10);
        Assert.assertEquals(stage.getIndentSize(), 10);

        try {
            stage.setIndentSize(-10);
            Assert.fail();
        } catch (AssertionError e) {
            Assert.assertEquals(stage.getIndentSize(), 10);
        }

        stage.initialize();
        try {
            stage.setIndentSize(2);
            Assert.fail();
        } catch (UnmodifiableComponentException e) {
            Assert.assertEquals(stage.getIndentSize(), 10);
        }

        stage = new ElementFormattingStage();
        stage.destroy();
        try {
            stage.setIndentSize(5);
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // expected this
        }
    }

    @Test public void testCdataSectionElements() throws ComponentInitializationException {
        ElementFormattingStage stage = new ElementFormattingStage();
        stage.setId("foo");
        Assert.assertNotNull(stage.getCdataSectionElements());
        Assert.assertTrue(stage.getCdataSectionElements().isEmpty());

        stage.setCdataSectionElements(Lists.newArrayList("foo", null, "", "bar"));
        Assert.assertEquals(stage.getCdataSectionElements().size(), 2);
        Assert.assertTrue(stage.getCdataSectionElements().contains("foo"));
        Assert.assertTrue(stage.getCdataSectionElements().contains("bar"));

        stage.setCdataSectionElements(Collections.EMPTY_LIST);
        Assert.assertTrue(stage.getCdataSectionElements().isEmpty());

        try {
            stage.getCdataSectionElements().add("foo");
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // expected this
        }

        stage.initialize();
        try {
            stage.setCdataSectionElements(null);
            Assert.fail();
        } catch (UnmodifiableComponentException e) {
            Assert.assertTrue(stage.getCdataSectionElements().isEmpty());
        }

        stage = new ElementFormattingStage();
        stage.destroy();
        try {
            stage.setCdataSectionElements(null);
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // expected this
        }
    }

    @Test public void testSingleLineFormatting() throws Exception {
        ElementFormattingStage stage = new ElementFormattingStage();
        stage.setId("foo");
        stage.setIndented(false);
        stage.setLineSeperator(null);
        stage.initialize();

        ArrayList<DomElementItem> itemCollection = Lists.newArrayList(new DomElementItem(unformattedElement));
        stage.execute(itemCollection);
        Assert.assertEquals(itemCollection.size(), 1);

        DomElementItem result = itemCollection.get(0);
        assertXmlIdentical(singleLineElement, result.unwrap());
        
        System.out.print(SerializeSupport.nodeToString(result.unwrap()));
    }
    
    @Test public void testPrettyPrintFormatting() throws Exception {
        ElementFormattingStage stage = new ElementFormattingStage();
        stage.setId("foo");
        stage.setIndented(true);
        stage.setLineSeperator("\n");
        stage.setIndentSize(4);
        stage.initialize();

        ArrayList<DomElementItem> itemCollection = Lists.newArrayList(new DomElementItem(unformattedElement));
        stage.execute(itemCollection);
        Assert.assertEquals(itemCollection.size(), 1);

        DomElementItem result = itemCollection.get(0);
        assertXmlIdentical(prettyPrintElement, result.unwrap());
        
        System.out.print(SerializeSupport.nodeToString(result.unwrap()));
    }
}