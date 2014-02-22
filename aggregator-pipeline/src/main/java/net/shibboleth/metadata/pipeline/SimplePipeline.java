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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.util.ItemMetadataSupport;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.AbstractDestructableIdentifiedInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * A very simple implementation of {@link Pipeline}.
 * 
 * @param <T> the type of item upon which this stage operates
 */
@ThreadSafe
public class SimplePipeline<T> extends AbstractDestructableIdentifiedInitializableComponent
        implements Pipeline<T> {

    /** Stages for this pipeline. */
    private List<Stage<T>> pipelineStages = Collections.emptyList();

    /** {@inheritDoc} */
    @Override public synchronized void setId(@Nonnull @NotEmpty String componentId) {
        super.setId(componentId);
    }

    /** {@inheritDoc} */
    @Override @Nonnull @NonnullElements @Unmodifiable public List<Stage<T>> getStages() {
        return pipelineStages;
    }

    /**
     * Sets the stages that make up this pipeline.
     * 
     * @param stages stages that make up this pipeline
     */
    public synchronized void setStages(final List<Stage<T>> stages) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        if (stages == null || stages.isEmpty()) {
            pipelineStages = Collections.emptyList();
        } else {
            pipelineStages = ImmutableList.copyOf(Iterables.filter(stages, Predicates.notNull()));
        }
    }

    /** {@inheritDoc} */
    @Override public void execute(@Nonnull @NonnullElements final Collection<Item<T>> itemCollection)
            throws PipelineProcessingException {
        final ComponentInfo compInfo = new ComponentInfo(this);

        for (Stage<T> stage : pipelineStages) {
            stage.execute(itemCollection);
        }

        compInfo.setCompleteInstant();
        ItemMetadataSupport.addToAll(itemCollection, Collections.singleton(compInfo));
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        pipelineStages = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        for (Stage<T> stage : pipelineStages) {
            if (!stage.isInitialized()) {
                stage.initialize();
            }
        }
    }
}