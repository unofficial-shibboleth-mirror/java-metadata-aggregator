package net.shibboleth.metadata.dom;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import net.shibboleth.metadata.Item;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class DOMElementSerializerTest extends BaseDOMTest {

    protected DOMElementSerializerTest() {
        super(DOMElementSerializer.class);
    }

    @Test
    public void noItemsCollection() {
        final Collection<Item<Element>> items = new ArrayList<>();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final DOMElementSerializer ser = new DOMElementSerializer();
        ser.serializeCollection(items, output);
        Assert.assertEquals(output.toByteArray().length, 0);
    }
    
    @Test
    public void twoItemsCollection() throws Exception {
        final DOMElementSerializer ser = new DOMElementSerializer();

        final Collection<Item<Element>> items = new ArrayList<>();
        final Element x1 = readXMLData("1.xml");
        final Item<Element> i1 = new DOMElementItem(x1);
        items.add(i1);
        final Element x2 = readXMLData("2.xml");
        final Item<Element> i2 = new DOMElementItem(x2);
        items.add(i2);
        Assert.assertEquals(items.size(), 2);
        
        // get the serialized form of the first item
        final ByteArrayOutputStream o1 = new ByteArrayOutputStream();
        ser.serialize(i1, o1);
        
        // get the serialized form of the collection
        final ByteArrayOutputStream o2 = new ByteArrayOutputStream();
        ser.serializeCollection(items, o2);
        
        Assert.assertEquals(o2.toByteArray(), o1.toByteArray());
    }
}
