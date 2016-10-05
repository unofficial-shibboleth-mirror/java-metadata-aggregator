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

import org.testng.annotations.Test;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;

public class NamespacesStrippingStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public NamespacesStrippingStageTest() {
        super(NamespacesStrippingStage.class);
    }

    /**
     * Test the simple case that is already handled by NamespaceStrippingStage,
     * of blacklisting a single namespace.
     * 
     * @throws Exception if something goes wrong
     */
    @Test
    public void simple() throws Exception {
        final Element doc = readXMLData("1-in.xml");
        final Item<Element> item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final NamespacesStrippingStage stage = new NamespacesStrippingStage();
        stage.setId("stripTest");
        final List<String> namespaces = new ArrayList<>();
        namespaces.add("urn:namespace:beta");
        stage.setNamespaces(namespaces);
        stage.initialize();
        
        stage.execute(items);
        
        final Element out = readXMLData("1-out.xml");
        assertXMLIdentical(out, item.unwrap());
    }

    /**
     * Test blacklisting.
     * 
     * @throws Exception if something goes wrong
     */
    @Test
    public void blacklist() throws Exception {
        final Element doc = readXMLData("2-in.xml");
        final Item<Element> item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final NamespacesStrippingStage stage = new NamespacesStrippingStage();
        stage.setId("stripTest");
        final List<String> namespaces = new ArrayList<>();
        namespaces.add("urn:namespace:bravo");
        namespaces.add("urn:namespace:charlie");
        stage.setNamespaces(namespaces);
        stage.initialize();
        
        stage.execute(items);
        
        final Element out = readXMLData("2-bl.xml");
        assertXMLIdentical(out, item.unwrap());
    }

    /**
     * Test whitelisting.
     * 
     * @throws Exception if something goes wrong
     */
    @Test
    public void whitelist() throws Exception {
        final Element doc = readXMLData("2-in.xml");
        final Item<Element> item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final NamespacesStrippingStage stage = new NamespacesStrippingStage();
        stage.setId("stripTest");
        final List<String> namespaces = new ArrayList<>();
        namespaces.add("urn:namespace:alfa"); // root element
        namespaces.add("urn:namespace:bravo");
        namespaces.add("urn:namespace:charlie");
        stage.setNamespaces(namespaces);
        stage.setWhitelisting(true);
        stage.initialize();
        
        stage.execute(items);
        
        final Element out = readXMLData("2-wl.xml");
        assertXMLIdentical(out, item.unwrap());
    }
}
