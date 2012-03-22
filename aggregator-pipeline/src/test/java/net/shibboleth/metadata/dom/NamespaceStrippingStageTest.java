package net.shibboleth.metadata.dom;

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.dom.NamespaceStrippingStage;

import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class NamespaceStrippingStageTest extends BaseDomTest {

    @Test
    public void doExecute() throws Exception {
        final Element doc = readXmlData("namespaceStripIn.xml");
        final DomElementItem item = new DomElementItem(doc);
        final List<DomElementItem> items = new ArrayList<DomElementItem>();
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
