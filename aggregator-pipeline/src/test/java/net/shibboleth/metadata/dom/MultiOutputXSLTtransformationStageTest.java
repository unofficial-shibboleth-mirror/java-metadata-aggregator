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

import net.shibboleth.metadata.AssertSupport;
import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.WarningStatus;

import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.xml.XMLParserException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** {@link MultiOutputXSLTransformationStage} unit test. */
public class MultiOutputXSLTtransformationStageTest extends BaseDomTest {

    /** Simple marker object to test correct passage of {@link ItemMetadata} through pipeline stages. */
    private static class TestInfo implements ItemMetadata {
        /** All {@link MetdataInfo} subclasses must declare version UIDs. */
        private static final long serialVersionUID = -4133926323393787487L;
    }

    /**
     * Utility method to grab our standard input file and turn it into a {@link DomElementItem}.
     * 
     * @return the standard input file imported into a {@link DomElementItem}.
     * 
     * @throws XMLParserException if there is a problem reading the input file
     */
    private DomElementItem makeInput() throws XMLParserException  {
        Element testInput = readXmlData("xsltStageInput.xml");
        DomElementItem metadata = new DomElementItem(testInput);
        // add a TestInfo so that we can check it is preserved by the stage.
        Assert.assertEquals(metadata.getItemMetadata().get(TestInfo.class).size(), 0);
        metadata.getItemMetadata().put(new TestInfo());
        Assert.assertEquals(metadata.getItemMetadata().get(TestInfo.class).size(), 1);
        return metadata;
    }

    /**
     * Test a transform which results in a single output element, which we
     * can test against a known good output file.
     * 
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void testTransform1() throws Exception {
        
        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(makeInput());

        Resource transform = new ClasspathResource("data/xsltStageTransform1.xsl");

        MultiOutputXSLTransformationStage stage = new MultiOutputXSLTransformationStage();
        stage.setId("test");
        stage.setXslResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DomElementItem result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, MultiOutputXSLTransformationStage.class, "test");
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

        Element expected = readXmlData("xsltStageOutput.xml");
        assertXmlEqual(expected, result.unwrap());
    }
    
    /**
     * Test a transform which results in no output elements.
     * 
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void testTransform0() throws Exception {
        
        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(makeInput());

        Resource transform = new ClasspathResource("data/xsltStageTransform0.xsl");

        MultiOutputXSLTransformationStage stage = new MultiOutputXSLTransformationStage();
        stage.setId("test");
        stage.setXslResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 0);
    }
    
    /**
     * Test a transform which results in two empty output elements, which we
     * can test against by collecting their element names.  We also need to
     * confirm that each output element retains the input {@link ItemMetadata}s.
     * 
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void testTransform2() throws Exception {
        
        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(makeInput());

        Resource transform = new ClasspathResource("data/xsltStageTransform2.xsl");

        MultiOutputXSLTransformationStage stage = new MultiOutputXSLTransformationStage();
        stage.setId("test");
        stage.setXslResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 2);

        Set<String> names = new HashSet<String>();
        for (DomElementItem result: mdCol) {
            AssertSupport.assertValidComponentInfo(result, 1, MultiOutputXSLTransformationStage.class, "test");
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
    @Test
    public void testTransformParam() throws Exception {
        
        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(makeInput());

        Resource transform = new ClasspathResource("data/xsltStageTransform1.xsl");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fruit", "avocados");
        
        MultiOutputXSLTransformationStage stage = new MultiOutputXSLTransformationStage();
        stage.setId("test");
        stage.setXslResource(transform);
        stage.setTransformParameters(params);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DomElementItem result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, MultiOutputXSLTransformationStage.class, "test");
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

        Element expected = readXmlData("xsltStageParamOutput.xml");
        assertXmlEqual(expected, result.unwrap());
    }
    

    /**
     * Test a transform which results in Status objects being attached to each of two empty
     * output elements.
     * 
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void testTransformListener() throws Exception {
        
        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(makeInput());

        Resource transform = new ClasspathResource("data/xsltStageTransformListener.xsl");

        MultiOutputXSLTransformationStage stage = new MultiOutputXSLTransformationStage();
        stage.setId("test");
        stage.setXslResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 2);

        Set<String> names = new HashSet<String>();
        for (DomElementItem result: mdCol) {
            AssertSupport.assertValidComponentInfo(result, 1, MultiOutputXSLTransformationStage.class, "test");
            
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
     * Test a transform which results from templates contained in a main
     * stylesheet and one which is included.
     * 
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void testInclude() throws Exception {
        
        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(makeInput());

        Resource transform = new ClasspathResource("data/xslIncludeMain.xsl");

        MultiOutputXSLTransformationStage stage = new MultiOutputXSLTransformationStage();
        stage.setId("test");
        stage.setXslResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DomElementItem result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, MultiOutputXSLTransformationStage.class, "test");
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

        Element expected = readXmlData("xsltStageOutput.xml");
        assertXmlEqual(expected, result.unwrap());
    }
    
}