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
import java.util.List;

import net.shibboleth.metadata.AssertSupport;
import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.utilities.java.support.resource.ClasspathResource;
import net.shibboleth.utilities.java.support.resource.Resource;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class XSLValidationStageTest extends BaseDOMTest {

    @BeforeClass
    private void init() {
        setTestingClass(XSLValidationStage.class);
    }

    /**
     * Utility method to grab our standard input file and turn it into a {@link DOMElementItem}.
     * 
     * @throws XMLParserException
     */
    private DOMElementItem makeInput() throws XMLParserException {
        Element testInput = readXmlData("input.xml");
        DOMElementItem metadata = new DOMElementItem(testInput);
        // add a TestInfo so that we can check it is preserved by the stage.
        Assert.assertEquals(metadata.getItemMetadata().get(TestInfo.class).size(), 0);
        metadata.getItemMetadata().put(new TestInfo());
        Assert.assertEquals(metadata.getItemMetadata().get(TestInfo.class).size(), 1);
        return metadata;
    }

    /**
     * Test a validation transform.
     */
    @Test public void testValidation() throws Exception {

        final List<DOMElementItem> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        final Resource transform =
                new ClasspathResource(classRelativeResource("validator.xsl").substring(1));

        final XSLValidationStage stage = new XSLValidationStage();
        stage.setId("test");
        stage.setXSLResource(transform);
        stage.initialize();

        stage.execute(mdCol);

        // The input element should still be the only thing in the collection
        Assert.assertEquals(mdCol.size(), 1);
        final DOMElementItem result = mdCol.get(0);

        // The XML should be unchanged
        final Element expected = readXmlData("input.xml");
        assertXmlIdentical(expected, result.unwrap());

        // It should have been processed by the appropriate stage
        AssertSupport.assertValidComponentInfo(result, 1, XSLValidationStage.class, "test");

        // result item should have preserved the TestInfo that was on the input
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);

        // verify the presence of the InfoStatus on the output
        List<InfoStatus> infos = result.getItemMetadata().get(InfoStatus.class);
        Assert.assertEquals(infos.size(), 2);
        Assert.assertEquals(infos.get(0).getStatusMessage(), "second value");
        Assert.assertEquals(infos.get(1).getStatusMessage(), "second value second message");

        // verify the presence of the WarningStatus on the output
        List<WarningStatus> warnings = result.getItemMetadata().get(WarningStatus.class);
        Assert.assertEquals(warnings.size(), 1);
        Assert.assertEquals(warnings.get(0).getStatusMessage(), "first value");

        // verify the presence of the ErrorStatus on the output
        List<ErrorStatus> errors = result.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 1);
        Assert.assertEquals(errors.get(0).getStatusMessage(), "error value");
    }

    /**
     * Test for MDA-45.
     */
    @Test public void testMDA45() throws Exception {

        final List<DOMElementItem> mdCol = new ArrayList<>();
        mdCol.add(makeInput());

        final Resource transform =
                new ClasspathResource(classRelativeResource("mda45.xsl").substring(1));

        final XSLValidationStage stage = new XSLValidationStage();
        stage.setId("test");
        stage.setXSLResource(transform);
        stage.initialize();

        stage.execute(mdCol);

        // The input element should still be the only thing in the collection
        Assert.assertEquals(mdCol.size(), 1);
        final DOMElementItem result = mdCol.get(0);

        // verify the presence of the InfoStatus on the output
        List<InfoStatus> infos = result.getItemMetadata().get(InfoStatus.class);
        Assert.assertEquals(infos.size(), 1);
        Assert.assertEquals(infos.get(0).getStatusMessage(), "values");
    }
    
    /** Simple marker object to test correct passage of {@link ItemMetadata} through pipeline stages. */
    private static class TestInfo implements ItemMetadata {

        /** Serial version UIDs. */
        private static final long serialVersionUID = -4133926323393787487L;
    }
}