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
import java.util.Collection;
import java.util.List;

import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.MockMetadata;
import net.shibboleth.metadata.pipeline.Pipeline;
import net.shibboleth.metadata.pipeline.PipelineJoinerSource;
import net.shibboleth.metadata.pipeline.SimplePipeline;
import net.shibboleth.metadata.pipeline.StaticSource;

import org.opensaml.util.collections.CollectionSupport;
import org.testng.annotations.Test;


/**
 *
 */
public class PipelineJoinerSourceTest {

    @Test
    public void test() throws Exception {
        MockMetadata md1 = new MockMetadata("one");
        StaticSource<MockMetadata> source1 = new StaticSource<MockMetadata>();
        source1.setId("src1");
        source1.setSourceMetadata(CollectionSupport.toList(md1));
        SimplePipeline<MockMetadata> pipeline1 = new SimplePipeline<MockMetadata>();
        pipeline1.setId("p1");
        pipeline1.setSource(source1);

        MockMetadata md2 = new MockMetadata("two");
        StaticSource<MockMetadata> source2 = new StaticSource<MockMetadata>();
        source2.setId("src2");
        source2.setSourceMetadata(CollectionSupport.toList(md2));
        SimplePipeline<MockMetadata> pipeline2 = new SimplePipeline<MockMetadata>();
        pipeline2.setId("p2");
        pipeline2.setSource(source2);

        Collection<Pipeline<MockMetadata>> joinedPipelines = new ArrayList<Pipeline<MockMetadata>>();
        joinedPipelines.add(pipeline1);
        joinedPipelines.add(pipeline2);
        
        PipelineJoinerSource joinSource = new PipelineJoinerSource();
        joinSource.setId("joinSource");
        joinSource.setJoinedPipelines((List)CollectionSupport.toList(pipeline1, pipeline2));
        
        assert !joinSource.isInitialized();
        assert !pipeline1.isInitialized();
        assert !pipeline2.isInitialized();

        joinSource.initialize();
        assert joinSource.isInitialized();
        assert pipeline1.isInitialized();
        assert pipeline2.isInitialized();

        MetadataCollection<Metadata<?>> result = joinSource.execute();
        assert result.size() == 2;

        boolean md1CloneMatch = false, md2CloneMatch = false;
        for (Metadata<?> metadata : result) {
            if ("one".equals(metadata.getMetadata())) {
                md1CloneMatch = true;
                assert metadata != md1;
            } else if ("two".equals(metadata.getMetadata())) {
                md2CloneMatch = true;
                assert metadata != md2;
            }
            //two ComponentInfo, one from the pipeline, one from the join source
            assert metadata.getMetadataInfo().values().size() == 2;
        }

        assert md1CloneMatch;
        assert md2CloneMatch;
    }
}