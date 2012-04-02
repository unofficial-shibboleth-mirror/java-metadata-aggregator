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

import net.shibboleth.metadata.DeduplicatingItemIdMergeStrategy;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.MockItem;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/** {@link PipelineMergeStage} unit test. */
public class PipelineMergeStageTest {

    @Test public void test() throws Exception {
        MockItem md1 = new MockItem("one");
        StaticItemSourceStage<MockItem> source1 = new StaticItemSourceStage<MockItem>();
        source1.setId("src1");
        source1.setSourceItems(Lists.newArrayList(md1));
        SimplePipeline<MockItem> pipeline1 = new SimplePipeline<MockItem>();
        pipeline1.setId("p1");
        pipeline1.setStages(Lists.newArrayList((Stage<MockItem>) source1));

        MockItem md2 = new MockItem("two");
        StaticItemSourceStage<MockItem> source2 = new StaticItemSourceStage<MockItem>();
        source2.setId("src2");
        source2.setSourceItems(Lists.newArrayList(md2));
        SimplePipeline<MockItem> pipeline2 = new SimplePipeline<MockItem>();
        pipeline2.setId("p2");
        pipeline2.setStages(Lists.newArrayList((Stage<MockItem>) source2));

        Collection<Pipeline<MockItem>> joinedPipelines = new ArrayList<Pipeline<MockItem>>();
        joinedPipelines.add(pipeline1);
        joinedPipelines.add(pipeline2);

        PipelineMergeStage joinSource = new PipelineMergeStage();
        joinSource.setId("joinSource");
        joinSource.setMergedPipelines(Lists.newArrayList(pipeline1, pipeline2));

        Assert.assertFalse(joinSource.isInitialized());
        Assert.assertFalse(pipeline1.isInitialized());
        Assert.assertFalse(pipeline2.isInitialized());

        joinSource.initialize();
        Assert.assertTrue(joinSource.isInitialized());
        Assert.assertTrue(pipeline1.isInitialized());
        Assert.assertTrue(pipeline2.isInitialized());

        ArrayList<Item<?>> metadataCollection = new ArrayList<Item<?>>();
        joinSource.execute(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 2);

        boolean md1CloneMatch = false;
        boolean md2CloneMatch = false;
        for (Item<?> metadata : metadataCollection) {
            if ("one".equals(metadata.unwrap())) {
                md1CloneMatch = true;
                Assert.assertFalse(metadata != md1);
            } else if ("two".equals(metadata.unwrap())) {
                md2CloneMatch = true;
                Assert.assertFalse(metadata != md2);
            }
            // two ComponentInfo: one from the pipeline, one from the static inject stage, one from the join stage
            Assert.assertEquals(metadata.getItemMetadata().values().size(), 3);
        }

        Assert.assertTrue(md1CloneMatch);
        Assert.assertTrue(md2CloneMatch);
    }

    @Test public void testDediplicatingItemIdMergeStrategySingleSource() {
        DeduplicatingItemIdMergeStrategy strategy = new DeduplicatingItemIdMergeStrategy();

        ArrayList<Item<?>> target = new ArrayList<Item<?>>();
        MockItem item1 = new MockItem("item1");
        target.add(item1);
        MockItem item2 = new MockItem("item2");
        item2.getItemMetadata().put(new ItemId("itemA"));
        target.add(item2);
        MockItem item3 = new MockItem("item3");
        item3.getItemMetadata().put(new ItemId("itemB"));
        item3.getItemMetadata().put(new ItemId("itemC"));
        target.add(item3);

        ArrayList<Item<?>> source1 = new ArrayList<Item<?>>();
        MockItem item4 = new MockItem("item4");
        item4.getItemMetadata().put(new ItemId("itemD"));
        source1.add(item4);
        MockItem item5 = new MockItem("item5");
        item5.getItemMetadata().put(new ItemId("itemA"));
        source1.add(item5);
        MockItem item6 = new MockItem("item6");
        source1.add(item6);

        strategy.mergeCollection(target, source1);
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

        ArrayList<Item<?>> target = new ArrayList<Item<?>>();
        MockItem item1 = new MockItem("item1");
        target.add(item1);
        MockItem item2 = new MockItem("item2");
        item2.getItemMetadata().put(new ItemId("itemA"));
        target.add(item2);
        MockItem item3 = new MockItem("item3");
        item3.getItemMetadata().put(new ItemId("itemB"));
        item3.getItemMetadata().put(new ItemId("itemC"));
        target.add(item3);

        ArrayList<Item<?>> source1 = new ArrayList<Item<?>>();
        MockItem item4 = new MockItem("item4");
        item4.getItemMetadata().put(new ItemId("itemD"));
        source1.add(item4);
        MockItem item5 = new MockItem("item5");
        item5.getItemMetadata().put(new ItemId("itemA"));
        source1.add(item5);
        MockItem item6 = new MockItem("item6");
        source1.add(item6);

        ArrayList<Item<?>> source2 = new ArrayList<Item<?>>();
        MockItem item7 = new MockItem("item7");
        item7.getItemMetadata().put(new ItemId("itemD"));
        source2.add(item7);
        MockItem item8 = new MockItem("item8");
        source2.add(item8);
        MockItem item9 = new MockItem("item9");
        item9.getItemMetadata().put(new ItemId("itemA"));
        source2.add(item9);

        strategy.mergeCollection(target, source1, source2);
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
}