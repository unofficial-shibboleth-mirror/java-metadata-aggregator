
package net.shibboleth.metadata.dom.saml;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.testing.BaseDOMTest;
import net.shibboleth.shared.xml.ElementSupport;

public class AttributeValueElementMatcherTest extends BaseDOMTest {

    private final @Nonnull Document doc;
    private Element value;

    protected AttributeValueElementMatcherTest() throws Exception {
        super(AttributeValueElementMatcher.class);
        setUp();
        doc = getParserPool().newDocument();
    }

    @BeforeMethod
    private void beforeMethod() throws Exception {
        value = ElementSupport.constructElement(doc, SAMLSupport.ATTRIBUTE_VALUE_NAME);
    }

    @Test
    public void apply() throws Exception {
        final var matcher1 = new AttributeValueElementMatcher("value");

        value.setTextContent("value");
        Assert.assertTrue(matcher1.match(value));

        value.setTextContent("other");
        Assert.assertFalse(matcher1.match(value));

        final var matcher2 = new AttributeValueElementMatcher("other");

        value.setTextContent("value");
        Assert.assertFalse(matcher2.match(value));

        value.setTextContent("other");
        Assert.assertTrue(matcher2.match(value));
    }

}
