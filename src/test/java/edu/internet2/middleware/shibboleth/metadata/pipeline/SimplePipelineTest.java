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

import java.util.List;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazyList;
import org.testng.annotations.Test;

import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.MockMetadata;

public class SimplePipelineTest {

    @Test
    public void testInitialize() throws Exception {
        Source<MockMetadata> source = buildSource();
        List<Stage<MockMetadata>> stages = buildStages();
        
        SimplePipeline<MockMetadata> pipeline = new SimplePipeline<MockMetadata>();
        pipeline.setId(" test ");
        pipeline.setSource(source);
        pipeline.setStages(stages);
        assert "test".equals(pipeline.getId());
        assert pipeline.getSource() == source;
        assert !pipeline.getSource().isInitialized();
        assert pipeline.getStages() != stages;
        assert pipeline.getStages().size() == 2;
        assert pipeline.getStages().containsAll(stages);
        assert !pipeline.getStages().get(0).isInitialized();
        assert !pipeline.getStages().get(1).isInitialized();
        assert pipeline.getInitializationInstant() == null;
        
        pipeline.initialize();        
        assert "test".equals(pipeline.getId());
        assert pipeline.getSource() == source;
        assert pipeline.getSource().isInitialized();
        assert pipeline.getStages() != stages;
        assert pipeline.getStages().size() == 2;
        assert pipeline.getStages().containsAll(stages);
        assert pipeline.getStages().get(0).isInitialized();
        assert pipeline.getStages().get(1).isInitialized();
        assert pipeline.getInitializationInstant() != null;
        
        try{
            pipeline = new SimplePipeline<MockMetadata>();
            pipeline.setSource(source);
            pipeline.setStages(stages);
            pipeline.initialize();
            throw new AssertionError();
        }catch(IllegalArgumentException e){
            //expected this
        }
        
        try{
            pipeline = new SimplePipeline<MockMetadata>();
            pipeline.setId("");
            pipeline.setSource(source);
            pipeline.setStages(stages);
            pipeline.initialize();
            throw new AssertionError();
        }catch(IllegalArgumentException e){
            //expected this
        }
        
        try{
            pipeline = new SimplePipeline<MockMetadata>();
            pipeline.setId("test");
            pipeline.setStages(stages);
            pipeline.initialize();
            throw new AssertionError();
        }catch(IllegalArgumentException e){
            //expected this
        }
    }
    
    @Test
    public void testExecution() throws Exception{
        Source<MockMetadata> source = buildSource();
        List<Stage<MockMetadata>> stages = buildStages();
        
        SimplePipeline<MockMetadata> pipeline = new SimplePipeline<MockMetadata>();
        pipeline.setId("test");
        pipeline.setSource(source);
        pipeline.setStages(stages);
                
        MetadataCollection<MockMetadata> result = pipeline.execute();
        assert result.size() == 2;
        
        assert ((CountingStage)stages.get(0)).getCount() == 1;
        assert ((CountingStage)stages.get(1)).getCount() == 1;
        
        MockMetadata md = result.iterator().next();
        assert md.getMetadataInfo().containsKey(ComponentInfo.class);
        assert md.getMetadataInfo().values().size() == 1;
        assert md.getMetadataInfo().containsKey(ComponentInfo.class);
        
        try{
            List<Stage<MockMetadata>> pipelineStages = pipeline.getStages();
            pipelineStages.clear();
            throw new AssertionError();
        }catch(UnsupportedOperationException e){
            //expected this
        }
        
        result = pipeline.execute();
        assert result.size() == 2;
        assert ((CountingStage)stages.get(0)).getCount() == 2;
        assert ((CountingStage)stages.get(0)).getCount() == 2;
    }
    
    protected Source<MockMetadata> buildSource(){
        MockMetadata md1 = new MockMetadata("one");
        MockMetadata md2 = new MockMetadata("two");
        
        StaticSource<MockMetadata> source = new StaticSource<MockMetadata>();
        source.setId("src");
        source.setSourceMetadata(CollectionSupport.toList(md1, md2));
        
        return source;
    }
    
    protected List<Stage<MockMetadata>> buildStages(){
        CountingStage<MockMetadata> stage1 = new CountingStage<MockMetadata>();
        CountingStage<MockMetadata> stage2 = new CountingStage<MockMetadata>();
        
        LazyList<? extends Stage<MockMetadata>> stages = CollectionSupport.toList(stage1, stage2);
        return (List<Stage<MockMetadata>>) stages;
    }
}