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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

/**
 * A stage that is composed of other stages. This allows a collection of stages to be grouped together and for that
 * composition to the be referenced and reused.
 * 
 * @param <T> type of metadata this stage, and its composed stages, operate upon
 */
@ThreadSafe
public class CompositeStage<T> extends BaseStage<T> {

    /** Stages which compose this stage. */
    private List<Stage<T>> composedStages = Collections.emptyList();


    /**
     * Gets an unmodifiable list the stages that compose this stage.
     * 
     * @return list the stages that compose this stage, never null nor containing null elements
     */
    @Nonnull @NonnullElements public List<Stage<T>> getComposedStages() {
        return composedStages;
    }

    /**
     * Sets the list the stages that compose this stage.
     * 
     * @param stages list the stages that compose this stage, may be null or contain null elements
     */
    public synchronized void setComposedStages(@Nullable @NullableElements final List<Stage<T>> stages) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        ArrayList<Stage<T>> newStages = new ArrayList<>();
        if (stages != null) {
            for (Stage<T> stage : stages) {
                if (stage != null) {
                    newStages.add(stage);
                }
            }
        }

        composedStages = Collections.unmodifiableList(newStages);
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull @NonnullElements final Collection<Item<T>> itemCollection)
            throws StageProcessingException {
        for (Stage<T> stage : composedStages) {
            stage.execute(itemCollection);
        }
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        composedStages = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (composedStages == null || composedStages.isEmpty()) {
            composedStages = Collections.emptyList();
        }
    }
}