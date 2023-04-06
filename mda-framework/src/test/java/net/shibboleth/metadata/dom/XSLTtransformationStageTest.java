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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.xml.XMLParserException;

import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** {@link XSLTransformationStage} unit test. */
public class XSLTtransformationStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public XSLTtransformationStageTest() {
        super(XSLTransformationStage.class);
    }

    /**
     * Utility method to grab our standard input file and turn it into a {@link DOMElementItem}.
     * 
     * @return the standard input file imported into a {@link DOMElementItem}.
     * 
     * @throws XMLParserException if there is a problem reading the input file
     */
    private DOMElementItem makeInput() throws XMLParserException {
        final Element testInput = readXMLData("input.xml");
        final DOMElementItem metadata = new DOMElementItem(testInput);
        // add a TestInfo so that we can check it is preserved by the stage.
        Assert.assertEquals(metadata.getItemMetadata().get(TestInfo.class).size(), 0);
        metadata.getItemMetadata().put(new TestInfo());
        Assert.assertEquals(metadata.getItemMetadata().get(TestInfo.class).size(), 1);
        return metadata;
    }

    /**
     * Test a transform which results in a single output element, which we can test against a known good output file.
     * 
     * @throws Exception if anything goes wrong.
     */
    @Test public void testTransform1() throws Exception {

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        final Resource transform = getClasspathResource("transform1.xsl");

        final XSLTransformationStage stage = new XSLTransformationStage();
        stage.setId("test");
        stage.setXSLResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 1);

        final Item<Element> result = mdCol.iterator().next();
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

        final Element expected = readXMLData("output.xml");
        assertXMLIdentical(expected, result.unwrap());
    }

    /**
     * Test a transform to which we supply a named parameter.
     * 
     * @throws Exception if something goes wrong
     */
    @Test public void testTransformParam() throws Exception {

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        final Resource transform = getClasspathResource("transform1.xsl");

        final Map<String, Object> params = new HashMap<>();
        params.put("fruit", "avocados");

        final XSLTransformationStage stage = new XSLTransformationStage();
        stage.setId("test");
        stage.setXSLResource(transform);
        stage.setTransformParameters(params);
        stage.initialize();

        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 1);

        final Item<Element> result = mdCol.iterator().next();
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

        final Element expected = readXMLData("paramOutput.xml");
        assertXMLIdentical(expected, result.unwrap());
    }

    /**
     * Test a transform which results in Status objects being attached to the output element.
     * 
     * @throws Exception if anything goes wrong
     */
    @Test public void testTransformListener() throws Exception {

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        final Resource transform = getClasspathResource("transformListener1.xsl");

        final XSLTransformationStage stage = new XSLTransformationStage();
        stage.setId("test");
        stage.setXSLResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 1);

        final Set<String> names = new HashSet<>();
        for (Item<Element> result : mdCol) {
            // each output item should have preserved the TestInfo that was on the input
            Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

            // collect the name of the output item's element
            names.add(result.unwrap().getNodeName());

            // verify the presence of the InfoStatus on the output
            List<InfoStatus> infos = result.getItemMetadata().get(InfoStatus.class);
            Assert.assertEquals(infos.size(), 2);
            Assert.assertEquals(infos.get(0).getStatusMessage(), "second value");
            Assert.assertEquals(infos.get(1).getStatusMessage(), "second value second message");

            // verify the presence of the WarningStatus on the output
            List<WarningStatus> warnings = result.getItemMetadata().get(WarningStatus.class);
            Assert.assertEquals(warnings.size(), 1);
            Assert.assertEquals(warnings.get(0).getStatusMessage(), "first value");

            // verify the presence of the WarningStatus on the output
            List<ErrorStatus> errors = result.getItemMetadata().get(ErrorStatus.class);
            Assert.assertEquals(errors.size(), 1);
            Assert.assertEquals(errors.get(0).getStatusMessage(), "error value");
        }

        Assert.assertFalse(names.contains("firstValue"));
        Assert.assertTrue(names.contains("secondValue"));
    }

    /**
     * Test a transform which results from templates contained in a main stylesheet and one which is included.
     * 
     * @throws Exception if anything goes wrong
     */
    @Test public void testInclude() throws Exception {

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        final Resource transform = getClasspathResource("includeMain.xsl");

        final XSLTransformationStage stage = new XSLTransformationStage();
        stage.setId("test");
        stage.setXSLResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 1);

        final Item<Element> result = mdCol.iterator().next();
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

        final Element expected = readXMLData("output.xml");
        assertXMLIdentical(expected, result.unwrap());
    }

    /**
     * Test a transform which manipulates the document's nodes that lie outside the document element.
     * 
     * @throws Exception if something goes wrong
     */
    @Test public void testOutsideDocumentElement() throws Exception {

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        final Resource transform = getClasspathResource("transform1.xsl");

        final XSLTransformationStage stage = new XSLTransformationStage();
        stage.setId("test");
        stage.setXSLResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 1);

        final Item<Element> result = mdCol.iterator().next();
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

        final Element expected = readXMLData("transform1.xml");
        // Compare *documents* here so that we include the prolog
        final var ownerDocument = expected.getOwnerDocument();
        assert ownerDocument != null;
        Document ownerDocument2 = result.unwrap().getOwnerDocument();
        assert ownerDocument2 != null;
        assertXMLIdentical(ownerDocument, ownerDocument2);

        // peek at the first node in the document; should be a comment
        final Node firstNode = ownerDocument2.getFirstChild();
        Assert.assertEquals(firstNode.getNodeType(), Node.COMMENT_NODE);
        Assert.assertEquals(firstNode.getNodeValue(), "this is a comment");
    }

    /** Simple marker object to test correct passage of {@link ItemMetadata} through pipeline stages. */
    @Immutable
    private static class TestInfo implements ItemMetadata {

    }

    @Test
    public void testURIResolver() throws Exception {
        
        // Local URIResolver
        class MyResolver implements URIResolver {

        	@Override public Source resolve(final String href, final String base) throws TransformerException {
                //System.out.println("href=" + href + ", base=" + base);
                
                // resolve just this one value of href, to cause a different file to be included
                // and the resulting output document to be changed
                if (href.equals("XSLTransformationStage-included.xsl")) {
                    final Resource resource = getClasspathResource("included2.xsl");
                    try {
                        return new StreamSource(resource.getInputStream());
                    } catch (IOException e) {
                        throw new TransformerException("couldn't fetch second included file", e);
                    }
                }
                
                // all other values of href cause the default processing to occur,
                // which will mean the original file will be included
                return null;
            }
            
        }

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        final Resource transform = getClasspathResource("includeMain.xsl");

        final XSLTransformationStage stage = new XSLTransformationStage();
        stage.setId("test");
        stage.setXSLResource(transform);
        stage.setURIResolver(new MyResolver());
        stage.initialize();

        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 1);

        final Item<Element> result = mdCol.iterator().next();
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

        final Element expected = readXMLData("output2.xml");
        assertXMLIdentical(expected, result.unwrap());
    }

    @Test
    public void mda219() throws Exception {
        final var resource = getClasspathResource("does-not-exist.txt");
        final var stage = new XSLTransformationStage();
        stage.setId("test");
        stage.setXSLResource(resource);
        try {
            stage.initialize();
            Assert.fail("expected exception");
        } catch (final ComponentInitializationException e) {
            // After MDA-219, we expect to see a cause which is an IOException.
            final var cause = e.getCause();
            // System.out.println("Message: " + e.getMessage());
            // System.out.println("Cause: " + cause);
            Assert.assertNotNull(cause, "exception had no cause");
            Assert.assertTrue(cause instanceof IOException, "cause should have been an IOException");
        }
    }

}
