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

import javax.xml.parsers.DocumentBuilder;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.saml.SAMLMetadataSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import net.shibboleth.utilities.java.support.xml.ParserPool;

public class WhitespaceTrimmingVisitorTest extends BaseDOMTest {
    
    /** Constructor sets class under test. */
    public WhitespaceTrimmingVisitorTest() {
        super(WhitespaceTrimmingVisitor.class);
    }
    
    private DOMElementItem makeItem() throws Exception {
        final ParserPool parserPool = getParserPool();
        final DocumentBuilder builder = parserPool.getBuilder();
        final Document document = builder.newDocument();
        final Element element = ElementSupport.constructElement(document,
                SAMLMetadataSupport.ENTITY_DESCRIPTOR_NAME);
        ElementSupport.setDocumentElement(document, element);
        return new DOMElementItem(element);
    }
    
    @Test
    public void visitAttr() throws Exception {
        final DOMElementItem item = makeItem();
        final Document doc = item.unwrap().getOwnerDocument();
        final Attr attr = doc.createAttribute("foo");
        attr.setTextContent("   trimmed   \n\n   \t   ");
        final AttrVisitor av = new WhitespaceTrimmingVisitor();
        av.visitAttr(attr, item);
        Assert.assertEquals(attr.getTextContent(), "trimmed");
    }

    @Test
    public void visitElement() throws Exception {
        final DOMElementItem item = makeItem();
        final Document doc = item.unwrap().getOwnerDocument();
        final Element e = doc.createElement("foo");
        e.setTextContent("   trimmed   \n\n   \t   ");
        final ElementVisitor ev = new WhitespaceTrimmingVisitor();
        ev.visitElement(e, item);
        Assert.assertEquals(e.getTextContent(), "trimmed");
    }

    @Test
    public void visitNode() throws Exception {
        final DOMElementItem item = makeItem();
        final Document doc = item.unwrap().getOwnerDocument();
        final Attr attr = doc.createAttribute("foo");
        attr.setTextContent("   trimmed   \n\n   \t   ");
        final NodeVisitor nv = new WhitespaceTrimmingVisitor();
        nv.visitNode(attr, item);
        Assert.assertEquals(attr.getTextContent(), "trimmed");
    }
}
