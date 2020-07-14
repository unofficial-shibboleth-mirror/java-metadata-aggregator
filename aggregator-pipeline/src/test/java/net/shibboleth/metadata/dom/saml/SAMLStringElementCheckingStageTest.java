
package net.shibboleth.metadata.dom.saml;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;

public class SAMLStringElementCheckingStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public SAMLStringElementCheckingStageTest() {
        super(SAMLStringElementCheckingStage.class);
    }
    
    @Test
    public void testOK() throws Exception {
        final Item<Element> item = new DOMElementItem(readXMLData("ok.xml"));
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final SAMLStringElementCheckingStage stage = new SAMLStringElementCheckingStage();
        stage.setId("test");
        stage.setElementNames(Set.of(SAMLMetadataSupport.ORGANIZATIONNAME_NAME));
        stage.initialize();
        
        stage.execute(items);
        
        final Item<Element> outItem = items.get(0);
        Assert.assertSame(outItem, item);
        final List<ErrorStatus> errors = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 0);
    }
    
    @Test
    public void testFail() throws Exception {
        final Item<Element> item = new DOMElementItem(readXMLData("fail.xml"));
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final SAMLStringElementCheckingStage stage = new SAMLStringElementCheckingStage();
        stage.setId("test");
        stage.setElementNames(Set.of(SAMLMetadataSupport.ORGANIZATIONNAME_NAME,
                SAMLMetadataSupport.ORGANIZATIONDISPLAYNAME_NAME));
        stage.initialize();
        
        stage.execute(items);
        
        final Item<Element> outItem = items.get(0);
        Assert.assertSame(outItem, item);
        final List<ErrorStatus> errors = item.getItemMetadata().get(ErrorStatus.class);
        //for (final ErrorStatus error : errors) {
        //    System.out.println(error.getComponentId() + ": " + error.getStatusMessage());
        //}
        Assert.assertEquals(errors.size(), 3);
    }
}
