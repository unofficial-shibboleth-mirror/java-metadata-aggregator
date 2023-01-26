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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.util.FixedStringIdentifierGenerationStrategy;
import net.shibboleth.shared.xml.impl.BasicParserPool;
import net.shibboleth.shared.xml.ElementSupport;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GenerateIdStageTest extends BaseDOMTest {


    protected GenerateIdStageTest() {
        super(GenerateIdStage.class);
    }

    /**
     * Test for MDA-90 (default identifier strategy used by GenerateIdStage
     * sometimes generates bad IDs).
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void mda90() throws Exception {
        final GenerateIdStage stage = new GenerateIdStage();
        stage.setId("test");
        stage.initialize();
        
        final BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        final Document doc = parserPool.newDocument();
        final QName qname = SAMLMetadataSupport.ENTITY_DESCRIPTOR_NAME;
        final Element element = ElementSupport.constructElement(doc, qname);
        Assert.assertEquals(element.getLocalName(), qname.getLocalPart());
        doc.appendChild(element);
        
        final DOMElementItem item = new DOMElementItem(doc);
        final List<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(item);

        final Pattern ncNamePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_\\-\\.]+$");
        final int howMany = 1000;
        final Set<String> values = new HashSet<>(howMany);
        for (int iteration = 1; iteration<=howMany; iteration++) {
            stage.execute(metadataCollection);
            final Element el = item.unwrap();
            Assert.assertEquals(el.getLocalName(), qname.getLocalPart());
            Assert.assertTrue(el.hasAttribute("ID"));
            final String value = el.getAttribute("ID");

            // we should get a value each time
            Assert.assertNotNull(value);
            
            // we shouldn't see the same value twice
            if (values.contains(value)) {
                Assert.fail("duplicate value '" + value + "' on iteration " + iteration);
            }
            values.add(value);
            
            // values should be valid NCNames
            final Matcher match = ncNamePattern.matcher(value);
            if (!match.matches()) {
                Assert.fail("value '" + value + "' is not a valid NCName on iteration " + iteration);
            }
        }
        
        Assert.assertEquals(values.size(), howMany);
    }

    @Test
    public void testExplicitConstructor() throws Exception {
        final var strat = new FixedStringIdentifierGenerationStrategy("hello");

        final var stage = new GenerateIdStage(strat);
        stage.setId("test");
        stage.initialize();
        
        final var item = readDOMItem("in.xml");
        
        stage.execute(List.of(item));
        
        stage.destroy();
        
        Assert.assertEquals(item.unwrap().getAttribute("ID"), "hello");
    }
    
    @Test
    public void testNotSAMLEntity() throws Exception {
        final var strat = new FixedStringIdentifierGenerationStrategy("hello");

        final var stage = new GenerateIdStage(strat);
        stage.setId("test");
        stage.initialize();
        
        final var item = readDOMItem("notentity.xml");
        
        stage.execute(List.of(item));
        
        stage.destroy();
        
        Assert.assertEquals(item.unwrap().getAttribute("ID"), "original");
    }
}
