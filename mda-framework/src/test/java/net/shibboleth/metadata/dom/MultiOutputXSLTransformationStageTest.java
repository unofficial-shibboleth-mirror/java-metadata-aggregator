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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.shared.xml.XMLParserException;

import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** {@link MultiOutputXSLTransformationStage} unit test. */
public class MultiOutputXSLTransformationStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public MultiOutputXSLTransformationStageTest() {
        super(MultiOutputXSLTransformationStage.class);
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

        final MultiOutputXSLTransformationStage stage = new MultiOutputXSLTransformationStage();
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
     * Test a transform which results in no output elements.
     * 
     * @throws Exception if anything goes wrong.
     */
    @Test public void testTransform0() throws Exception {

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        final Resource transform = getClasspathResource("transform0.xsl");

        final MultiOutputXSLTransformationStage stage = new MultiOutputXSLTransformationStage();
        stage.setId("test");
        stage.setXSLResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 0);
    }

    /**
     * Test a transform which results in two empty output elements, which we can test against by collecting their
     * element names. We also need to confirm that each output element retains the input {@link ItemMetadata}s.
     * 
     * @throws Exception if anything goes wrong.
     */
    @Test public void testTransform2() throws Exception {

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        final Resource transform = getClasspathResource("transform2.xsl");

        final MultiOutputXSLTransformationStage stage = new MultiOutputXSLTransformationStage();
        stage.setId("test");
        stage.setXSLResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 2);

        final Set<String> names = new HashSet<>();
        for (Item<Element> result : mdCol) {
            Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);
            names.add(result.unwrap().getNodeName());
        }

        Assert.assertTrue(names.contains("firstValue"));
        Assert.assertTrue(names.contains("secondValue"));
    }

    /**
     * Test a transform to which we supply a named parameter.
     * 
     * @throws Exception if anything goes wrong.
     */
    @Test public void testTransformParam() throws Exception {

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        final Resource transform = getClasspathResource("transform1.xsl");

        final Map<String, Object> params = new HashMap<>();
        params.put("fruit", "avocados");

        final MultiOutputXSLTransformationStage stage = new MultiOutputXSLTransformationStage();
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
     * Test a transform which results in Status objects being attached to each of two empty output elements.
     * 
     * @throws Exception if anything goes wrong.
     */
    @Test public void testTransformListener() throws Exception {

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        final Resource transform = getClasspathResource("transformListener.xsl");

        final MultiOutputXSLTransformationStage stage = new MultiOutputXSLTransformationStage();
        stage.setId("test");
        stage.setXSLResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 2);

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

        Assert.assertTrue(names.contains("firstValue"));
        Assert.assertTrue(names.contains("secondValue"));
    }

    /**
     * Test a transform which results from templates contained in a main stylesheet and one which is included.
     * 
     * @throws Exception if anything goes wrong.
     */
    @Test public void testInclude() throws Exception {

        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        Resource transform = getClasspathResource("includeMain.xsl");

        MultiOutputXSLTransformationStage stage = new MultiOutputXSLTransformationStage();
        stage.setId("test");
        stage.setXSLResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 1);

        final Item<Element> result = mdCol.iterator().next();
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

        Element expected = readXMLData("output.xml");
        assertXMLIdentical(expected, result.unwrap());
    }

    /** Simple marker object to test correct passage of {@link ItemMetadata} through pipeline stages. */
    @Immutable
    private static class TestInfo implements ItemMetadata {

    }

}
