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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.util.ItemMetadataSupport;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazyList;

/**
 * A very simple implementation of {@link Pipeline}.
 * 
 * @param <ItemType> the type of Item upon which this stage operates
 */
@ThreadSafe
public class SimplePipeline<ItemType extends Item<?>> extends AbstractComponent implements
        Pipeline<ItemType> {

    /** Stages for this pipeline. */
    private List<Stage<ItemType>> pipelineStages = Collections.emptyList();

    /** {@inheritDoc} */
    public List<Stage<ItemType>> getStages() {
        return pipelineStages;
    }

    /**
     * Sets the stages that make up this pipeline.
     * 
     * @param stages stages that make up this pipeline
     */
    public synchronized void setStages(final List<Stage<ItemType>> stages) {
        if (isInitialized()) {
            return;
        }
        pipelineStages = Collections.unmodifiableList(CollectionSupport.addNonNull(stages,
                new LazyList<Stage<ItemType>>()));
    }

    /** {@inheritDoc} */
    public void execute(Collection<ItemType> itemCollection) throws PipelineProcessingException {
        final ComponentInfo compInfo = new ComponentInfo(this);

        for (Stage<ItemType> stage : pipelineStages) {
            stage.execute(itemCollection);
        }

        compInfo.setCompleteInstant();
        ItemMetadataSupport.addToAll(itemCollection, compInfo);
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        if(pipelineStages == null || pipelineStages.isEmpty()){
            pipelineStages = Collections.emptyList();
        }
        
        for (Stage<ItemType> stage : pipelineStages) {
            if (!stage.isInitialized()) {
                stage.initialize();
            }
        }
    }
}