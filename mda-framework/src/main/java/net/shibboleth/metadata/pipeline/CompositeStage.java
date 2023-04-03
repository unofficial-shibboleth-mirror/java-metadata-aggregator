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

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;

import net.shibboleth.metadata.Item;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.primitive.DeprecationSupport;
import net.shibboleth.shared.primitive.DeprecationSupport.ObjectType;
import net.shibboleth.shared.primitive.LoggerFactory;

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

    /**
     * Class logger.
     *
     * @since 0.10.0
     */
    private static final @Nonnull Logger LOG = LoggerFactory.getLogger(CompositeStage.class);

    /**
     * Whether we are logging progress for all instances, regardless of their
     * {@link #loggingProgress} settings.
     *
     * <p>
     * To enable this feature, define the system property
     * <code>net.shibboleth.metadata.loggingAllProgress</code>
     * to the token <code>true</code>.
     * </p>
     */
    private static final boolean LOGGING_ALL_PROGRESS =
            Boolean.parseBoolean(System.getProperty("net.shibboleth.metadata.loggingAllProgress"));

    /** Stages which compose this stage. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<Stage<T>> composedStages = CollectionSupport.emptyList();
    
    /**
     * Whether we are logging progress through the stages.
     *
     * <p>Default value: <code>false</code></p>
     *
     * @since 0.10.0
     */
    @GuardedBy("this")
    private boolean loggingProgress;

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
        composedStages = CollectionSupport.copyToList(stages);
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

    /**
     * Returns whether we are logging progress.
     *
     * @return <code>true</code> if we are logging progress
     *
     * @since 0.10.0
     */
    public final synchronized boolean isLoggingProgress() {
        return loggingProgress;
    }

    /**
     * Sets whether we are logging progress.
     *
     * @param log <code>true</code> to log progress
     *
     * @since 0.10.0
     */
    public final synchronized void setLoggingProgress(final boolean log) {
        checkSetterPreconditions();
        loggingProgress = log;
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws StageProcessingException {
        if (LOGGING_ALL_PROGRESS || isLoggingProgress()) {            
            final var id = getId();
            final var start = Instant.now();
            for (final Stage<T> stage : getStages()) {
                final var stageId = stage.getId();
                final var stageStart = Instant.now();
                LOG.info("{} >>> {}, count={}", id, stageId, items.size());
                stage.execute(items);
                final var stageEnd = Instant.now();
                final var stageTime = Duration.between(stageStart, stageEnd);
                LOG.info("{} <<< {}, count={}, duration={}", id, stageId,
                        items.size(), stageTime);
            }
            LOG.info("{} completed, duration={}", id, Duration.between(start, Instant.now()));
        } else {
            for (final Stage<T> stage : getStages()) {
                stage.execute(items);
            }
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
