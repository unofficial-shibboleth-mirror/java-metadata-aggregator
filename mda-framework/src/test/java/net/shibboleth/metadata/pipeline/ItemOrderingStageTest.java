
package net.shibboleth.metadata.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.shared.collection.CollectionSupport;

public class ItemOrderingStageTest {

    private class ReversalOrderingStrategy<T> implements ItemOrderingStrategy<T> {

        @Override
        public @Nonnull List<Item<T>> order(@Nonnull List<Item<T>> items) throws StageProcessingException {
            final var collection = new ArrayList<Item<T>>();
            collection.addAll(items);
            Collections.reverse(collection);
            return CollectionSupport.copyToList(collection);
        }
        
    }

    @Test
    public void testIdentity() throws Exception {
        final var stage = new ItemOrderingStage<String>();
        stage.setId("test");
        stage.initialize();

        final List<Item<String>> items = new ArrayList<>();
        items.add(new MockItem("one"));
        items.add(new MockItem("two"));
        items.add(new MockItem("three"));
        
        stage.execute(items);
        
        Assert.assertEquals(items.size(), 3);
        Assert.assertEquals(items.get(0).unwrap(), "one");
        Assert.assertEquals(items.get(1).unwrap(), "two");
        Assert.assertEquals(items.get(2).unwrap(), "three");

        stage.destroy();
    }

    @Test
    public void testReverse() throws Exception {
        final var stage = new ItemOrderingStage<String>();
        stage.setId("test");
        stage.setItemOrderingStrategy(new ReversalOrderingStrategy<String>());
        stage.initialize();

        final List<Item<String>> items = new ArrayList<>();
        items.add(new MockItem("one"));
        items.add(new MockItem("two"));
        items.add(new MockItem("three"));
        
        stage.execute(items);
        
        Assert.assertEquals(items.size(), 3);
        Assert.assertEquals(items.get(2).unwrap(), "one");
        Assert.assertEquals(items.get(1).unwrap(), "two");
        Assert.assertEquals(items.get(0).unwrap(), "three");

        stage.destroy();
    }

}
