
package net.shibboleth.metadata.dom.saml;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.shared.xml.ElementSupport;

public class AttributeElementMatcherTest extends BaseDOMTest {

    private final Document doc;
    private Element attr;

    protected AttributeElementMatcherTest() throws Exception {
        super(AttributeElementMatcher.class);
        setUp();
        doc = getParserPool().newDocument();
    }

    @BeforeMethod
    private void beforeMethod() throws Exception {
        attr = ElementSupport.constructElement(doc, SAMLSupport.ATTRIBUTE_NAME);
    }

    @Test
    public void matchNormal() throws Exception {
        final var matcher1 = new AttributeElementMatcher("name", "name-format");
        attr.setAttribute("Name", "name");
        attr.setAttribute("NameFormat", "name-format");
        Assert.assertTrue(matcher1.match(attr));

        final var matcher2 = new AttributeElementMatcher("name2", "name-format");
        Assert.assertFalse(matcher2.match(attr));

        final var matcher3 = new AttributeElementMatcher("name", "name-format2");
        Assert.assertFalse(matcher3.match(attr));
    }

    @Test
    public void matchDefaultFormat() throws Exception {
        attr.setAttribute("Name", "name");

        final var matcher1 = new AttributeElementMatcher("name", SAMLSupport.ATTRNAME_FORMAT_UNSPECIFIED);
        Assert.assertTrue(matcher1.match(attr));

        final var matcher2 = new AttributeElementMatcher("name2", SAMLSupport.ATTRNAME_FORMAT_UNSPECIFIED);
        Assert.assertFalse(matcher2.match(attr));
    }

}
