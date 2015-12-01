
package net.shibboleth.metadata.dom;

import javax.xml.parsers.DocumentBuilder;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.saml.SAMLMetadataSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import net.shibboleth.utilities.java.support.xml.ParserPool;

public class WhitespaceTrimmingVisitorTest extends BaseDOMTest {
    
    /** Constructor sets class under test. */
    public WhitespaceTrimmingVisitorTest() {
        super(WhitespaceTrimmingVisitor.class);
    }
    
    private DOMElementItem makeItem() throws Exception {
        final ParserPool parserPool = getParserPool();
        final DocumentBuilder builder = parserPool.getBuilder();
        final Document document = builder.newDocument();
        final Element element = ElementSupport.constructElement(document,
                SAMLMetadataSupport.ENTITY_DESCRIPTOR_NAME);
        ElementSupport.setDocumentElement(document, element);
        return new DOMElementItem(element);
    }
    
    @Test
    public void visitAttr() throws Exception {
        final DOMElementItem item = makeItem();
        final Document doc = item.unwrap().getOwnerDocument();
        final Attr attr = doc.createAttribute("foo");
        attr.setTextContent("   trimmed   \n\n   \t   ");
        final AttrVisitor av = new WhitespaceTrimmingVisitor();
        av.visitAttr(attr, item);
        Assert.assertEquals(attr.getTextContent(), "trimmed");
    }

    @Test
    public void visitElement() throws Exception {
        final DOMElementItem item = makeItem();
        final Document doc = item.unwrap().getOwnerDocument();
        final Element e = doc.createElement("foo");
        e.setTextContent("   trimmed   \n\n   \t   ");
        final ElementVisitor ev = new WhitespaceTrimmingVisitor();
        ev.visitElement(e, item);
        Assert.assertEquals(e.getTextContent(), "trimmed");
    }

    @Test
    public void visitNode() throws Exception {
        final DOMElementItem item = makeItem();
        final Document doc = item.unwrap().getOwnerDocument();
        final Attr attr = doc.createAttribute("foo");
        attr.setTextContent("   trimmed   \n\n   \t   ");
        final NodeVisitor nv = new WhitespaceTrimmingVisitor();
        nv.visitNode(attr, item);
        Assert.assertEquals(attr.getTextContent(), "trimmed");
    }
}
