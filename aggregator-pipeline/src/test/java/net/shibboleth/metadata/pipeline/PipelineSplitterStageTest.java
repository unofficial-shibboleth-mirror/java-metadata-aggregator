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
import net.shibboleth.metadata.SimpleItemCollectionFactory;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test of {@link PipelineSplitterStage}. */
public class PipelineSplitterStageTest {

    @Test
    public void testCollectionFactory() {
        PipelineSplitterStage stage = new PipelineSplitterStage();

        SimpleItemCollectionFactory factory = new SimpleItemCollectionFactory();
        stage.setCollectionFactory(factory);
        Assert.assertEquals(stage.getCollectionFactory(), factory);
    }

    @Test
    public void testExecutorService() {
        PipelineSplitterStage stage = new PipelineSplitterStage();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        stage.setExecutorService(executor);
        Assert.assertEquals(stage.getExecutorService(), executor);
    }

    @Test
    public void testNonselectedItemPipeline() {
        PipelineSplitterStage stage = new PipelineSplitterStage();

        SimplePipeline pipeline = new SimplePipeline();
        stage.setNonselectedItemPipeline(pipeline);
        Assert.assertSame(stage.getNonselectedItemPipeline(), pipeline);
    }

    @Test
    public void testSelectedItemPipeline() {
        PipelineSplitterStage stage = new PipelineSplitterStage();

        SimplePipeline pipeline = new SimplePipeline();
        stage.setSelectedItemPipeline(pipeline);
        Assert.assertSame(stage.getSelectedItemPipeline(), pipeline);
    }

    @Test
    public void testSelectionStrategy() {
        PipelineSplitterStage stage = new PipelineSplitterStage();

        MockItemSelectionStrategy strategy = new MockItemSelectionStrategy();
        stage.setSelectionStrategy(strategy);
        Assert.assertEquals(stage.getSelectionStrategy(), strategy);
    }

    @Test
    public void testWaitingForNonselectedItemPipeline() {
        PipelineSplitterStage stage = new PipelineSplitterStage();

        stage.setWaitingForNonselectedItemPipeline(true);
        Assert.assertTrue(stage.isWaitingForNonselectedItemPipeline());
    }

    @Test
    public void testWaitingForSelectedItemPipeline() {
        PipelineSplitterStage stage = new PipelineSplitterStage();

        stage.setWaitingForSelectedItemPipeline(true);
        Assert.assertTrue(stage.isWaitingForSelectedItemPipeline());
    }

    @Test
    public void testInitialization() throws Exception {
        SimplePipeline pipeline = new SimplePipeline();
        pipeline.setId("pipeline");

        PipelineSplitterStage stage;

        stage = new PipelineSplitterStage();
        stage.setId("test");
        stage.setNonselectedItemPipeline(pipeline);
        stage.setSelectedItemPipeline(pipeline);
        stage.setSelectionStrategy(new MockItemSelectionStrategy());
        stage.initialize();
        Assert.assertNotNull(stage.getCollectionFactory());
        Assert.assertNotNull(stage.getExecutorService());

        stage = new PipelineSplitterStage();
        stage.setId("test");
        stage.setSelectedItemPipeline(pipeline);
        stage.setSelectionStrategy(new MockItemSelectionStrategy());
        stage.initialize();

        stage = new PipelineSplitterStage();
        stage.setId("test");
        stage.setNonselectedItemPipeline(pipeline);
        stage.setSelectionStrategy(new MockItemSelectionStrategy());
        stage.initialize();

        try {
            stage = new PipelineSplitterStage();
            stage.setId("test");
            stage.setSelectionStrategy(new MockItemSelectionStrategy());
            stage.initialize();
            Assert.fail();
        } catch (ComponentInitializationException e) {
            // expected this
        }

        try {
            stage = new PipelineSplitterStage();
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
    public void testExecute() throws Exception{
        SimplePipeline selectedPipeline = new SimplePipeline();
        selectedPipeline.setId("selectedPipeline");
        CountingStage selectedCount = new CountingStage();
        selectedPipeline.setStages(Collections.singletonList(selectedCount));
        
        SimplePipeline nonselectedPipeline = new SimplePipeline();
        nonselectedPipeline.setId("nonselectedPipeline");
        CountingStage nonselectedCount = new CountingStage();
        nonselectedPipeline.setStages(Collections.singletonList(nonselectedCount));
        
        ArrayList<MockItem> items = new ArrayList<MockItem>();
        items.add(new MockItem("one"));
        items.add(new MockItem("two"));
        items.add(new MockItem("three"));
        
        PipelineSplitterStage stage = new PipelineSplitterStage();
        stage.setId("test");
        stage.setSelectionStrategy(new MockItemSelectionStrategy());
        stage.setNonselectedItemPipeline(nonselectedPipeline);
        stage.setWaitingForNonselectedItemPipeline(true);
        stage.setSelectedItemPipeline(selectedPipeline);
        stage.setWaitingForSelectedItemPipeline(true);
        stage.initialize();
        
        stage.execute(items);
        
        Assert.assertEquals(selectedCount.getCount(), 1);
        Assert.assertEquals(nonselectedCount.getCount(), 1);
    }
}