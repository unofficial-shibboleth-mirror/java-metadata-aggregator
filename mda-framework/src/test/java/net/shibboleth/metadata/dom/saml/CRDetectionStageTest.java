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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;

public class CRDetectionStageTest extends BaseDOMTest {

    public CRDetectionStageTest() throws Exception {
        super(CRDetectionStage.class);
    }
    
    // Tests various assumptions about how character references are incorporated into
    // text and attribute nodes.
    @Test
    public void testDocumentAssumptions() throws Exception {
        final Item<Element> item = readDOMItem("assumptions.xml");
        final Element doc = item.unwrap();
        Assert.assertEquals(doc.getTagName(), "root");
        final Node node = doc.getFirstChild();
        // text&#13;text turns into a raw CR
        Assert.assertEquals(node.getNodeValue(), "text\rtext");
        final Element foo = (Element)node.getNextSibling();
        // same happens within an attribute value
        final String fop = foo.getAttribute("fop");
        Assert.assertEquals(fop, "x\ry");
    }

    // Tests the assumption that character references are NOT incorporated into
    // comment nodes.
    @Test
    public void testCommentAssumptions() throws Exception {
        // Make a parser pool which does NOT ignore comments
        // (Shibboleth stack ignores comments by default, removing all comment nodes)
        final BasicParserPool commentingParserPool = new BasicParserPool();
        commentingParserPool.setIgnoreComments(false);
        commentingParserPool.initialize();

        final InputStream input = getClasspathResource("comment.xml").getInputStream();
        Assert.assertNotNull(input);
        final Element doc = commentingParserPool.parse(input).getDocumentElement();
        Assert.assertEquals(doc.getTagName(), "root");
        final Node node1 = doc.getFirstChild();
        Assert.assertEquals(node1.getNodeType(), Node.TEXT_NODE);
        final Node node2 = node1.getNextSibling();
        Assert.assertEquals(node2.getNodeType(), Node.COMMENT_NODE);
        final Comment comment = (Comment)node2;
        Assert.assertEquals(comment.getData(), " a comment incorporating a &#13; ");
    }

    private List<ErrorStatus> execute(final Item<Element> item) throws Exception {
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        final CRDetectionStage stage = new CRDetectionStage();
        stage.setId("test");
        stage.initialize();
        stage.execute(items);
        final List<WarningStatus> warnings = item.getItemMetadata().get(WarningStatus.class);
        Assert.assertTrue(warnings.isEmpty());
        final List<InfoStatus> infos = item.getItemMetadata().get(InfoStatus.class);
        Assert.assertTrue(infos.isEmpty());
        return item.getItemMetadata().get(ErrorStatus.class);
    }

    private List<ErrorStatus> execute(final String filename) throws Exception {
        final Item<Element> item = readDOMItem(filename);
        return execute(item);
    }
    
    private ErrorStatus expectError(final String filename, final String errorContains) throws Exception {
        final List<ErrorStatus> errors = execute(filename);
        Assert.assertEquals(errors.size(), 1, "errors size on " + filename);
        final ErrorStatus error = errors.get(0);
        Assert.assertTrue(error.getStatusMessage().contains(errorContains),
                filename + " does not contain " + errorContains);
        return error;
    }

    private void expectErrorNoPrefix(final String filename, final String errorContains) throws Exception {
        var error = expectError(filename, errorContains);
        Assert.assertFalse(error.getStatusMessage().contains(": "));
    }
    
    @Test
    public void testErrors() throws Exception {
        expectErrorNoPrefix("element.xml", "element");
        expectErrorNoPrefix("attribute.xml", "attribute");
        expectErrorNoPrefix("assumptions.xml", "carriage return"); // contains both
        expectErrorNoPrefix("nested-element.xml", "element");
        expectErrorNoPrefix("nested-attribute.xml", "attribute");
        expectErrorNoPrefix("multiple.xml", "element");
    }

    @Test
    public void testOK() throws Exception {
        final List<ErrorStatus> errors = execute("ok.xml");
        Assert.assertTrue(errors.isEmpty());
    }
    
    @Test
    public void testEntityWithID() throws Exception {
        expectError("ID.xml", "uk000006: ");
    }
    
    @Test
    public void testEntityWithEntityID() throws Exception {
        expectError("entityID.xml", "https://idp2.iay.org.uk/idp/shibboleth: ");
    }
    
    @Test
    public void testEntityWithNoEntityIDorID() throws Exception {
        expectErrorNoPrefix("noID.xml", "element");
    }
}
