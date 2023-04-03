
package net.shibboleth.metadata;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.shared.collection.CollectionSupport;

public class DeduplicatingItemIdMergeStrategyTest {

    @Test
    public void testWithoutItemIds() {
        // First collection has an entity with an ID, and another without
        final List<Item<String>> coll1 = List.of(new MockItem("first"), new MockItem("second"));
        coll1.get(0).getItemMetadata().put(new ItemId("first-id"));

        // Second collection has an entity with the same ID, and another without
        final List<Item<String>> coll2 = List.of(new MockItem("third"), new MockItem("fourth"));
        coll2.get(0).getItemMetadata().put(new ItemId("first-id"));

        final var merge = new DeduplicatingItemIdMergeStrategy();
        final var result = new ArrayList<Item<String>>();
        merge.merge(result, CollectionSupport.listOf(coll1, coll2));
        
        Assert.assertEquals(result.size(), 3);
        final var i1 = result.get(0);
        final var i2 = result.get(1);
        final var i3 = result.get(2);
        Assert.assertEquals(i1.unwrap(), "first");
        Assert.assertEquals(i2.unwrap(), "second");
        Assert.assertEquals(i3.unwrap(), "fourth");
    }

}
