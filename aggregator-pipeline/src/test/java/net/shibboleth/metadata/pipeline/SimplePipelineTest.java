/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import net.shibboleth.metadata.MockMetadata;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazyList;
import org.testng.annotations.Test;

public class SimplePipelineTest {

    @Test
    public void testInitialize() throws Exception {
        List<Stage<MockMetadata>> stages = buildStages();

        SimplePipeline<MockMetadata> pipeline = new SimplePipeline<MockMetadata>();
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
            pipeline = new SimplePipeline<MockMetadata>();
            pipeline.setStages(stages);
            pipeline.initialize();
            throw new AssertionError();
        } catch (ComponentInitializationException e) {
            // expected this
        }

        try {
            pipeline = new SimplePipeline<MockMetadata>();
            pipeline.setId("");
            pipeline.setStages(stages);
            pipeline.initialize();
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // expected this
        }
    }

    @Test
    public void testExecution() throws Exception {
        List<Stage<MockMetadata>> stages = buildStages();

        SimplePipeline<MockMetadata> pipeline = new SimplePipeline<MockMetadata>();
        pipeline.setId("test");
        pipeline.setStages(stages);

        ArrayList<MockMetadata> metadata = new ArrayList<MockMetadata>();
        pipeline.execute(metadata);
        assert metadata.size() == 2;

        assert ((CountingStage) stages.get(1)).getCount() == 1;
        assert ((CountingStage) stages.get(2)).getCount() == 1;

        MockMetadata md = metadata.iterator().next();
        assert md.getMetadataInfo().containsKey(ComponentInfo.class);
        assert md.getMetadataInfo().values().size() == 2;
        assert md.getMetadataInfo().containsKey(ComponentInfo.class);

        try {
            List<Stage<MockMetadata>> pipelineStages = pipeline.getStages();
            pipelineStages.clear();
            throw new AssertionError();
        } catch (UnsupportedOperationException e) {
            // expected this
        }

        metadata = new ArrayList<MockMetadata>();
        pipeline.execute(metadata);
        assert metadata.size() == 2;
        assert ((CountingStage) stages.get(1)).getCount() == 2;
        assert ((CountingStage) stages.get(2)).getCount() == 2;
    }

    protected List<Stage<MockMetadata>> buildStages() {
        MockMetadata md1 = new MockMetadata("one");
        MockMetadata md2 = new MockMetadata("two");

        StaticMetadataSourceStage<MockMetadata> source = new StaticMetadataSourceStage<MockMetadata>();
        source.setId("src");
        source.setSourceMetadata(CollectionSupport.toList(md1, md2));

        CountingStage<MockMetadata> stage1 = new CountingStage<MockMetadata>();
        CountingStage<MockMetadata> stage2 = new CountingStage<MockMetadata>();

        LazyList<? extends Stage<MockMetadata>> stages = CollectionSupport.toList(source, stage1, stage2);
        return (List<Stage<MockMetadata>>) stages;
    }
}