
package net.shibboleth.metadata.pipeline;

import java.util.ArrayList;
import java.util.Collection;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AtLeastCollectionPredicateTest {

    @Test
    public void defaultMinimum() {
        final AtLeastCollectionPredicate<String> pred = new AtLeastCollectionPredicate<>();
        Assert.assertEquals(pred.getMinimum(), 0);
        
        final Collection<String> empty = new ArrayList<>();
        Assert.assertTrue(pred.apply(empty));
        
        final Collection<String> single = new ArrayList<>();
        single.add("single");
        Assert.assertTrue(pred.apply(single));
    }
    
    @Test
    public void explicitMinimum() {
        final AtLeastCollectionPredicate<String> pred = new AtLeastCollectionPredicate<>();
        pred.setMinimum(1);
        Assert.assertEquals(pred.getMinimum(), 1);
        
        final Collection<String> empty = new ArrayList<>();
        Assert.assertFalse(pred.apply(empty));
        
        final Collection<String> single = new ArrayList<>();
        single.add("single");
        Assert.assertTrue(pred.apply(single));
    }
    
}
