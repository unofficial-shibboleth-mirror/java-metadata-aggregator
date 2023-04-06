
package net.shibboleth.metadata.dom.saml;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.Container;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;

public class AttributeValueElementMakerTest extends BaseDOMTest {

    private final Document doc;

    protected AttributeValueElementMakerTest() throws Exception {
        super(AttributeValueElementMaker.class);
        setUp();
        doc = getParserPool().newDocument();
    }

    @Test
    public void apply() {
        final var maker = new AttributeValueElementMaker("value text");
        final Element root = doc.createElementNS("ns", "root");
        assert root != null;
        final Container rootContainer = new Container(root);
        final Element newElement = maker.make(rootContainer);
        Assert.assertNotNull(newElement);
        Assert.assertEquals(newElement.getLocalName(), "AttributeValue");
        Assert.assertEquals(newElement.getNamespaceURI(), SAMLSupport.SAML_NS);
        Assert.assertEquals(newElement.getTextContent(), "value text");
    }
}
