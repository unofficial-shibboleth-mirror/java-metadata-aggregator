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

import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.SimpleItemCollectionFactory;
import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/** Unit test of {@link PipelineDemultiplexerStage}. */
public class PipelineDemultiplexerStageTest {

    @Test public void testCollectionFactory() {
        PipelineDemultiplexerStage stage = new PipelineDemultiplexerStage();

        SimpleItemCollectionFactory factory = new SimpleItemCollectionFactory();
        stage.setCollectionFactory(factory);
        Assert.assertEquals(stage.getCollectionFactory(), factory);
    }

    @Test public void testExecutorService() {
        PipelineDemultiplexerStage stage = new PipelineDemultiplexerStage();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        stage.setExecutorService(executor);
        Assert.assertEquals(stage.getExecutorService(), executor);
    }

    @Test public void testPipelineAndSelectionStrategies() {
        PipelineDemultiplexerStage stage = new PipelineDemultiplexerStage();

        final List<Pair<Pipeline, Predicate>> pass = new ArrayList<>();
        stage.setPipelineAndSelectionStrategies(pass);
        Assert.assertEquals(stage.getPipelineAndSelectionStrategies(), pass);
    }

    @Test public void testWaitingForPipelines() {
        PipelineDemultiplexerStage stage = new PipelineDemultiplexerStage();

        stage.setWaitingForPipelines(true);
        Assert.assertTrue(stage.isWaitingForPipelines());
    }

    @Test public void testInitialize() throws Exception {
        SimplePipeline pipeline = new SimplePipeline();
        pipeline.setId("pipeline");

        PipelineDemultiplexerStage stage;

        stage = new PipelineDemultiplexerStage();
        stage.setId("test");
        stage.setPipelineAndSelectionStrategies(Collections.singletonList(new Pair<Pipeline, Predicate>(pipeline,
                Predicates.alwaysTrue())));
        stage.initialize();
        Assert.assertNotNull(stage.getCollectionFactory());
        Assert.assertNotNull(stage.getExecutorService());

        try {
            stage = new PipelineDemultiplexerStage();
            stage.setId("test");
            stage.initialize();
            Assert.fail();
        } catch (ComponentInitializationException e) {
            // expected this
        }
    }

    @Test public void testExecute() throws Exception {
        SimplePipeline pipeline = new SimplePipeline();
        pipeline.setId("selectedPipeline");
        CountingStage countStage = new CountingStage();
        pipeline.setStages(Collections.singletonList(countStage));

        final List<MockItem> items = new ArrayList<>();
        items.add(new MockItem("one"));
        items.add(new MockItem("two"));
        items.add(new MockItem("three"));

        PipelineDemultiplexerStage stage = new PipelineDemultiplexerStage();
        stage.setId("test");
        stage.setWaitingForPipelines(true);
        stage.setPipelineAndSelectionStrategies(Collections.singletonList(new Pair<Pipeline, Predicate>(pipeline,
                Predicates.alwaysTrue())));
        stage.initialize();

        stage.execute(items);

        Assert.assertEquals(countStage.getInvocationCount(), 1);
    }
}