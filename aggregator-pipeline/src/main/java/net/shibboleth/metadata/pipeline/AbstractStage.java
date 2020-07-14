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
import java.util.Collection;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.impl.BaseIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A base class for {@link Stage} implementations.
 * 
 * @param <T> type of item this stage operates upon
 */
@ThreadSafe
public abstract class AbstractStage<T> extends BaseIdentifiableInitializableComponent implements Stage<T> {

    /**
     * The {@link Predicate} applied to the supplied item collection to determine whether the stage will be executed.
     * Default value: always <code>true</code>.
     */
    @Nonnull @GuardedBy("this")
    private Predicate<Collection<Item<T>>> collectionPredicate = x -> true;
    
    /**
     * Sets the {@link Predicate} applied to the supplied item collection to determine whether
     * the stage will be executed.
     * 
     * @param pred the {@link Predicate} applied to the supplied item collection to determine
     * whether the stage will be executed
     */
    public synchronized void setCollectionPredicate(@Nonnull final Predicate<Collection<Item<T>>> pred) {
        throwSetterPreconditionExceptions();
        collectionPredicate = Constraint.isNotNull(pred, "collectionPredicate may not be null");
    }
    
    /**
     * Gets the {@link Predicate} being applied to the supplied item collection to determine whether
     * the stage will be executed.
     * 
     * @return the {@link Predicate} being applied to the supplied item collection to determine whether
     * the stage will be executed.
     */
    @Nonnull
    public final synchronized Predicate<Collection<Item<T>>> getCollectionPredicate() {
        return collectionPredicate;
    }

    @Override
    public void execute(@Nonnull @NonnullElements final Collection<Item<T>> itemCollection)
            throws StageProcessingException {
        throwComponentStateExceptions();

        final var start = Instant.now();

        if (getCollectionPredicate().test(itemCollection)) {
            doExecute(itemCollection);
        }

        final var componentInfo = new ComponentInfo(getId(), getClass(), start, Instant.now());
        for (final var item : itemCollection) {
            item.getItemMetadata().put(componentInfo);
        }
    }

    /**
     * Performs the stage processing on the given Item collection.
     * 
     * <p>
     * The stage is guaranteed to be have been initialized and not destroyed when this is invoked.
     * </p>
     * 
     * @param itemCollection collection to be processed
     * 
     * @throws StageProcessingException thrown if there is an unrecoverable problem when processing the stage
     */
    protected abstract void doExecute(@Nonnull @NonnullElements final Collection<Item<T>> itemCollection)
            throws StageProcessingException;

}
