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

import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class SimplePipelineTest {

    @Test public void testInitialize() throws Exception {
        List<? extends Stage<MockItem>> stages = buildStages();

        SimplePipeline<MockItem> pipeline = new SimplePipeline<MockItem>();
        pipeline.setId(" test ");
        pipeline.setStages(stages);
        assert "test".equals(pipeline.getId());
        assert pipeline.getStages() != stages;
        assert pipeline.getStages().size() == 3;
        assert pipeline.getStages().containsAll(stages);
        assert !pipeline.getStages().get(0).isInitialized();
        assert !pipeline.getStages().get(1).isInitialized();
        assert pipeline.getInitializationInstant() == null;

        pipeline.initialize();
        assert "test".equals(pipeline.getId());
        assert pipeline.getStages() != stages;
        assert pipeline.getStages().size() == 3;
        assert pipeline.getStages().containsAll(stages);
        assert pipeline.getStages().get(0).isInitialized();
        assert pipeline.getStages().get(1).isInitialized();
        assert pipeline.getStages().get(2).isInitialized();
        assert pipeline.getInitializationInstant() != null;

        try {
            pipeline = new SimplePipeline<MockItem>();
            pipeline.setStages(stages);
            pipeline.initialize();
            throw new AssertionError();
        } catch (ComponentInitializationException e) {
            // expected this
        }

        try {
            pipeline = new SimplePipeline<MockItem>();
            pipeline.setId("");
            pipeline.setStages(stages);
            pipeline.initialize();
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // expected this
        }
    }

    @Test public void testExecution() throws Exception {
        List<? extends Stage<MockItem>> stages = buildStages();

        SimplePipeline<MockItem> pipeline = new SimplePipeline<MockItem>();
        pipeline.setId("test");
        pipeline.setStages(stages);

        ArrayList<MockItem> metadata = new ArrayList<MockItem>();
        pipeline.execute(metadata);
        assert metadata.size() == 2;

        assert ((CountingStage) stages.get(1)).getInvocationCount() == 1;
        assert ((CountingStage) stages.get(2)).getInvocationCount() == 1;

        MockItem md = metadata.iterator().next();
        assert md.getItemMetadata().containsKey(ComponentInfo.class);
        assert md.getItemMetadata().values().size() == 2;
        assert md.getItemMetadata().containsKey(ComponentInfo.class);

        try {
            List<? extends Stage<MockItem>> pipelineStages = pipeline.getStages();
            pipelineStages.clear();
            throw new AssertionError();
        } catch (UnsupportedOperationException e) {
            // expected this
        }

        metadata = new ArrayList<MockItem>();
        pipeline.execute(metadata);
        assert metadata.size() == 2;
        assert ((CountingStage) stages.get(1)).getInvocationCount() == 2;
        assert ((CountingStage) stages.get(2)).getInvocationCount() == 2;
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