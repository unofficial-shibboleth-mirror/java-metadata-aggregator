
package net.shibboleth.metadata.dom.saml;

import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Predicate;

public class AttributeElementMatcherTest extends BaseDOMTest {

    private final Document doc;
    private Element attr;

    protected AttributeElementMatcherTest() throws Exception {
        super(AttributeElementMatcher.class);
        setUp();
        doc = getParserPool().newDocument();
    }

    @BeforeTest
    private void beforeTest() throws Exception {
        attr = ElementSupport.constructElement(doc, SAMLSupport.ATTRIBUTE_NAME);
    }

    @Test
    public void matchNormal() throws Exception {
        final Predicate<Element> matcher1 = new AttributeElementMatcher("name", "name-format");
        attr.setAttribute("Name", "name");
        attr.setAttribute("NameFormat", "name-format");
        Assert.assertTrue(matcher1.apply(attr));

        final Predicate<Element> matcher2 = new AttributeElementMatcher("name2", "name-format");
        Assert.assertFalse(matcher2.apply(attr));

        final Predicate<Element> matcher3 = new AttributeElementMatcher("name", "name-format2");
        Assert.assertFalse(matcher3.apply(attr));
    }

    @Test
    public void matchDefaultFormat() throws Exception {
        attr.setAttribute("Name", "name");

        final Predicate<Element> matcher1 = new AttributeElementMatcher("name", SAMLSupport.ATTRNAME_FORMAT_UNSPECIFIED);
        Assert.assertTrue(matcher1.apply(attr));

        final Predicate<Element> matcher2 = new AttributeElementMatcher("name2", SAMLSupport.ATTRNAME_FORMAT_UNSPECIFIED);
        Assert.assertFalse(matcher2.apply(attr));
    }

}