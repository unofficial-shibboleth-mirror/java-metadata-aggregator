
package net.shibboleth.metadata.pipeline;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.TestMarker;

public class CompositeStageTest {

    @Test
    public void doExecute0Test() throws Exception {
        final var stage = new CompositeStage<String>();
        stage.setId("test");
        stage.initialize();
        Assert.assertEquals(stage.getComposedStages().size(), 0);
        
        final var items = List.<Item<String>>of(new MockItem("hello"));
        stage.execute(items);
        Assert.assertEquals(items.size(), 1);
        // No stages --> no errors added
        Assert.assertEquals(items.get(0).getItemMetadata().get(TestMarker.class).size(), 0);
    }

    @Test
    public void doExecute1Test() throws Exception {
        final var marker = new MarkerStage<String>();
        marker.setId("marker");
        marker.initialize();

        final var stage = new CompositeStage<String>();
        stage.setId("test");
        stage.setComposedStages(List.of(marker));
        stage.initialize();
        Assert.assertEquals(stage.getComposedStages().size(), 1);

        final var items = List.<Item<String>>of(new MockItem("hello"));
        stage.execute(items);
        Assert.assertEquals(items.size(), 1);
        // One stage --> one error added
        Assert.assertEquals(items.get(0).getItemMetadata().get(TestMarker.class).size(), 1);
    }

    @Test
    public void doExecute2Test() throws Exception {
        final var marker = new MarkerStage<String>();
        marker.setId("marker");
        marker.initialize();

        final var stage = new CompositeStage<String>();
        stage.setId("test");
        stage.setComposedStages(List.of(marker, marker));
        stage.initialize();
        Assert.assertEquals(stage.getComposedStages().size(), 2);

        final var items = List.<Item<String>>of(new MockItem("hello"));
        stage.execute(items);
        Assert.assertEquals(items.size(), 1);
        // Two stages --> two errors added
        Assert.assertEquals(items.get(0).getItemMetadata().get(TestMarker.class).size(), 2);
    }

    @Test
    public void doDestroyTest() throws Exception {
        final var stage = new CompositeStage<String>();
        stage.setId("test");
        stage.initialize();
        stage.destroy();
    }

}
