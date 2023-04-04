
package net.shibboleth.metadata.pipeline;

import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.shared.collection.CollectionSupport;

public class StaticItemSourceStageTest {
    
    @Test
    public void testExecute() throws Exception {
        final var stage = new StaticItemSourceStage<String>();
        stage.setId("test");
        stage.setSourceItems(CollectionSupport.listOf(new MockItem("one"), new MockItem("two")));
        stage.initialize();
        final var items = new ArrayList<Item<String>>();
        stage.execute(items);
        Assert.assertEquals(items.size(), 2);
    }
    
    @Test
    public void testDestroy() throws Exception {
        final var stage = new StaticItemSourceStage<String>();
        stage.setId("test");
        stage.initialize();
        stage.destroy();
    }
}
