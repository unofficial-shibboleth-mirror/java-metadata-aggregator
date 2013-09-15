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

import net.shibboleth.metadata.MockItem;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class SimplePipelineTest {

    @Test public void testInitialize() throws Exception {
        List<? extends Stage<MockItem>> stages = buildStages();

        SimplePipeline<MockItem> pipeline = new SimplePipeline<MockItem>();
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
            pipeline = new SimplePipeline<MockItem>();
            pipeline.setStages(stages);
            pipeline.initialize();
            Assert.fail();
        } catch (ComponentInitializationException e) {
            // expected this
        }

        try {
            pipeline = new SimplePipeline<MockItem>();
            pipeline.setId("");
            pipeline.setStages(stages);
            pipeline.initialize();
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected this
        }
    }

    @Test public void testExecution() throws Exception {
        List<? extends Stage<MockItem>> stages = buildStages();

        SimplePipeline<MockItem> pipeline = new SimplePipeline<MockItem>();
        pipeline.setId("test");
        pipeline.setStages(stages);
        pipeline.initialize();

        final List<MockItem> metadata = new ArrayList<>();
        pipeline.execute(metadata);
        Assert.assertEquals(metadata.size(), 2);

        Assert.assertEquals(((CountingStage) stages.get(1)).getInvocationCount(), 1);
        Assert.assertEquals(((CountingStage) stages.get(2)).getInvocationCount(), 1);

        MockItem md = metadata.iterator().next();
        Assert.assertTrue(md.getItemMetadata().containsKey(ComponentInfo.class));
        Assert.assertEquals(md.getItemMetadata().values().size(), 2);
        Assert.assertTrue(md.getItemMetadata().containsKey(ComponentInfo.class));

        try {
            List<? extends Stage<MockItem>> pipelineStages = pipeline.getStages();
            pipelineStages.clear();
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // expected this
        }

        metadata.clear();
        pipeline.execute(metadata);
        Assert.assertEquals(metadata.size(), 2);
        Assert.assertEquals(((CountingStage) stages.get(1)).getInvocationCount(), 2);
        Assert.assertEquals(((CountingStage) stages.get(2)).getInvocationCount(), 2);
    }

    protected List<? extends Stage<MockItem>> buildStages() {
        MockItem md1 = new MockItem("one");
        MockItem md2 = new MockItem("two");

        StaticItemSourceStage<MockItem> source = new StaticItemSourceStage<MockItem>();
        source.setId("src");
        source.setSourceItems(Lists.newArrayList(md1, md2));

        CountingStage<MockItem> stage1 = new CountingStage<MockItem>();
        CountingStage<MockItem> stage2 = new CountingStage<MockItem>();

        return Lists.newArrayList(source, stage1, stage2);
    }
}