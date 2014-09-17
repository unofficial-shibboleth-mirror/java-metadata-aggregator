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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.SimpleItemCollectionFactory;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/** Unit test of {@link SplitMergeStage}. */
public class SplitMergeStageTest {

    @Test public void testCollectionFactory() {
        SplitMergeStage<Object> stage = new SplitMergeStage<>();

        SimpleItemCollectionFactory<Object> factory = new SimpleItemCollectionFactory<>();
        stage.setCollectionFactory(factory);
        Assert.assertEquals(stage.getCollectionFactory(), factory);
    }

    @Test public void testExecutorService() {
        SplitMergeStage<Object> stage = new SplitMergeStage<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        stage.setExecutorService(executor);
        Assert.assertEquals(stage.getExecutorService(), executor);
    }

    @Test public void testNonselectedItemPipeline() {
        SplitMergeStage<Object> stage = new SplitMergeStage<>();

        SimplePipeline<Object> pipeline = new SimplePipeline<>();
        stage.setNonselectedItemPipeline(pipeline);
        Assert.assertSame(stage.getNonselectedItemPipeline(), pipeline);
    }

    @Test public void testSelectedItemPipeline() {
        SplitMergeStage<Object> stage = new SplitMergeStage<>();

        SimplePipeline<Object> pipeline = new SimplePipeline<>();
        stage.setSelectedItemPipeline(pipeline);
        Assert.assertSame(stage.getSelectedItemPipeline(), pipeline);
    }

    @Test public void testSelectionStrategy() {
        SplitMergeStage<Object> stage = new SplitMergeStage<>();

        stage.setSelectionStrategy(Predicates.<Item<Object>>alwaysTrue());
        Assert.assertEquals(stage.getSelectionStrategy(), Predicates.alwaysTrue());
    }

    @Test public void testInitialization() throws Exception {
        SimplePipeline<Object> pipeline = new SimplePipeline<>();
        pipeline.setId("pipeline");

        SplitMergeStage<Object> stage;

        stage = new SplitMergeStage<>();
        stage.setId("test");
        stage.setNonselectedItemPipeline(pipeline);
        stage.setSelectedItemPipeline(pipeline);
        stage.setSelectionStrategy(Predicates.<Item<Object>>alwaysTrue());
        stage.initialize();
        Assert.assertNotNull(stage.getCollectionFactory());
        Assert.assertNotNull(stage.getExecutorService());

        stage = new SplitMergeStage<>();
        stage.setId("test");
        stage.setSelectedItemPipeline(pipeline);
        stage.setSelectionStrategy(Predicates.<Item<Object>>alwaysTrue());
        stage.initialize();

        stage = new SplitMergeStage<>();
        stage.setId("test");
        stage.setNonselectedItemPipeline(pipeline);
        stage.setSelectionStrategy(Predicates.<Item<Object>>alwaysTrue());
        stage.initialize();

        try {
            stage = new SplitMergeStage<>();
            stage.setId("test");
            stage.setSelectionStrategy(Predicates.<Item<Object>>alwaysTrue());
            stage.initialize();
            Assert.fail();
        } catch (ComponentInitializationException e) {
            // expected this
        }
    }

    @Test public void testExecute() throws Exception {
        SimplePipeline<String> selectedPipeline = new SimplePipeline<>();
        selectedPipeline.setId("selectedPipeline");
        CountingStage<String> selectedCount = new CountingStage<>();
        selectedPipeline.setStages(Collections.<Stage<String>>singletonList(selectedCount));

        SimplePipeline<String> nonselectedPipeline = new SimplePipeline<>();
        nonselectedPipeline.setId("nonselectedPipeline");
        CountingStage<String> nonselectedCount = new CountingStage<>();
        nonselectedPipeline.setStages(Collections.<Stage<String>>singletonList(nonselectedCount));

        MockItem item1 = new MockItem("one");
        MockItem item2 = new MockItem("two");
        MockItem item3 = new MockItem("three");
        final List<Item<String>> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        items.add(item3);

        SplitMergeStage<String> stage = new SplitMergeStage<>();
        stage.setId("test");
        stage.setSelectionStrategy(Predicates.<Item<String>>alwaysTrue());
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
    
    /** Tests case where the selected items pipeline throws a {@link TerminationException}. */
    @Test public void testThrowSelected() throws Exception {
        final SimplePipeline<String> selectedPipeline = new SimplePipeline<>();
        selectedPipeline.setId("selectedPipeline");
        final TerminatingStage<String> selectedTerm = new TerminatingStage<>();
        selectedPipeline.setStages(Collections.<Stage<String>>singletonList(selectedTerm));

        SimplePipeline<String> nonselectedPipeline = new SimplePipeline<>();
        nonselectedPipeline.setId("nonselectedPipeline");
        CountingStage<String> nonselectedCount = new CountingStage<>();
        nonselectedPipeline.setStages(Collections.<Stage<String>>singletonList(nonselectedCount));

        MockItem item1 = new MockItem("one");
        MockItem item2 = new MockItem("two");
        MockItem item3 = new MockItem("three");
        final List<Item<String>> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        items.add(item3);

        SplitMergeStage<String> stage = new SplitMergeStage<>();
        stage.setId("test");
        stage.setSelectionStrategy(Predicates.<Item<String>>alwaysTrue());
        stage.setNonselectedItemPipeline(nonselectedPipeline);
        stage.setSelectedItemPipeline(selectedPipeline);
        stage.initialize();

        try {
            stage.execute(items);
            Assert.fail("did not throw expected exception");
        } catch (TerminationException e) {
            // this was expected
        }
    }
    
    /** Tests case where the nonselected items pipeline throws a {@link TerminationException}. */
    @Test public void testThrowNonselected() throws Exception {
        final SimplePipeline<String> selectedPipeline = new SimplePipeline<>();
        selectedPipeline.setId("selectedPipeline");
        CountingStage<String> selectedCount = new CountingStage<>();
        selectedPipeline.setStages(Collections.<Stage<String>>singletonList(selectedCount));

        SimplePipeline<String> nonselectedPipeline = new SimplePipeline<>();
        nonselectedPipeline.setId("nonselectedPipeline");
        final TerminatingStage<String> selectedTerm = new TerminatingStage<>();
        nonselectedPipeline.setStages(Collections.<Stage<String>>singletonList(selectedTerm));

        MockItem item1 = new MockItem("one");
        MockItem item2 = new MockItem("two");
        MockItem item3 = new MockItem("three");
        final List<Item<String>> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        items.add(item3);

        final SplitMergeStage<String> stage = new SplitMergeStage<>();
        stage.setId("test");
        stage.setSelectionStrategy(Predicates.<Item<String>>alwaysTrue());
        stage.setNonselectedItemPipeline(nonselectedPipeline);
        stage.setSelectedItemPipeline(selectedPipeline);
        stage.initialize();

        try {
            stage.execute(items);
            Assert.fail("did not throw expected exception");
        } catch (TerminationException e) {
            // this was expected
        }
    }

    @Test
    public void testMDA140selected() throws Exception {
        // make some items. we will send "one" down the selected pipeline, the rest down the nonselected one
        final MockItem item1 = new MockItem("one");
        final MockItem item2 = new MockItem("two");
        final MockItem item3 = new MockItem("three");
        final List<Item<String>> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        items.add(item3);

        final SimplePipeline<String> selectedPipeline = new SimplePipeline<>();
        selectedPipeline.setId("selected");
        selectedPipeline.initialize();
        
        final SplitMergeStage<String> stage = new SplitMergeStage<>();
        stage.setId("test");
        stage.setSelectionStrategy(new Predicate<Item<String>>(){
            @Override
            public boolean apply(Item<String> input) {
                return input.unwrap().equals("one");
            }
            
        });
        stage.setSelectedItemPipeline(selectedPipeline);
        stage.initialize();
        
        stage.execute(items);
        Assert.assertEquals(3, items.size());
    }

    @Test
    public void testMDA140nonselected() throws Exception {
        // make some items. we will send "one" down the selected pipeline, the rest down the nonselected one
        final MockItem item1 = new MockItem("one");
        final MockItem item2 = new MockItem("two");
        final MockItem item3 = new MockItem("three");
        final List<Item<String>> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        items.add(item3);

        final SimplePipeline<String> nonselectedPipeline = new SimplePipeline<>();
        nonselectedPipeline.setId("nonselected");
        nonselectedPipeline.initialize();
        
        final SplitMergeStage<String> stage = new SplitMergeStage<>();
        stage.setId("test");
        stage.setSelectionStrategy(new Predicate<Item<String>>(){
            @Override
            public boolean apply(Item<String> input) {
                return input.unwrap().equals("one");
            }
            
        });
        stage.setNonselectedItemPipeline(nonselectedPipeline);
        stage.initialize();
        
        stage.execute(items);
        Assert.assertEquals(3, items.size());
    }
}