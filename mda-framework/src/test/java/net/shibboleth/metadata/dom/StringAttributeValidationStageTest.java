
package net.shibboleth.metadata.dom;

import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.namespace.QName;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.saml.SAMLMetadataSupport;
import net.shibboleth.metadata.validate.RejectAllValidator;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.metadata.validate.testing.CollectingValidator;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.XMLConstants;

public class StringAttributeValidationStageTest extends BaseDOMTest {

    protected StringAttributeValidationStageTest() {
        super(StringAttributeValidationStage.class);
    }

    @Test
    public void testLifecycle() throws Exception {
        var stage = new StringAttributeValidationStage();
        stage.setId("test");
        stage.setElementName(new QName("test"));
        stage.setAttributeName("test");
        stage.initialize();
        stage.destroy();
    }

    @Test(expectedExceptions = ComponentInitializationException.class)
    public void noAttributeNames() throws Exception {
        var stage = new StringAttributeValidationStage();
        stage.setId("test");
        stage.setElementName(new QName("test"));
        stage.initialize();
        stage.destroy();
    }
    
    /**
     * Simple test to see if we can reach a singleton attribute
     * on an unqualified element.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSimpleXY() throws Exception {
        var item = parseDOMItem("<a><x y='a'/></a>");
        var items = new ArrayList<Item<Element>>();
        items.add(item);
        
        var reject = new RejectAllValidator<String>();
        reject.setId("reject");
        reject.initialize();
        var collect = CollectingValidator.<String>getInstance("collect");
        var validators = new ArrayList<Validator<String>>();
        validators.add(collect);
        validators.add(reject);

        var stage = new StringAttributeValidationStage();
        stage.setId("test");
        stage.setElementName(new QName("x"));
        stage.setAttributeName("y");
        stage.setValidators(validators);
        stage.initialize();
        stage.execute(items);

        stage.destroy();
        reject.destroy();
        
        var values = collect.getValues();
        collect.destroy();
        Assert.assertEquals(values.size(), 1);
        Assert.assertEquals(values.get(0), "a");

        var errors = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 1);
        var error = errors.get(0);
        Assert.assertEquals(error.getStatusMessage(), "value rejected: 'a'");
    }
    
    /**
     * Comprehensive test with multiple applicable elements and attributes,
     * both with namespaces and without.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testMultiple() throws Exception {
        var item = readDOMItem("multiple.xml");
        var items = new ArrayList<Item<Element>>();
        items.add(item);

        /*
         * We are going to pick off a few attributes:
         *
         * - The entityID from each of three EntityDescriptor elements (3)
         * - The index from each of 12 AssertionConsumerService elements (0..5, twice)
         * - The xml:lang from each of three OrganizationName elements (3)
         *
         * Total of: 18.
         * 
         * This will happen by intersecting the element names with the
         * attribute names.
         */
        var elements = new HashSet<QName>();
        elements.add(SAMLMetadataSupport.ENTITY_DESCRIPTOR_NAME);
        elements.add(new QName(SAMLMetadataSupport.MD_NS, "AssertionConsumerService"));
        elements.add(SAMLMetadataSupport.ORGANIZATIONNAME_NAME);
        var attributes = new HashSet<QName>();
        attributes.add(new QName("entityID"));
        attributes.add(new QName("index"));
        attributes.add(XMLConstants.XML_LANG_ATTRIB_NAME);

        var collect = CollectingValidator.<String>getInstance("collect");
        var validators = new ArrayList<Validator<String>>();
        validators.add(collect);

        var stage = new StringAttributeValidationStage();
        stage.setId("test");
        stage.setElementNames(elements);
        stage.setQualifiedAttributeNames(attributes);
        stage.setValidators(validators);
        stage.initialize();
        stage.execute(items);

        stage.destroy();

        var values = collect.getValues();
        collect.destroy();

        System.out.println(values);
        Assert.assertEquals(values.size(), 18);
        // Count how many "en"s there are
        Assert.assertEquals(values.stream().filter(x -> x.equals("en")).count(), 3);
        // Count how many "shibboleth.net"s there are
        Assert.assertEquals(values.stream().filter(x -> x.contains("shibboleth.net")).count(), 3);
        // Count how many indexes (matching single digits) there are
        Assert.assertEquals(values.stream().filter(x -> x.matches("\\d")).count(), 12);
    }
}
