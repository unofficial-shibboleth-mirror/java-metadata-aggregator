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
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** {@link XPathFilteringStage} unit test. */
public class XPathFilteringStageTest extends BaseDomTest {

    /**
     * Test XPathFilteringStage using an example from the UK federation build process.
     * 
     * @throws Exception if something goes wrong
     */
    @Test
    public void testExecute() throws Exception {
        // Construct a map containing required namespace prefix definitions
        Map<String, String> prefixMappings = new HashMap<String, String>();
        prefixMappings.put("ukfedlabel", "http://ukfederation.org.uk/2006/11/label");

        // Construct the strategy object
        XPathFilteringStage strategy = new XPathFilteringStage();
        strategy.setXpathExpression("//ukfedlabel:DeletedEntity");
        strategy.setNamespaceContext(new SimpleNamespaceContext(prefixMappings));

        // Construct the input metadata
        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        metadataCollection.add(new DomElementItem(readXmlData("xpathInput1.xml")));
        metadataCollection.add(new DomElementItem(readXmlData("xpathInput2.xml")));
        metadataCollection.add(new DomElementItem(readXmlData("xpathInput3.xml")));
        Assert.assertEquals(metadataCollection.size(), 3);

        // Filter the metadata collection
        strategy.doExecute(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 1);
        Element element = metadataCollection.get(0).unwrap();
        String id = element.getAttribute("id");
        Assert.assertEquals(id, "entity2");
    }

}