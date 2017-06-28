
package net.shibboleth.metadata.dom.saml;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Function;

import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.Container;

public class AttributeValueElementMakerTest extends BaseDOMTest {

    private final Document doc;

    protected AttributeValueElementMakerTest() throws Exception {
        super(AttributeValueElementMaker.class);
        setUp();
        doc = getParserPool().newDocument();
    }

    @Test
    public void apply() {
        final Function<Container, Element> maker = new AttributeValueElementMaker("value text");
        final Element root = doc.createElementNS("ns", "root");
        final Container rootContainer = new Container(root);
        final Element newElement = maker.apply(rootContainer);
        Assert.assertNotNull(newElement);
        Assert.assertEquals(newElement.getLocalName(), "AttributeValue");
        Assert.assertEquals(newElement.getNamespaceURI(), SAMLSupport.SAML_NS);
        Assert.assertEquals(newElement.getTextContent(), "value text");
    }
}
