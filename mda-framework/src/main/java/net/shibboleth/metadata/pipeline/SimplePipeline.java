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

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.shared.component.ComponentInitializationException;

/**
 * A very simple implementation of {@link Pipeline}.
 * 
 * @param <T> the type of item upon which this stage operates
 */
@ThreadSafe
public class SimplePipeline<T> extends AbstractIdentifiableInitializableComponent
        implements Pipeline<T> {

    /** Stages for this pipeline. */
    @Nonnull @NonnullElements @GuardedBy("this")
    private List<Stage<T>> pipelineStages = Collections.emptyList();

    @Override
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized List<Stage<T>> getStages() {
        return pipelineStages;
    }

    /**
     * Sets the stages that make up this pipeline.
     * 
     * @param stages stages that make up this pipeline
     */
    public synchronized void setStages(
            @Nonnull @NonnullElements @Unmodifiable final List<Stage<T>> stages) {
        checkSetterPreconditions();
        pipelineStages = List.copyOf(stages);
    }

    @Override
    public void execute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws PipelineProcessingException {

        final var start = Instant.now();

        for (final Stage<T> stage : getStages()) {
            stage.execute(items);
        }

        final var componentInfo = new ComponentInfo(getId(), getClass(), start, Instant.now());
        for (final var item : items) {
            item.getItemMetadata().put(componentInfo);
        }
    }

    @Override
    protected synchronized void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        for (final Stage<T> stage : pipelineStages) {
            if (!stage.isInitialized()) {
                stage.initialize();
            }
        }
    }
}
