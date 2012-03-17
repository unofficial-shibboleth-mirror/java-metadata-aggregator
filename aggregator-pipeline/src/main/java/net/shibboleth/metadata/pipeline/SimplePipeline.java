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

import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.util.ItemMetadataSupport;
import net.shibboleth.utilities.java.support.component.AbstractDestrucableIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * A very simple implementation of {@link Pipeline}.
 * 
 * @param <ItemType> the type of Item upon which this stage operates
 */
@ThreadSafe
public class SimplePipeline<ItemType extends Item<?>> extends AbstractDestrucableIdentifiableInitializableComponent
        implements Pipeline<ItemType> {

    /** Stages for this pipeline. */
    private List<? extends Stage<ItemType>> pipelineStages = Collections.emptyList();

    /** {@inheritDoc} */
    public synchronized void setId(String componentId) {
        super.setId(componentId);
    }
    
    /** {@inheritDoc} */
    public List<? extends Stage<ItemType>> getStages() {
        return pipelineStages;
    }

    /**
     * Sets the stages that make up this pipeline.
     * 
     * @param stages stages that make up this pipeline
     */
    public synchronized void setStages(final List<? extends Stage<ItemType>> stages) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        pipelineStages = ImmutableList.copyOf(Iterables.filter(stages, Predicates.notNull()));
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
    protected void doDestroy() {
        pipelineStages = null;
        
        super.doDestroy();
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (pipelineStages == null || pipelineStages.isEmpty()) {
            pipelineStages = Collections.emptyList();
        }

        for (Stage<ItemType> stage : pipelineStages) {
            if (!stage.isInitialized()) {
                stage.initialize();
            }
        }
    }
}