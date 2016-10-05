/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.shibboleth.metadata.DeduplicatingItemIdMergeStrategy;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.MockItem;

import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link PipelineMergeStage} unit test. */
public class PipelineMergeStageTest {

    private <T> List<T> newSingletonList(T element) {
        final List<T> list = new ArrayList<>();
        list.add(element);
        return list;
    }
    
    private <T> List<T> newTwoElementList(T element1, T element2) {
        final List<T> list = new ArrayList<>();
        list.add(element1);
        list.add(element2);
        return list;
    }

    @Test public void test() throws Exception {
        Item<String> md1 = new MockItem("one");
        StaticItemSourceStage<String> source1 = new StaticItemSourceStage<>();
        source1.setId("src1");
        source1.setSourceItems(newSingletonList(md1));
        SimplePipeline<String> pipeline1 = new SimplePipeline<>();
        pipeline1.setId("p1");
        pipeline1.setStages(newSingletonList((Stage<String>) source1));

        Item<String> md2 = new MockItem("two");
        StaticItemSourceStage<String> source2 = new StaticItemSourceStage<>();
        source2.setId("src2");
        source2.setSourceItems(newSingletonList(md2));
        SimplePipeline<String> pipeline2 = new SimplePipeline<>();
        pipeline2.setId("p2");
        pipeline2.setStages(newSingletonList((Stage<String>) source2));

        final Collection<Pipeline<String>> joinedPipelines = new ArrayList<>();
        joinedPipelines.add(pipeline1);
        joinedPipelines.add(pipeline2);

        PipelineMergeStage<String> joinSource = new PipelineMergeStage<>();
        joinSource.setId("joinSource");
        joinSource.setMergedPipelines(newTwoElementList(pipeline1, pipeline2));

        Assert.assertFalse(joinSource.isInitialized());
        Assert.assertFalse(pipeline1.isInitialized());
        Assert.assertFalse(pipeline2.isInitialized());

        joinSource.initialize();
        Assert.assertTrue(joinSource.isInitialized());
        Assert.assertTrue(pipeline1.isInitialized());
        Assert.assertTrue(pipeline2.isInitialized());

        final ArrayList<Item<String>> metadataCollection = new ArrayList<>();
        joinSource.execute(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 2);

        boolean md1CloneMatch = false;
        boolean md2CloneMatch = false;
        for (Item<?> metadata : metadataCollection) {
            if ("one".equals(metadata.unwrap())) {
                md1CloneMatch = true;
                Assert.assertFalse(metadata == md1);
            } else if ("two".equals(metadata.unwrap())) {
                md2CloneMatch = true;
                Assert.assertFalse(metadata == md2);
            }
            // two ComponentInfo: one from the pipeline, one from the static inject stage, one from the join stage
            Assert.assertEquals(metadata.getItemMetadata().values().size(), 3);
        }

        Assert.assertTrue(md1CloneMatch);
        Assert.assertTrue(md2CloneMatch);
    }

    @Test public void testDediplicatingItemIdMergeStrategySingleSource() {
        DeduplicatingItemIdMergeStrategy strategy = new DeduplicatingItemIdMergeStrategy();

        final List<Item<String>> target = new ArrayList<>();
        MockItem item1 = new MockItem("item1");
        target.add(item1);
        MockItem item2 = new MockItem("item2");
        item2.getItemMetadata().put(new ItemId("itemA"));
        target.add(item2);
        MockItem item3 = new MockItem("item3");
        item3.getItemMetadata().put(new ItemId("itemB"));
        item3.getItemMetadata().put(new ItemId("itemC"));
        target.add(item3);

        final List<Item<String>> source1 = new ArrayList<>();
        MockItem item4 = new MockItem("item4");
        item4.getItemMetadata().put(new ItemId("itemD"));
        source1.add(item4);
        MockItem item5 = new MockItem("item5");
        item5.getItemMetadata().put(new ItemId("itemA"));
        source1.add(item5);
        MockItem item6 = new MockItem("item6");
        source1.add(item6);

        final List<Collection<Item<String>>> sources = new ArrayList<>();
        sources.add(source1);
        
        strategy.mergeCollection(target, sources);
        Assert.assertTrue(target.contains(item1));
        Assert.assertTrue(target.contains(item2));
        Assert.assertTrue(target.contains(item3));
        Assert.assertTrue(target.contains(item4));
        Assert.assertFalse(target.contains(item5));
        Assert.assertTrue(target.contains(item6));
        Assert.assertEquals(target.size(), 5);
    }

    @Test public void testDediplicatingItemIdMergeStrategyMultipleSource() {
        DeduplicatingItemIdMergeStrategy strategy = new DeduplicatingItemIdMergeStrategy();

        final List<Item<String>> target = new ArrayList<>();
        MockItem item1 = new MockItem("item1");
        target.add(item1);
        MockItem item2 = new MockItem("item2");
        item2.getItemMetadata().put(new ItemId("itemA"));
        target.add(item2);
        MockItem item3 = new MockItem("item3");
        item3.getItemMetadata().put(new ItemId("itemB"));
        item3.getItemMetadata().put(new ItemId("itemC"));
        target.add(item3);

        final List<Item<String>> source1 = new ArrayList<>();
        MockItem item4 = new MockItem("item4");
        item4.getItemMetadata().put(new ItemId("itemD"));
        source1.add(item4);
        MockItem item5 = new MockItem("item5");
        item5.getItemMetadata().put(new ItemId("itemA"));
        source1.add(item5);
        MockItem item6 = new MockItem("item6");
        source1.add(item6);

        final List<Item<String>> source2 = new ArrayList<>();
        MockItem item7 = new MockItem("item7");
        item7.getItemMetadata().put(new ItemId("itemD"));
        source2.add(item7);
        MockItem item8 = new MockItem("item8");
        source2.add(item8);
        MockItem item9 = new MockItem("item9");
        item9.getItemMetadata().put(new ItemId("itemA"));
        source2.add(item9);

        final List<Collection<Item<String>>> sources = new ArrayList<>();
        sources.add(source1);
        sources.add(source2);
        
        strategy.mergeCollection(target, sources);
        Assert.assertTrue(target.contains(item1));
        Assert.assertTrue(target.contains(item2));
        Assert.assertTrue(target.contains(item3));
        Assert.assertTrue(target.contains(item4));
        Assert.assertFalse(target.contains(item5));
        Assert.assertTrue(target.contains(item6));
        Assert.assertFalse(target.contains(item7));
        Assert.assertTrue(target.contains(item8));
        Assert.assertFalse(target.contains(item9));
        Assert.assertEquals(target.size(), 6);
    }
    
    /** Tests the case where one of the pipelines throws an exception. */
    @Test public void testThrow() throws Exception {
        Item<String> md1 = new MockItem("one");
        StaticItemSourceStage<String> source1 = new StaticItemSourceStage<>();
        source1.setId("src1");
        source1.setSourceItems(newSingletonList(md1));
        SimplePipeline<String> pipeline1 = new SimplePipeline<>();
        pipeline1.setId("p1");
        pipeline1.setStages(newSingletonList((Stage<String>) source1));

        final Item<String> md2 = new MockItem("two");
        final StaticItemSourceStage<String> source2 = new StaticItemSourceStage<>();
        source2.setId("src2");
        source2.setSourceItems(newSingletonList(md2));
        final TerminatingStage<String> term = new TerminatingStage<>();
        SimplePipeline<String> pipeline2 = new SimplePipeline<>();
        pipeline2.setId("p2");
        final List<Stage<String>> stages = new ArrayList<>();
        stages.add(source2);
        stages.add(term);
        pipeline2.setStages(stages);

        final Collection<Pipeline<String>> joinedPipelines = new ArrayList<>();
        joinedPipelines.add(pipeline1);
        joinedPipelines.add(pipeline2);

        PipelineMergeStage<String> joinSource = new PipelineMergeStage<>();
        joinSource.setId("joinSource");
        joinSource.setMergedPipelines(newTwoElementList(pipeline1, pipeline2));

        Assert.assertFalse(joinSource.isInitialized());
        Assert.assertFalse(pipeline1.isInitialized());
        Assert.assertFalse(pipeline2.isInitialized());

        joinSource.initialize();
        Assert.assertTrue(joinSource.isInitialized());
        Assert.assertTrue(pipeline1.isInitialized());
        Assert.assertTrue(pipeline2.isInitialized());

        final ArrayList<Item<String>> metadataCollection = new ArrayList<>();
        try {
            joinSource.execute(metadataCollection);
            Assert.fail("expected exception not thrown");
        } catch (TerminationException e) {
            // this is expected
        }
    }

}