package net.shibboleth.metadata.dom;

import javax.xml.parsers.DocumentBuilderFactory;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class BaseDOMTestTest extends BaseDOMTest {

    protected BaseDOMTestTest() {
        super(BaseDOMTest.class);
    }

    // Check that readXMLData can read the prolog from a resource.
    @Test
    public void readXMLData_prolog() throws Exception {
        final Element docElement = readXMLData("prolog.xml");
        final Document doc = docElement.getOwnerDocument();
        Assert.assertEquals(doc.getChildNodes().getLength(), 2);

        final Node com = doc.getChildNodes().item(0);
        Assert.assertEquals(com.getNodeType(), Node.COMMENT_NODE);
        Assert.assertEquals(com.getNodeValue(), "comment in prolog");

        final Node root = doc.getChildNodes().item(1);
        Assert.assertEquals(root.getNodeType(), Node.ELEMENT_NODE);
    }

    // Check that assertXMLIdentical normalizes consecutive text nodes
    @Test
    public void assertXMLIdentical_normalized() throws Exception {
        final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        final Element el1 = doc.createElement("tag");
        el1.appendChild(doc.createTextNode("foo"));
        el1.appendChild(doc.createTextNode(""));
        el1.appendChild(doc.createTextNode("bar"));

        final Element el2 = doc.createElement("tag");
        el2.appendChild(doc.createTextNode("foobar"));

        // Verify that the nodes are *different* according to XMLUnit.
        final Diff diff = DiffBuilder.compare(el1).withTest(el2)
                .checkForIdentical().build();
        Assert.assertTrue(diff.hasDifferences());

        // Check that the nodes *look* identical to assertXMLIdentical.
        assertXMLIdentical(el1, el2);

        // Verify that the original nodes are *still* different according to XMLUnit.
        // (assertXMLIdentical should not have side-effects)
        final Diff diff2 = DiffBuilder.compare(el1).withTest(el2)
                .checkForIdentical().build();
        Assert.assertTrue(diff2.hasDifferences(), "side effect detected");
    }
}
