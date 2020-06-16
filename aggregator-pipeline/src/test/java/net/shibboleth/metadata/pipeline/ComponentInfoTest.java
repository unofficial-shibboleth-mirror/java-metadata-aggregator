
package net.shibboleth.metadata.pipeline;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;

public class ComponentInfoTest {

    @Test
    public void testBasicOperation() throws Exception {
        
        // A predicate which just delays for 11ms and then returns true.
        final var delayingCollectionPredicate = new Predicate<Collection<Item<String>>>() {
            public boolean test(Collection<Item<String>> t) {
                try {
                    Thread.sleep(11);
                } catch (InterruptedException e) {
                }
                return true;
            }
        };

        final Item<String> item = new MockItem("test");
        final List<Item<String>> items = new ArrayList<>();
        items.add(item);

        // comp1 is a CompositeStage which does nothing other than delay for 10ms
        final CompositeStage<String> comp1 = new CompositeStage<>();
        comp1.setId("comp1");
        comp1.setCollectionPredicate(delayingCollectionPredicate);
        comp1.initialize();

        // comp2 is a CompositeStage which does nothing other than delay for 10ms
        final CompositeStage<String> comp2 = new CompositeStage<>();
        comp2.setId("comp2");
        comp2.setCollectionPredicate(delayingCollectionPredicate);
        comp2.initialize();

        final List<Stage<String>> stages = new ArrayList<>();
        stages.add(comp1);
        stages.add(comp2);

        final SimplePipeline<String> pipe = new SimplePipeline<>();
        pipe.setId("pipe");
        pipe.setStages(stages);
        pipe.initialize();

        pipe.execute(items);

        final List<ComponentInfo> infos = item.getItemMetadata().get(ComponentInfo.class);

        // expect one for each CompositeStage and one for the SimplePipeline
        Assert.assertEquals(infos.size(), 3);
        final var info0 = infos.get(0);
        final var info1 = infos.get(1);
        final var info2 = infos.get(2);

        Assert.assertSame(infos.get(0).getComponentType(), CompositeStage.class, "0");
        Assert.assertEquals(infos.get(0).getComponentId(), "comp1", "0");

        Assert.assertSame(infos.get(1).getComponentType(), CompositeStage.class, "1");
        Assert.assertEquals(infos.get(1).getComponentId(), "comp2", "1");

        Assert.assertSame(infos.get(2).getComponentType(), SimplePipeline.class, "2");
        Assert.assertEquals(infos.get(2).getComponentId(), "pipe", "2");

        // Timing for first stage: takes at least 10ms.
        final var time0 = Duration.between(info0.getStartInstant(), info0.getCompleteInstant());
        final var nano0 = time0.toNanos();
        Assert.assertTrue(nano0 >= 10_000_000);

        // Timing for second stage: takes at least 10ms.
        final var time1 = Duration.between(info1.getStartInstant(), info1.getCompleteInstant());
        final var nano1 = time1.toNanos();
        Assert.assertTrue(nano1 >= 10_000_000);

        // Second stage does not start before first stage ends
        Assert.assertFalse(info1.getStartInstant().isBefore(info0.getCompleteInstant()));

        // Timing for pipeline: takes at least 20ms
        final var time2 = Duration.between(info2.getStartInstant(), info2.getCompleteInstant());
        final var nano2 = time2.toNanos();
        Assert.assertTrue(nano2 >= 20_000_000);

        // Stage 1 does not start earlier than pipeline
        Assert.assertFalse(info0.getStartInstant().isBefore(info2.getStartInstant()));
        
        // Pipeline does not end earlier than stage 2
        Assert.assertFalse(info2.getCompleteInstant().isBefore(info1.getCompleteInstant()));

        // Check that we're getting ISO 8601 Z time out from toString
        // Java 8 gives a result with three sub-second digits (millisecond precision)
        // Java 9 gives six digits (microsecond precision)
        // Accept anything with at least three, which is what we used to get from
        // Joda-Time DateTime values.
        final String zuluPattern =
                "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d\\d*Z";
        for (final ComponentInfo c : infos) {
            final String startString = c.getStartInstant().toString();
            Assert.assertTrue(startString.matches(zuluPattern), "start: " + startString);
            final String completeString = c.getCompleteInstant().toString();
            Assert.assertTrue(completeString.matches(zuluPattern), "complete: " + completeString);
        }
    }

}
