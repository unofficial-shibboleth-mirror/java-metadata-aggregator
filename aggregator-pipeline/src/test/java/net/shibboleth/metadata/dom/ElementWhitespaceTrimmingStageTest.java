
package net.shibboleth.metadata.dom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.testng.annotations.Test;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.saml.SAMLMetadataSupport;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

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
    
    private DOMElementItem makeItem(final String which) throws XMLParserException {
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
        
        assertXMLIdentical(expected, item.unwrap());
    }
    
}
