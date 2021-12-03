
package net.shibboleth.metadata.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;

public class StaticItemSourceStageTest {
    
    @Test
    public void testExecute() throws Exception {
        final var stage = new StaticItemSourceStage<String>();
        stage.setId("test");
        stage.setSourceItems(List.of(new MockItem("one"), new MockItem("two")));
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
