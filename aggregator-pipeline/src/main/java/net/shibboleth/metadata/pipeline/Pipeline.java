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

import java.util.Collection;
import java.util.List;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.Metadata;

/**
 * A pipeline represents an ordered list of {@link Stage}s that operate on a collection of metadata. In general the
 * first stage is responsible for populating the collection with an initial set of metadata which subsequent stages
 * further modify.
 * 
 * Each pipeline must be initialized, via the {@link #initialize()} method, before use. After a pipeline has been
 * initialized it may never be re-initialized. A pipeline is not considered initialized until all of its {@link Stages},
 * have been initialized.
 * 
 * Pipelines are reusable and threadsafe.
 * 
 * @param <MetadataType> type of metadata element which is produced by this source
 */
@ThreadSafe
public interface Pipeline<MetadataType extends Metadata<?>> extends Component {

    /**
     * Gets the list of Stages within the pipeline.
     * 
     * @return unmodifiable list of stages within the pipeline
     */
    public List<Stage<MetadataType>> getStages();

    /**
     * Executes each registered {@link Stage} in turn.
     * 
     * @param metadataCollection the collection that will hold the metadata as it passes from stage to stage
     * 
     * @throws PipelineProcessingException thrown if there is a problem processing the pipeline
     */
    public void execute(Collection<MetadataType> metadataCollection) throws PipelineProcessingException;
}