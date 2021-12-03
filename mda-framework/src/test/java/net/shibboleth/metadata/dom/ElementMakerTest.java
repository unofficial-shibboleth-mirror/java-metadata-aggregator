
package net.shibboleth.metadata.dom;

import java.util.function.Function;

import javax.xml.namespace.QName;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ElementMakerTest extends BaseDOMTest {

    private final Document doc;

    protected ElementMakerTest() throws Exception {
        super(ElementMaker.class);
        setUp();
        doc = getParserPool().newDocument();
    }

    @Test
    public void apply() {
        final Function<Container, Element> maker = new ElementMaker(new QName("ns", "local"));
        final Element root = doc.createElementNS("ns", "root");
        final Container rootContainer = new Container(root);
        final Element newElement = maker.apply(rootContainer);
        Assert.assertNotNull(newElement);
        Assert.assertEquals(newElement.getLocalName(), "local");
        Assert.assertEquals(newElement.getNamespaceURI(), "ns");
    }
}
