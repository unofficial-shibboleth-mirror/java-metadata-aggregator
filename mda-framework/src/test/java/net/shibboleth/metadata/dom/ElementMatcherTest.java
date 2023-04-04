
package net.shibboleth.metadata.dom;

import javax.xml.namespace.QName;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

public class ElementMatcherTest extends BaseDOMTest {

    private final Document doc;

    protected ElementMatcherTest() throws Exception {
        super(SimpleElementMatcher.class);
        setUp();
        doc = getParserPool().newDocument();
    }

    @Test
    public void matcher() throws Exception {
        final ElementMatcher matcher = new SimpleElementMatcher(new QName("ns", "xxx"));
        Assert.assertTrue(matcher.match(doc.createElementNS("ns", "xxx")));
        Assert.assertFalse(matcher.match(doc.createElementNS("ns", "yyy")));
        Assert.assertFalse(matcher.match(doc.createElementNS("ns2", "xxx")));
        Assert.assertFalse(matcher.match(doc.createElementNS("ns2", "yyy")));
    }

}
