
package net.shibboleth.metadata;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FirstItemIdItemIdentificationStrategyTest {

    @Test
    public void unidentified() {
        final FirstItemIdItemIdentificationStrategy strat = new FirstItemIdItemIdentificationStrategy();
        Assert.assertEquals(strat.getNoItemIdIdentifier(), "unidentified");
        final Item<String> item = new MockItem("item");
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "unidentified");
    }
    
    @Test
    public void setNoItemIdIdentifier() {
        final FirstItemIdItemIdentificationStrategy strat = new FirstItemIdItemIdentificationStrategy();
        strat.setNoItemIdIdentifier("mu");
        Assert.assertEquals(strat.getNoItemIdIdentifier(), "mu");
        final Item<String> item = new MockItem("item");
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "mu");
    }
    
    @Test
    public void oneIdentifier() {
        final FirstItemIdItemIdentificationStrategy strat = new FirstItemIdItemIdentificationStrategy();
        final Item<String> item = new MockItem("item");
        item.getItemMetadata().put(new ItemId("id"));
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "id");
    }

    @Test
    public void twoIdentifiers() {
        final FirstItemIdItemIdentificationStrategy strat = new FirstItemIdItemIdentificationStrategy();
        final Item<String> item = new MockItem("item");
        item.getItemMetadata().put(new ItemId("first"));
        item.getItemMetadata().put(new ItemId("second"));
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "first");
    }

}
