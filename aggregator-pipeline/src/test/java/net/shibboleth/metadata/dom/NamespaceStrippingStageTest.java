package net.shibboleth.metadata.dom;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class NamespaceStrippingStageTest extends BaseDOMTest {

    @Test
    public void doExecute() throws Exception {
        final Element doc = readXmlData("namespaceStripIn.xml");
        final DOMElementItem item = new DOMElementItem(doc);
        final List<DOMElementItem> items = new ArrayList<>();
        items.add(item);
        
        final NamespaceStrippingStage stage = new NamespaceStrippingStage();
        stage.setId("stripTest");
        stage.setNamespace("urn:namespace:beta");
        stage.initialize();
        
        stage.execute(items);
        
        final Element out = readXmlData("namespaceStripOut.xml");
        assertXmlIdentical(out, item.unwrap());
    }
}
