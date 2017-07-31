package net.shibboleth.metadata.dom;

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.Item;

import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class ElementsStrippingStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public ElementsStrippingStageTest() {
        super(ElementsStrippingStage.class);
    }

    @Test
    public void emptyNamesList() throws Exception {
        final Element doc = readXMLData("in-sb.xml");
        final Item<Element> item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);

        final ElementsStrippingStage stage = new ElementsStrippingStage();
        stage.setId("stripTest");
        stage.setElementNamespace("urn:namespace:beta");
        stage.initialize();

        stage.execute(items);

        // nothing should have changed; compare with original
        final Element out = readXMLData("in-sb.xml");
        assertXMLIdentical(out, item.unwrap());
    }

    // Equivalent to the test for ElementStrippingStage
    @Test
    public void singleElementBlacklist() throws Exception {
        final Element doc = readXMLData("in-sb.xml");
        final Item<Element> item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);

        final ElementsStrippingStage stage = new ElementsStrippingStage();
        stage.setId("stripTest");
        stage.setElementNamespace("urn:namespace:beta");
        final List<String> names = new ArrayList<>();
        names.add("StripMe");
        stage.setElementNames(names);
        stage.initialize();

        stage.execute(items);

        final Element out = readXMLData("out-sb.xml");
        assertXMLIdentical(out, item.unwrap());
    }

    @Test
    public void singleElementWhitelist() throws Exception {
        final Element doc = readXMLData("in-sw.xml");
        final Item<Element> item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);

        final ElementsStrippingStage stage = new ElementsStrippingStage();
        stage.setId("stripTest");
        stage.setElementNamespace("urn:namespace:beta");
        final List<String> names = new ArrayList<>();
        names.add("StripMe");
        stage.setElementNames(names);
        stage.setWhitelisting(true);
        stage.initialize();

        stage.execute(items);

        final Element out = readXMLData("out-sw.xml");
        assertXMLIdentical(out, item.unwrap());
    }

    @Test
    public void multiElementWhitelist() throws Exception {
        final Element doc = readXMLData("in-multi.xml");
        final Item<Element> item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);

        final ElementsStrippingStage stage = new ElementsStrippingStage();
        stage.setId("stripTest");
        stage.setElementNamespace("urn:namespace:beta");
        final List<String> names = new ArrayList<>();
        names.add("one");
        names.add("three");
        stage.setElementNames(names);
        stage.initialize();

        stage.execute(items);

        final Element out = readXMLData("out-multi-b.xml");
        assertXMLIdentical(out, item.unwrap());
    }

    @Test
    public void multiElementBlacklist() throws Exception {
        final Element doc = readXMLData("in-multi.xml");
        final Item<Element> item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);

        final ElementsStrippingStage stage = new ElementsStrippingStage();
        stage.setId("stripTest");
        stage.setElementNamespace("urn:namespace:beta");
        final List<String> names = new ArrayList<>();
        names.add("one");
        names.add("three");
        stage.setElementNames(names);
        stage.setWhitelisting(true);
        stage.initialize();

        stage.execute(items);

        final Element out = readXMLData("out-multi-w.xml");
        assertXMLIdentical(out, item.unwrap());
    }
}
