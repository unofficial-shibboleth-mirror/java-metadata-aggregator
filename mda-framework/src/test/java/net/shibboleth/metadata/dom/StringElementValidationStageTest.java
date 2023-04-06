
package net.shibboleth.metadata.dom;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import org.testng.Assert;
import org.testng.Assert.ThrowingRunnable;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.saml.SAMLMetadataSupport;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;
import net.shibboleth.metadata.validate.RejectAllValidator;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.metadata.validate.string.AcceptStringRegexValidator;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.component.ComponentInitializationException;

public class StringElementValidationStageTest extends BaseDOMTest {


    protected StringElementValidationStageTest() {
        super(StringElementValidationStage.class);
    }

    @Test
    public void testNoElementNames() throws Exception {
        final StringElementValidationStage stage = new StringElementValidationStage();
        stage.setId("test");
        Assert.assertThrows(ComponentInitializationException.class,
                new ThrowingRunnable() {
                    @Override
                    public void run() throws Throwable {
                        stage.initialize();
                    }
                }
        );
    }
    
    @Test
    public void testNoValidators() throws Exception {
        final Item<Element> item = readDOMItem("two-bad-addrs.xml");
        final List<Item<Element>> items = new ArrayList<>();

        final StringElementValidationStage stage = new StringElementValidationStage();
        stage.setId("test");
        stage.setElementName(new QName(SAMLMetadataSupport.MD_NS, "EmailAddress"));
        // don't set any validators
        stage.initialize();
        
        stage.execute(items);
        stage.destroy();
        
        final List<ErrorStatus> errors = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertTrue(errors.isEmpty());
    }

    @Test
    public void testEmail() throws Exception {
        final Item<Element> item = readDOMItem("two-bad-addrs.xml");
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
    
        final var val = new AcceptStringRegexValidator();
        val.setId("email");
        val.setRegex("mailto:[0-9a-zA-Z]+\\@[0-9a-zA-Z.]+");
        val.initialize();

        final var stop = new RejectAllValidator<String>();
        stop.setId("stop");
        stop.initialize();
        
        final @Nonnull List<Validator<String>> validators = CollectionSupport.listOf(val, stop);
      
        final StringElementValidationStage stage = new StringElementValidationStage();
        stage.setId("test");
        stage.setElementName(new QName(SAMLMetadataSupport.MD_NS, "EmailAddress"));
        stage.setValidators(validators);
        stage.initialize();
      
        stage.execute(items);
        stage.destroy();

        final List<ErrorStatus> errors = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 2);
    }

    @Test
    public void testTwoNames() throws Exception {
        final Item<Element> item = readDOMItem("two-bad-addrs.xml");
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);

        final var stop = new RejectAllValidator<String>();
        stop.setId("stop");
        stop.initialize();
        
        final StringElementValidationStage stage = new StringElementValidationStage();
        stage.setId("test");
        stage.setElementNames(CollectionSupport.listOf(SAMLMetadataSupport.ORGANIZATIONNAME_NAME,
                SAMLMetadataSupport.ORGANIZATIONDISPLAYNAME_NAME));
        stage.setValidators(CollectionSupport.listOf(stop)); // reject everything
        stage.initialize();
      
        stage.execute(items);
        stage.destroy();

        final List<ErrorStatus> errors = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 2 /* each */ * 3 /* entities */);
    }

}
