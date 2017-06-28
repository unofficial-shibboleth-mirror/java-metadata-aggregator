
package net.shibboleth.metadata.dom.saml;

import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Predicate;

public class AttributeValueElementMatcherTest extends BaseDOMTest {

    private final Document doc;
    private Element value;

    protected AttributeValueElementMatcherTest() throws Exception {
        super(AttributeValueElementMatcher.class);
        setUp();
        doc = getParserPool().newDocument();
    }

    @BeforeTest
    private void beforeTest() throws Exception {
        value = ElementSupport.constructElement(doc, SAMLSupport.ATTRIBUTE_VALUE_NAME);
    }

    @Test
    public void apply() throws Exception {
        final Predicate<Element> matcher1 = new AttributeValueElementMatcher("value");

        value.setTextContent("value");
        Assert.assertTrue(matcher1.apply(value));

        value.setTextContent("other");
        Assert.assertFalse(matcher1.apply(value));

        final Predicate<Element> matcher2 = new AttributeValueElementMatcher("other");

        value.setTextContent("value");
        Assert.assertFalse(matcher2.apply(value));

        value.setTextContent("other");
        Assert.assertTrue(matcher2.apply(value));
    }

}
