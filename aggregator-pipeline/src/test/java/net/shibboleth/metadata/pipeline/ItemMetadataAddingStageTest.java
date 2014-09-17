
package net.shibboleth.metadata.pipeline;

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.ItemTag;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.StatusMetadata;
import net.shibboleth.metadata.WarningStatus;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ItemMetadataAddingStageTest {

    @Test
    public void testAddingMetadata() throws Exception {
        // make some item metadata to add
        final ItemTag tag1 = new ItemTag("tag1");
        final ItemTag tag2 = new ItemTag("tag2");
        final WarningStatus warn = new WarningStatus("component", "warning");
        final InfoStatus info = new InfoStatus("component", "information");
        final List<ItemMetadata> moreMeta = new ArrayList<>();
        moreMeta.add(tag1);
        moreMeta.add(tag2);
        moreMeta.add(warn);
        moreMeta.add(info);
        
        // make some items to add things to
        final MockItem mock1 = new MockItem("item1");
        final MockItem mock2 = new MockItem("item2");
        final List<Item<String>> items = new ArrayList<>();
        items.add(mock1);
        items.add(mock2);
        
        // make the stage
        final ItemMetadataAddingStage<String> stage = new ItemMetadataAddingStage<>();
        stage.setId("test");
        stage.setAdditionalItemMetadata(moreMeta);
        stage.initialize();
        
        stage.execute(items);
        
        Assert.assertEquals(2, items.size());
        Assert.assertEquals(2, items.get(0).getItemMetadata().get(ItemTag.class).size());
        Assert.assertEquals(2, items.get(1).getItemMetadata().get(ItemTag.class).size());
        Assert.assertEquals(1, items.get(0).getItemMetadata().get(WarningStatus.class).size());
        Assert.assertEquals(1, items.get(1).getItemMetadata().get(WarningStatus.class).size());
        Assert.assertEquals(1, items.get(0).getItemMetadata().get(InfoStatus.class).size());
        Assert.assertEquals(1, items.get(1).getItemMetadata().get(InfoStatus.class).size());
        Assert.assertEquals(2, items.get(0).getItemMetadata().get(StatusMetadata.class).size());
        Assert.assertEquals(2, items.get(1).getItemMetadata().get(StatusMetadata.class).size());
    }

}
