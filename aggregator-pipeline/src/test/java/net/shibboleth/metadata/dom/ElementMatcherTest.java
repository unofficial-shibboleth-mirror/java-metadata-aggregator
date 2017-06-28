
package net.shibboleth.metadata.dom;

import javax.xml.namespace.QName;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Predicate;

public class ElementMatcherTest extends BaseDOMTest {

    private final Document doc;

    protected ElementMatcherTest() throws Exception {
        super(ElementMatcher.class);
        setUp();
        doc = getParserPool().newDocument();
    }

    @Test
    public void matcher() throws Exception {
        final Predicate<Element> matcher = new ElementMatcher(new QName("ns", "xxx"));
        Assert.assertTrue(matcher.apply(doc.createElementNS("ns", "xxx")));
        Assert.assertFalse(matcher.apply(doc.createElementNS("ns", "yyy")));
        Assert.assertFalse(matcher.apply(doc.createElementNS("ns2", "xxx")));
        Assert.assertFalse(matcher.apply(doc.createElementNS("ns2", "yyy")));
    }

}
