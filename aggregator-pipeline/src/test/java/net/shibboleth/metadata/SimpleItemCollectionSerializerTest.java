package net.shibboleth.metadata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleItemCollectionSerializerTest {

    @Test
    public void serializeCollection() {
        final Item<String> i1 = new MockItem("one");
        final Item<String> i2 = new MockItem("two");
        final Item<String> i3 = new MockItem("three");
        final List<Item<String>> items = new ArrayList<>();
        items.add(i1);
        items.add(i2);
        items.add(i3);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final ItemSerializer<String> base = new ItemSerializer<String>() {
            @Override
            public void serialize(Item<String> item, OutputStream output) {
                try {
                    output.write(item.unwrap().getBytes());
                } catch (IOException e) {
                    Assert.fail("exception in string serializer", e);
                }
            }
        };
        final ItemCollectionSerializer<String> ics = new SimpleItemCollectionSerializer<>(base);
        ics.serializeCollection(items, output);
        Assert.assertEquals(output.toByteArray(),
                new byte[]{'o', 'n', 'e', 't', 'w', 'o', 't', 'h', 'r', 'e', 'e'});
    }

}
