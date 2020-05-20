
package net.shibboleth.metadata.dom.saml.mdui;

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.utilities.java.support.collection.ClassToInstanceMultiMap;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class IPHintValidationStageTest extends BaseDOMTest {

    /**
     * Constructor sets class under test.
     * 
     * @throws Exception if something goes wrong
     */
    public IPHintValidationStageTest() throws Exception {
        super(IPHintValidationStage.class);
    }
    
    @Test
    public void missingComponent() throws Exception {
        final Element doc = readXMLData("1.xml");
        final DOMElementItem item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final IPHintValidationStage stage = new IPHintValidationStage();
        stage.setId("test");
        stage.initialize();
        
        stage.execute(items);
        
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        final List<ErrorStatus> errors = metadata.get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 1);
        
        final ErrorStatus error = errors.get(0);
        Assert.assertTrue(error.getStatusMessage().contains("193.72.192/26"));
    }

    @Test
    public void hostAddress() throws Exception {
        final Element doc = readXMLData("2.xml");
        final DOMElementItem item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final IPHintValidationStage stage = new IPHintValidationStage();
        stage.setId("test");
        stage.setCheckingNetworks(true);
        stage.initialize();
        
        stage.execute(items);
        
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        final List<ErrorStatus> errors = metadata.get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 1);
        
        final ErrorStatus error = errors.get(0);
        Assert.assertTrue(error.getStatusMessage().contains("82.68.124.32/3"));
    }

    @Test
    public void ignoreHostAddress() throws Exception {
        final Element doc = readXMLData("2.xml");
        final DOMElementItem item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final IPHintValidationStage stage = new IPHintValidationStage();
        stage.setId("test");
        stage.setCheckingNetworks(false);
        stage.initialize();
        
        stage.execute(items);
        
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        final List<ErrorStatus> errors = metadata.get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 0);
    }
    
    @Test
    public void missingSlash() throws Exception {
        final Element doc = readXMLData("3.xml");
        final DOMElementItem item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final IPHintValidationStage stage = new IPHintValidationStage();
        stage.setId("test");
        stage.setCheckingNetworks(true);
        stage.initialize();
        
        stage.execute(items);
        
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        final List<ErrorStatus> errors = metadata.get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 1);
        
        final ErrorStatus error = errors.get(0);
        Assert.assertTrue(error.getStatusMessage().contains("11.12.13.14"));
    }

}
