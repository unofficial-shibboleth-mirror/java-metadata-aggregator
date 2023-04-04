
package net.shibboleth.metadata.dom;

import javax.xml.namespace.QName;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ElementMakerTest extends BaseDOMTest {

    private final Document doc;

    protected ElementMakerTest() throws Exception {
        super(SimpleElementMaker.class);
        setUp();
        doc = getParserPool().newDocument();
    }

    @Test
    public void apply() {
        final var maker = new SimpleElementMaker(new QName("ns", "local"));
        final Element root = doc.createElementNS("ns", "root");
        assert root != null;
        final Container rootContainer = new Container(root);
        final Element newElement = maker.make(rootContainer);
        Assert.assertNotNull(newElement);
        Assert.assertEquals(newElement.getLocalName(), "local");
        Assert.assertEquals(newElement.getNamespaceURI(), "ns");
    }
}
