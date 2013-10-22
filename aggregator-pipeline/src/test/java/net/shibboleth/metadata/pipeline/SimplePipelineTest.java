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

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SimplePipelineTest {

    @Test public void testInitialize() throws Exception {
        final List<Stage<String>> stages = buildStages();

        SimplePipeline<String> pipeline = new SimplePipeline<>();
        pipeline.setId(" test ");
        pipeline.setStages(stages);
        Assert.assertEquals(pipeline.getId(), "test");
        Assert.assertFalse(pipeline.getStages() == stages);
        Assert.assertEquals(pipeline.getStages().size(), 3);
        Assert.assertTrue(pipeline.getStages().containsAll(stages));
        Assert.assertFalse(pipeline.getStages().get(0).isInitialized());
        Assert.assertFalse(pipeline.getStages().get(1).isInitialized());

        pipeline.initialize();
        Assert.assertEquals(pipeline.getId(), "test");
        Assert.assertFalse(pipeline.getStages() == stages);
        Assert.assertEquals(pipeline.getStages().size(), 3);
        Assert.assertTrue(pipeline.getStages().containsAll(stages));
        Assert.assertTrue(pipeline.getStages().get(0).isInitialized());
        Assert.assertTrue(pipeline.getStages().get(1).isInitialized());
        Assert.assertTrue(pipeline.getStages().get(2).isInitialized());

        try {
            pipeline = new SimplePipeline<>();
            pipeline.setStages(stages);
            pipeline.initialize();
            Assert.fail();
        } catch (ComponentInitializationException e) {
            // expected this
        }

        try {
            pipeline = new SimplePipeline<>();
            pipeline.setId("");
            pipeline.setStages(stages);
            pipeline.initialize();
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

    @Test public void testExecution() throws Exception {
        final List<Stage<String>> stages = buildStages();

        SimplePipeline<String> pipeline = new SimplePipeline<>();
        pipeline.setId("test");
        pipeline.setStages(stages);
        pipeline.initialize();

        final List<Item<String>> metadata = new ArrayList<>();
        pipeline.execute(metadata);
        Assert.assertEquals(metadata.size(), 2);

        Assert.assertEquals(((CountingStage<String>) stages.get(1)).getInvocationCount(), 1);
        Assert.assertEquals(((CountingStage<String>) stages.get(2)).getInvocationCount(), 1);

        Item<String> md = metadata.iterator().next();
        Assert.assertTrue(md.getItemMetadata().containsKey(ComponentInfo.class));
        Assert.assertEquals(md.getItemMetadata().values().size(), 2);
        Assert.assertTrue(md.getItemMetadata().containsKey(ComponentInfo.class));

        try {
            List<Stage<String>> pipelineStages = pipeline.getStages();
            pipelineStages.clear();
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // expected this
        }

        metadata.clear();
        pipeline.execute(metadata);
        Assert.assertEquals(metadata.size(), 2);
        Assert.assertEquals(((CountingStage<String>) stages.get(1)).getInvocationCount(), 2);
        Assert.assertEquals(((CountingStage<String>) stages.get(2)).getInvocationCount(), 2);
    }

    protected List<Stage<String>> buildStages() {
        final Item<String> md1 = new MockItem("one");
        final Item<String> md2 = new MockItem("two");
        final List<Item<String>> items = new ArrayList<>();
        items.add(md1);
        items.add(md2);

        final StaticItemSourceStage<String> source = new StaticItemSourceStage<>();
        source.setId("src");
        source.setSourceItems(items);

        final CountingStage<String> stage1 = new CountingStage<>();
        final CountingStage<String> stage2 = new CountingStage<>();

        final List<Stage<String>> list = new ArrayList<>();
        list.add(source);
        list.add(stage1);
        list.add(stage2);
        return list;
    }
}