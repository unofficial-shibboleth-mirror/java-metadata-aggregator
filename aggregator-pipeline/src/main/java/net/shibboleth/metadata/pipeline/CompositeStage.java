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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

/**
 * A stage that is composed of other stages. This allows a collection of stages to be grouped together and for that
 * composition to the be referenced and reused.
 * 
 * @param <T> type of metadata this stage, and its composed stages, operate upon
 */
@ThreadSafe
public class CompositeStage<T> extends AbstractStage<T> {

    /** Stages which compose this stage. */
    @Nonnull @NonnullElements @Unmodifiable
    private List<Stage<T>> composedStages = List.of();


    /**
     * Gets an unmodifiable list of the stages that compose this stage.
     * 
     * @return list the stages that compose this stage, never null nor containing null elements
     */
    @Nonnull @NonnullElements @Unmodifiable
    public List<Stage<T>> getComposedStages() {
        return composedStages;
    }

    /**
     * Sets the list of stages that compose this stage.
     * 
     * @param stages list of the stages that compose this stage
     */
    public synchronized void setComposedStages(
            @Nonnull @NonnullElements @Unmodifiable final List<Stage<T>> stages) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        composedStages = List.copyOf(stages);
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final Collection<Item<T>> itemCollection)
            throws StageProcessingException {
        for (final Stage<T> stage : composedStages) {
            stage.execute(itemCollection);
        }
    }

    @Override
    protected void doDestroy() {
        composedStages = null;

        super.doDestroy();
    }
}
