package net.shibboleth.metadata.dom;

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.Item;

import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class NamespaceStrippingStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public NamespaceStrippingStageTest() {
        super(NamespaceStrippingStage.class);
    }

    @Test
    public void doExecute() throws Exception {
        final Element doc = readXMLData("in.xml");
        final Item<Element> item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final NamespaceStrippingStage stage = new NamespaceStrippingStage();
        stage.setId("stripTest");
        stage.setNamespace("urn:namespace:beta");
        stage.initialize();
        
        stage.execute(items);
        
        final Element out = readXMLData("out.xml");
        assertXMLIdentical(out, item.unwrap());
    }
}
