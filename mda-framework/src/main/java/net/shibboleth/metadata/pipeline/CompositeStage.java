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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.primitive.DeprecationSupport;
import net.shibboleth.shared.primitive.DeprecationSupport.ObjectType;

/**
 * A stage that is composed of other stages. This allows a collection of stages to be grouped together and for that
 * composition to the be referenced and reused.
 *
 * <p>
 * Provides a simple implementation of the {@link Pipeline<T>} interface.
 * </p>
 *
 * @param <T> type of metadata this stage, and its composed stages, operate upon
 */
@ThreadSafe
public class CompositeStage<T> extends AbstractStage<T> implements Pipeline<T> {

    /** Stages which compose this stage. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<Stage<T>> composedStages = List.of();

    /**
     * Gets an unmodifiable list of the stages that compose this stage.
     * 
     * @return list the stages that compose this stage, never null nor containing null elements
     *
     * @since 0.10.0
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized List<Stage<T>> getStages() {
        return composedStages;
    }

    /**
     * Sets the list of stages that compose this stage.
     * 
     * @param stages list of the stages that compose this stage
     *
     * @since 0.10.0
     */
    public final synchronized void setStages(
            @Nonnull @NonnullElements @Unmodifiable final List<Stage<T>> stages) {
        checkSetterPreconditions();
        composedStages = List.copyOf(stages);
    }

    /**
     * Gets an unmodifiable list of the stages that compose this stage.
     * 
     * @return list the stages that compose this stage, never null nor containing null elements
     *
     * @deprecated Replaced by {@link #getStages}.
     * @see #getStages
     */
    @Deprecated(since="0.10.0", forRemoval=true)
    @Nonnull @NonnullElements @Unmodifiable
    public final List<Stage<T>> getComposedStages() {
        DeprecationSupport.warnOnce(ObjectType.METHOD, "getComposedStages", "CompositeStage", "getStages");
        return getStages();
    }

    /**
     * Sets the list of stages that compose this stage.
     * 
     * @param stages list of the stages that compose this stage
     *
     * @deprecated Replaced by {@link setStages}.
     * @see #setStages
     */
    @Deprecated(since="0.10.0", forRemoval=true)
    public final void setComposedStages(
            @Nonnull @NonnullElements @Unmodifiable final List<Stage<T>> stages) {
        DeprecationSupport.warnOnce(ObjectType.METHOD, "setComposedStages", "CompositeStage", "setStages");
        setStages(stages);
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws StageProcessingException {
        for (final Stage<T> stage : getStages()) {
            stage.execute(items);
        }
    }

    @Override
    protected synchronized void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        for (final Stage<T> stage : composedStages) {
            if (!stage.isInitialized()) {
                stage.initialize();
            }
        }
    }

}
