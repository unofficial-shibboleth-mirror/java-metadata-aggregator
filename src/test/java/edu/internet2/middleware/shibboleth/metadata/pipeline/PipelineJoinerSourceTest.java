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

package edu.internet2.middleware.shibboleth.metadata.pipeline;

import java.util.ArrayList;
import java.util.Collection;

import org.testng.annotations.Test;

import edu.internet2.middleware.shibboleth.metadata.MockMetadata;
import edu.internet2.middleware.shibboleth.metadata.core.Metadata;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.Pipeline;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.PipelineJoinerSource;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.SimplePipeline;

/**
 *
 */
public class PipelineJoinerSourceTest {

    @Test
    public void test() throws Exception {
        MockMetadata md1 = new MockMetadata("one");
        Pipeline<Metadata<?>> pipeline1 = new SimplePipeline("p1", new MockSource(md1), null);

        MockMetadata md2 = new MockMetadata("two");
        Pipeline<Metadata<?>> pipeline2 = new SimplePipeline("p2", new MockSource(md2), null);

        Collection<Pipeline<Metadata<?>>> joinedPipelines = new ArrayList<Pipeline<Metadata<?>>>();
        joinedPipelines.add(pipeline1);
        joinedPipelines.add(pipeline2);

        PipelineJoinerSource joinSource = new PipelineJoinerSource("joinSource", joinedPipelines);
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