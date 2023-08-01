/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import org.testng.annotations.Test;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.saml.SAMLMetadataSupport;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.xml.XMLParserException;

public class ElementWhitespaceTrimmingStageTest extends BaseDOMTest {
    
    /** Constructor sets class under test. */
    public ElementWhitespaceTrimmingStageTest() {
        super(ElementWhitespaceTrimmingStage.class);
    }
    
    private ElementWhitespaceTrimmingStage makeStage() throws ComponentInitializationException {
        final ElementWhitespaceTrimmingStage stage = new ElementWhitespaceTrimmingStage();
        stage.setId("test");
        return stage; 
    }
    
    private DOMElementItem makeItem(final @Nonnull String which) throws XMLParserException {
        final Element doc = readXMLData(classRelativeResource(which));
        return new DOMElementItem(doc);
    }
    
    @Test
    public void simpleTest() throws Exception {
        final DOMElementItem item = makeItem("1-in.xml");
        final Element expected = readXMLData(classRelativeResource("1-out.xml"));
        
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final Set<QName> names = new HashSet<>();
        names.add(new QName("urn:oasis:names:tc:SAML:metadata:ui", "DisplayName"));
        names.add(new QName(SAMLMetadataSupport.MD_NS, "NameIDFormat"));
                
        final ElementWhitespaceTrimmingStage stage = makeStage();
        stage.setElementNames(names);
        stage.initialize();
        
        stage.execute(items);
        stage.destroy();
        
        assertXMLIdentical(expected, item.unwrap());
    }
    
    @Test
    public void testSingleton() throws Exception {
        final DOMElementItem item = makeItem("2-in.xml");
        final Element expected = readXMLData(classRelativeResource("2-out.xml"));
        
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
                        
        final ElementWhitespaceTrimmingStage stage = makeStage();
        stage.setElementName(new QName(SAMLMetadataSupport.MD_NS, "NameIDFormat"));
        stage.initialize();
        
        stage.execute(items);
        stage.destroy();
        
        assertXMLIdentical(expected, item.unwrap());
    }
    
}
