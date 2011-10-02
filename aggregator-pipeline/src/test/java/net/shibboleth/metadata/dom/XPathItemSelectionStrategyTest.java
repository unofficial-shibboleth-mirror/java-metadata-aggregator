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

import java.util.HashMap;
import java.util.Map;

import net.shibboleth.metadata.ItemSelectionStrategy;

import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link XPathItemSelectionStrategy} unit test. */
public class XPathItemSelectionStrategyTest extends BaseDomTest {

    /**
     * Test XPathItemSelectionStrategy using an example from the UK federation build process.
     * 
     * @throws Exception if something goes wrong
     */
    @Test
    public void test() throws Exception {
        // Construct a map containing required namespace prefix definitions
        Map<String, String> prefixMappings = new HashMap<String, String>();
        prefixMappings.put("ukfedlabel", "http://ukfederation.org.uk/2006/11/label");

        // Construct the strategy object
        ItemSelectionStrategy<DomElementItem> strategy =
                new XPathItemSelectionStrategy("//ukfedlabel:DeletedEntity",
                        new SimpleNamespaceContext(prefixMappings));

        // Construct the input metadata
        DomElementItem item1 = new DomElementItem(readXmlData("xpathInput1.xml"));
        DomElementItem item2 = new DomElementItem(readXmlData("xpathInput2.xml"));
        DomElementItem item3 = new DomElementItem(readXmlData("xpathInput3.xml"));

        Assert.assertTrue(strategy.isSelectedItem(item1));
        Assert.assertFalse(strategy.isSelectedItem(item2));
        Assert.assertTrue(strategy.isSelectedItem(item3));
    }

}