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

import net.shibboleth.utilities.java.support.xml.SimpleNamespaceContext;

import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link XPathItemSelectionStrategy} unit test. */
public class XPathItemSelectionStrategyTest extends BaseDOMTest {

    /**
     * Test XPathItemSelectionStrategy using an example from the UK federation build process.
     * 
     * @throws Exception if something goes wrong
     */
    @Test public void test() throws Exception {
        // Construct a map containing required namespace prefix definitions
        final Map<String, String> prefixMappings = new HashMap<>();
        prefixMappings.put("ukfedlabel", "http://ukfederation.org.uk/2006/11/label");

        // Construct the strategy object
        XPathItemSelectionStrategy strategy =
                new XPathItemSelectionStrategy("//ukfedlabel:DeletedEntity", new SimpleNamespaceContext(prefixMappings));

        // Construct the input metadata
        DOMElementItem item1 = new DOMElementItem(readXmlData("xpathInput1.xml"));
        DOMElementItem item2 = new DOMElementItem(readXmlData("xpathInput2.xml"));
        DOMElementItem item3 = new DOMElementItem(readXmlData("xpathInput3.xml"));

        Assert.assertTrue(strategy.apply(item1));
        Assert.assertFalse(strategy.apply(item2));
        Assert.assertTrue(strategy.apply(item3));
    }

}
