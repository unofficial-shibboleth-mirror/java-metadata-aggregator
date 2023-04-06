
package net.shibboleth.metadata.dom;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.testing.BaseDOMTest;

public class SimpleElementMatcherTest extends BaseDOMTest {

    private final Document doc;

    protected SimpleElementMatcherTest() throws Exception {
        super(SimpleElementMatcher.class);
        setUp();
        doc = getParserPool().newDocument();
    }

    private @Nonnull Element createElementNS(final @Nonnull String ns, final @Nonnull String name) {
        final Element e = doc.createElementNS(ns, name);
        assert e != null;
        return e;
    }

    @Test
    public void matcher() throws Exception {
        final ElementMatcher matcher = new SimpleElementMatcher(new QName("ns", "xxx"));
        Assert.assertTrue(matcher.match(createElementNS("ns", "xxx")));
        Assert.assertFalse(matcher.match(createElementNS("ns", "yyy")));
        Assert.assertFalse(matcher.match(createElementNS("ns2", "xxx")));
        Assert.assertFalse(matcher.match(createElementNS("ns2", "yyy")));
    }

}
