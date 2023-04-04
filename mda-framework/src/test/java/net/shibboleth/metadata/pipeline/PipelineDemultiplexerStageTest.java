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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.SimpleItemCollectionFactory;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.collection.Pair;
import net.shibboleth.shared.component.ComponentInitializationException;

/** Unit test of {@link PipelineDemultiplexerStage}. */
public class PipelineDemultiplexerStageTest {

    @Test public void testCollectionFactory() {
        PipelineDemultiplexerStage<Object> stage = new PipelineDemultiplexerStage<>();

        SimpleItemCollectionFactory<Object> factory = new SimpleItemCollectionFactory<>();
        stage.setCollectionFactory(factory);
        Assert.assertEquals(stage.getCollectionFactory(), factory);
    }

    @SuppressWarnings("removal")
    @Test public void testExecutorService() {
        PipelineDemultiplexerStage<Object> stage = new PipelineDemultiplexerStage<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        assert executor != null;
        stage.setExecutorService(executor);
        Assert.assertEquals(stage.getExecutorService(), executor);
    }

    @Test public void testExecutor() {
        PipelineDemultiplexerStage<Object> stage = new PipelineDemultiplexerStage<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        assert executor != null;
        stage.setExecutor(executor);
        Assert.assertEquals(stage.getExecutor(), executor);
    }

    @Test public void testPipelineAndSelectionStrategies() {
        PipelineDemultiplexerStage<Object> stage = new PipelineDemultiplexerStage<>();

        final List<Pair<Pipeline<Object>, Predicate<Item<Object>>>> pass = new ArrayList<>();
        stage.setPipelineAndSelectionStrategies(pass);
        Assert.assertEquals(stage.getPipelineAndSelectionStrategies(), pass);
    }

    @Test public void testWaitingForPipelines() {
        PipelineDemultiplexerStage<Object> stage = new PipelineDemultiplexerStage<>();

        stage.setWaitingForPipelines(true);
        Assert.assertTrue(stage.isWaitingForPipelines());
    }

    @Test public void testInitialize() throws Exception {
        SimplePipeline<String> pipeline = new SimplePipeline<>();
        pipeline.setId("pipeline");

        PipelineDemultiplexerStage<String> stage;

        stage = new PipelineDemultiplexerStage<>();
        stage.setId("test");
        stage.setPipelineAndSelectionStrategies(CollectionSupport.listOf(new Pair<Pipeline<String>, Predicate<Item<String>>>(pipeline,
                x -> true)));
        stage.initialize();
        Assert.assertNotNull(stage.getCollectionFactory());
        Assert.assertNotNull(stage.getExecutor());

        try {
            stage = new PipelineDemultiplexerStage<>();
            stage.setId("test");
            stage.initialize();
            Assert.fail();
        } catch (ComponentInitializationException e) {
            // expected this
        }
    }

    @Test public void testExecute() throws Exception {
        SimplePipeline<String> pipeline = new SimplePipeline<>();
        pipeline.setId("selectedPipeline");
        CountingStage<String> countStage = new CountingStage<>();
        pipeline.setStages(CollectionSupport.<Stage<String>>listOf(countStage));

        final List<Item<String>> items = new ArrayList<>();
        items.add(new MockItem("one"));
        items.add(new MockItem("two"));
        items.add(new MockItem("three"));

        PipelineDemultiplexerStage<String> stage = new PipelineDemultiplexerStage<>();
        stage.setId("test");
        stage.setWaitingForPipelines(true);
        stage.setPipelineAndSelectionStrategies(CollectionSupport.listOf(new Pair<Pipeline<String>, Predicate<Item<String>>>(pipeline,
                x -> true)));
        stage.initialize();

        stage.execute(items);

        Assert.assertEquals(countStage.getInvocationCount(), 1);
        stage.destroy();
    }
    
    @Test public void testThrow() throws Exception {
        final SimplePipeline<String> pipeline = new SimplePipeline<>();
        pipeline.setId("selectedPipeline");
        final TerminatingStage<String> terminatingStage = new TerminatingStage<>();
        pipeline.setStages(CollectionSupport.<Stage<String>>listOf(terminatingStage));

        final List<Item<String>> items = new ArrayList<>();
        items.add(new MockItem("one"));
        items.add(new MockItem("two"));
        items.add(new MockItem("three"));

        PipelineDemultiplexerStage<String> stage = new PipelineDemultiplexerStage<>();
        stage.setId("test");
        stage.setWaitingForPipelines(true);
        stage.setPipelineAndSelectionStrategies(CollectionSupport.listOf(new Pair<Pipeline<String>, Predicate<Item<String>>>(pipeline,
                x -> true)));
        stage.initialize();

        try {
            stage.execute(items);
            Assert.fail("expected exception to be thrown");
        } catch (TerminationException e) {
            // this was expected
        }
    }

    @Test public void testMDA206() throws Exception {
        final PipelineDemultiplexerStage<String> stage = new PipelineDemultiplexerStage<>();
        Assert.assertTrue(stage.isWaitingForPipelines());
    }

}
