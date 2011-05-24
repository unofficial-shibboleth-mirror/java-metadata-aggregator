/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Map;
import java.util.Set;

import net.shibboleth.metadata.AssertSupport;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.dom.DomElementItem;

import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.xml.XMLParserException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** {@link XSLTransformationStage} unit test. */
public class XSLTransformationStageTest extends BaseDomTest {

    /** Simple marker object to test correct passage of {@link ItemMetadata} through pipeline stages. */
    private static class TestInfo implements ItemMetadata {
        /** All {@link MetdataInfo} subclasses must declare version UIDs. */
        private static final long serialVersionUID = -4133926323393787487L;
    }

    /**
     * Utility method to grab our standard input file and turn it into a {@link DomElementItem}.
     * 
     * @throws XMLParserException
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
     */
    @Test
    public void testTransform1() throws Exception {
        
        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(makeInput());

        Resource transform = new ClasspathResource("data/xsltStageTransform1.xsl");

        XSLTransformationStage stage = new XSLTransformationStage();
        stage.setId("test");
        stage.setXslResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DomElementItem result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, XSLTransformationStage.class, "test");
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

        Element expected = readXmlData("xsltStageOutput.xml");
        assertXmlEqual(expected, result.unwrap());
    }
    
    /**
     * Test a transform which results in a no output elements.
     */
    @Test
    public void testTransform0() throws Exception {
        
        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(makeInput());

        Resource transform = new ClasspathResource("data/xsltStageTransform0.xsl");

        XSLTransformationStage stage = new XSLTransformationStage();
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
     */
    @Test
    public void testTransform2() throws Exception {
        
        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(makeInput());

        Resource transform = new ClasspathResource("data/xsltStageTransform2.xsl");

        XSLTransformationStage stage = new XSLTransformationStage();
        stage.setId("test");
        stage.setXslResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 2);

        Set<String> names = new HashSet<String>();
        for (DomElementItem result: mdCol) {
            AssertSupport.assertValidComponentInfo(result, 1, XSLTransformationStage.class, "test");
            Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);
            names.add(result.unwrap().getNodeName());
        }
        
        Assert.assertTrue(names.contains("firstValue"));
        Assert.assertTrue(names.contains("secondValue"));
    }
    
    /**
     * Test a transform to which we supply a named parameter.
     */
    @Test
    public void testTransformParam() throws Exception {
        
        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(makeInput());

        Resource transform = new ClasspathResource("data/xsltStageTransform1.xsl");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fruit", "avocados");
        
        XSLTransformationStage stage = new XSLTransformationStage();
        stage.setId("test");
        stage.setXslResource(transform);
        stage.setTransformParameters(params);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DomElementItem result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, XSLTransformationStage.class, "test");
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

        Element expected = readXmlData("xsltStageParamOutput.xml");
        assertXmlEqual(expected, result.unwrap());
    }
    

    
}