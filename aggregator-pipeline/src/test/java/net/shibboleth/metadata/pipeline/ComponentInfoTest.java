
package net.shibboleth.metadata.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;

public class ComponentInfoTest {

    @Test
    public void testBasicOperation() throws Exception {
        final Item<String> item = new MockItem("test");
        final List<Item<String>> items = new ArrayList<>();
        items.add(item);
        final CompositeStage<String> comp1 = new CompositeStage<>();
        comp1.setId("comp1");
        comp1.initialize();
        final CompositeStage<String> comp2 = new CompositeStage<>();
        comp2.setId("comp2");
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

        Assert.assertSame(infos.get(0).getComponentType(), CompositeStage.class, "0");
        Assert.assertEquals(infos.get(0).getComponentId(), "comp1", "0");

        Assert.assertSame(infos.get(1).getComponentType(), CompositeStage.class, "1");
        Assert.assertEquals(infos.get(1).getComponentId(), "comp2", "1");

        Assert.assertSame(infos.get(2).getComponentType(), SimplePipeline.class, "2");
        Assert.assertEquals(infos.get(2).getComponentId(), "pipe", "2");

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
