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
import java.util.List;

import org.testng.annotations.Test;

import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.MockMetadata;
import edu.internet2.middleware.shibboleth.metadata.pipeline.ComponentInfo;
import edu.internet2.middleware.shibboleth.metadata.pipeline.SimplePipeline;
import edu.internet2.middleware.shibboleth.metadata.pipeline.Stage;

/**
 *
 */
public class SimplePipelineTest {

    @Test
    public void test() throws Exception{
        MockMetadata md1 = new MockMetadata("one");
        MockMetadata md2 = new MockMetadata("two");
        MockSource source = new MockSource(md1, md2);
        
        ArrayList<Stage<MockMetadata>> stages = new ArrayList<Stage<MockMetadata>>();
        CountingStage<MockMetadata> stage1 = new CountingStage<MockMetadata>();
        stages.add(stage1);
        CountingStage<MockMetadata> stage2 = new CountingStage<MockMetadata>();
        stages.add(stage2);
        
        try{
            new SimplePipeline<MockMetadata>(null, source, stages);
            throw new AssertionError();
        }catch(IllegalArgumentException e){
            //expected this
        }
        
        try{
            new SimplePipeline<MockMetadata>("test", null, stages);
            throw new AssertionError();
        }catch(IllegalArgumentException e){
            //expected this
        }
        
        SimplePipeline<MockMetadata> pipeline = new SimplePipeline<MockMetadata>(" test ", source, stages);
        assert pipeline.getId().equals("test");
        assert !pipeline.isInitialized();
        assert !source.isInitialized();
        assert !stage1.isInitialized();
        assert !stage2.isInitialized();
        
        pipeline.initialize();
        assert pipeline.isInitialized();
        assert source.isInitialized();
        assert stage1.isInitialized();
        assert stage2.isInitialized();
        
        MetadataCollection<MockMetadata> result = pipeline.execute();
        assert result.size() == 2;
        assert stage1.getCount() == 1;
        assert stage2.getCount() == 1;
        assert md1.getMetadataInfo().values().size() == 1;
        assert md1.getMetadataInfo().containsKey(ComponentInfo.class);
        assert md2.getMetadataInfo().values().size() == 1;
        assert md2.getMetadataInfo().containsKey(ComponentInfo.class);
        
        try{
            List<Stage<MockMetadata>> pipelineStages = pipeline.getStages();
            pipelineStages.clear();
            throw new AssertionError();
        }catch(UnsupportedOperationException e){
            //expected this
        }
        
        result = pipeline.execute();
        assert result.size() == 2;
        assert stage1.getCount() == 2;
        assert stage2.getCount() == 2;
    }
}