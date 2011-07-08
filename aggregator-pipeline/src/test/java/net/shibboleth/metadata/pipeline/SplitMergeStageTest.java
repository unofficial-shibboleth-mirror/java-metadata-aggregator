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
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.AlwaysItemSelectionStrategy;
import net.shibboleth.metadata.SimpleItemCollectionFactory;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test of {@link SplitMergeStage}. */
public class SplitMergeStageTest {

    @Test
    public void testCollectionFactory() {
        SplitMergeStage stage = new SplitMergeStage();

        SimpleItemCollectionFactory factory = new SimpleItemCollectionFactory();
        stage.setCollectionFactory(factory);
        Assert.assertEquals(stage.getCollectionFactory(), factory);
    }

    @Test
    public void testExecutorService() {
        SplitMergeStage stage = new SplitMergeStage();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        stage.setExecutorService(executor);
        Assert.assertEquals(stage.getExecutorService(), executor);
    }

    @Test
    public void testNonselectedItemPipeline() {
        SplitMergeStage stage = new SplitMergeStage();

        SimplePipeline pipeline = new SimplePipeline();
        stage.setNonselectedItemPipeline(pipeline);
        Assert.assertSame(stage.getNonselectedItemPipeline(), pipeline);
    }

    @Test
    public void testSelectedItemPipeline() {
        SplitMergeStage stage = new SplitMergeStage();

        SimplePipeline pipeline = new SimplePipeline();
        stage.setSelectedItemPipeline(pipeline);
        Assert.assertSame(stage.getSelectedItemPipeline(), pipeline);
    }

    @Test
    public void testSelectionStrategy() {
        SplitMergeStage stage = new SplitMergeStage();

        AlwaysItemSelectionStrategy strategy = new AlwaysItemSelectionStrategy();
        stage.setSelectionStrategy(strategy);
        Assert.assertEquals(stage.getSelectionStrategy(), strategy);
    }

    @Test
    public void testInitialization() throws Exception {
        SimplePipeline pipeline = new SimplePipeline();
        pipeline.setId("pipeline");

        SplitMergeStage stage;

        stage = new SplitMergeStage();
        stage.setId("test");
        stage.setNonselectedItemPipeline(pipeline);
        stage.setSelectedItemPipeline(pipeline);
        stage.setSelectionStrategy(new AlwaysItemSelectionStrategy());
        stage.initialize();
        Assert.assertNotNull(stage.getCollectionFactory());
        Assert.assertNotNull(stage.getExecutorService());

        stage = new SplitMergeStage();
        stage.setId("test");
        stage.setSelectedItemPipeline(pipeline);
        stage.setSelectionStrategy(new AlwaysItemSelectionStrategy());
        stage.initialize();

        stage = new SplitMergeStage();
        stage.setId("test");
        stage.setNonselectedItemPipeline(pipeline);
        stage.setSelectionStrategy(new AlwaysItemSelectionStrategy());
        stage.initialize();

        try {
            stage = new SplitMergeStage();
            stage.setId("test");
            stage.setSelectionStrategy(new AlwaysItemSelectionStrategy());
            stage.initialize();
            Assert.fail();
        } catch (ComponentInitializationException e) {
            // expected this
        }

        try {
            stage = new SplitMergeStage();
            stage.setId("test");
            stage.setNonselectedItemPipeline(pipeline);
            stage.setSelectedItemPipeline(pipeline);
            stage.initialize();
            Assert.fail();
        } catch (ComponentInitializationException e) {
            // expected this
        }
    }

    @Test
    public void testExecute() throws Exception {
        SimplePipeline selectedPipeline = new SimplePipeline();
        selectedPipeline.setId("selectedPipeline");
        CountingStage selectedCount = new CountingStage();
        selectedPipeline.setStages(Collections.singletonList(selectedCount));

        SimplePipeline nonselectedPipeline = new SimplePipeline();
        nonselectedPipeline.setId("nonselectedPipeline");
        CountingStage nonselectedCount = new CountingStage();
        nonselectedPipeline.setStages(Collections.singletonList(nonselectedCount));

        MockItem item1 = new MockItem("one");
        MockItem item2 = new MockItem("two");
        MockItem item3 = new MockItem("three");
        ArrayList<MockItem> items = new ArrayList<MockItem>();
        items.add(item1);
        items.add(item2);
        items.add(item3);

        SplitMergeStage stage = new SplitMergeStage();
        stage.setId("test");
        stage.setSelectionStrategy(new AlwaysItemSelectionStrategy());
        stage.setNonselectedItemPipeline(nonselectedPipeline);
        stage.setSelectedItemPipeline(selectedPipeline);
        stage.initialize();

        stage.execute(items);

        Assert.assertEquals(selectedCount.getInvocationCount(), 1);
        Assert.assertEquals(selectedCount.getItemCount(), 3);
        Assert.assertEquals(nonselectedCount.getInvocationCount(), 1);
        Assert.assertEquals(nonselectedCount.getItemCount(), 0);
        Assert.assertEquals(items.size(), 3);
        Assert.assertTrue(items.contains(item1));
        Assert.assertTrue(items.contains(item2));
        Assert.assertTrue(items.contains(item3));
    }
}