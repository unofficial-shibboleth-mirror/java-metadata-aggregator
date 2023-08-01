/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.SimpleItemCollectionFactory;
import net.shibboleth.metadata.testing.CountingStage;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.metadata.testing.TerminatingStage;
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

    // Test deprecated property setter and getter. The internal representation is in
    // terms of the new record type, and the code is converting on the way in and back out.
    // Lets make sure that is, at least plausibly, working.
    @SuppressWarnings("removal")
    @Test public void testPipelineAndSelectionStrategies() {
        PipelineDemultiplexerStage<Object> stage = new PipelineDemultiplexerStage<>();

        // Test with an empty list. It should trivially compare equal to the generated empty list
        final List<Pair<Pipeline<Object>, Predicate<Item<Object>>>> pass = new ArrayList<>();
        stage.setPipelineAndSelectionStrategies(pass);
        Assert.assertEquals(stage.getPipelineAndSelectionStrategies(), pass);

        // A little trickier: a singleton with a couple of empty pipelines and always-boolean strategy
        // The implementation will reconstruct the original List, although
        // it will not be identical it should compare equal with what we put in.
        final Predicate<Item<Object>> truthy = x -> true;
        final Predicate<Item<Object>> falsey = x -> false;
        final List<Pair<Pipeline<Object>, Predicate<Item<Object>>>> pass2 = CollectionSupport.listOf(
                new Pair<>(new SimplePipeline<Object>(), truthy),
                new Pair<>(new SimplePipeline<Object>(), falsey)
                );
        stage.setPipelineAndSelectionStrategies(pass2);
        Assert.assertEquals(stage.getPipelineAndSelectionStrategies(), pass2);
    }

    @Test public void testPipelinesAndStrategies() {
        PipelineDemultiplexerStage<Object> stage = new PipelineDemultiplexerStage<>();

        // Test with an empty list. It should trivially compare equal to the generated empty list
        final List<PipelineAndStrategy<Object>> pass = new ArrayList<>();
        stage.setPipelinesAndStrategies(pass);
        Assert.assertEquals(stage.getPipelinesAndStrategies(), pass);

        // A little trickier: a singleton with a couple of empty pipelines and always-boolean strategy
        // The implementation will reconstruct the original List, although
        // it will not be identical it should compare equal with what we put in.
        final Predicate<Item<Object>> truthy = x -> true;
        final Predicate<Item<Object>> falsey = x -> false;
        final @Nonnull List<PipelineAndStrategy<Object>> pass2 = CollectionSupport.listOf(
                new PipelineAndStrategy<>(new SimplePipeline<Object>(), truthy),
                new PipelineAndStrategy<>(new SimplePipeline<Object>(), falsey)
                );
        System.out.println("..." + pass2.get(0).getClass().getCanonicalName());
        stage.setPipelinesAndStrategies(pass2);
        Assert.assertEquals(stage.getPipelinesAndStrategies(), pass2);
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
        stage.setPipelinesAndStrategies(CollectionSupport.listOf(
                new PipelineAndStrategy<>(pipeline, x -> true)));
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
        stage.setPipelinesAndStrategies(CollectionSupport.listOf(
                new PipelineAndStrategy<>(pipeline, x -> true)));
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
        stage.setPipelinesAndStrategies(CollectionSupport.listOf(
                new PipelineAndStrategy<>(pipeline, x -> true)));
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
