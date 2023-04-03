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
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.FirstItemIdItemIdentificationStrategy;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemIdentificationStrategy;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.collection.ClassToInstanceMultiMap;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.logic.Constraint;

/**
 * An abstract {@link Stage} that selects {@link Item}s for further processing if they have a specific
 * type of {@link ItemMetadata} attached to them.
 * 
 * @param <T> the type of data included in the items being processed
 * @param <B> the type bound for the selection requirements set
 */
@ThreadSafe
public abstract class AbstractItemMetadataSelectionStage<T, B> extends AbstractStage<T> {

    /**
     * {@link ItemMetadata} classes that, if an item contains them, will cause the {@link Item} to be selected.
     */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private Set<Class<? extends B>> selectionRequirements = CollectionSupport.emptySet();

    /** Strategy used to generate item identifiers for logging purposes. */
    @Nonnull @GuardedBy("this")
    private ItemIdentificationStrategy<T> identificationStrategy = new FirstItemIdItemIdentificationStrategy<>();

    /**
     * Gets the {@link ItemMetadata} classes that, if an item contains them, will cause the {@link Item} to be
     * selected.
     * 
     * @return {@link ItemMetadata} classes that, if an item contains them, will cause the {@link Item} to be
     *         selected, never null nor containing null elements
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized Collection<Class<? extends B>> getSelectionRequirements() {
        return selectionRequirements;
    }

    /**
     * Sets the {@link ItemMetadata} classes that, if an item contains them, will cause the {@link Item} to be
     * selected.
     * 
     * @param requirements {@link ItemMetadata} classes that, if an item contains them, will cause the
     *            {@link Item} to be selected
     */
    public synchronized void setSelectionRequirements(
            @Nonnull @NonnullElements @Unmodifiable final Collection<Class<? extends B>> requirements) {
        checkSetterPreconditions();
        selectionRequirements = CollectionSupport.copyToSet(requirements);
    }

    /**
     * Gets the strategy used to generate {@link Item} identifiers for logging purposes.
     * 
     * @return strategy used to generate {@link Item} identifiers for logging purposes
     */
    @Nonnull public final synchronized ItemIdentificationStrategy<T> getItemIdentificationStrategy() {
        return identificationStrategy;
    }

    /**
     * Sets the strategy used to generate {@link Item} identifiers for logging purposes.
     * 
     * @param strategy strategy used to generate {@link Item} identifiers for logging purposes, can not be null
     */
    public synchronized void setIdentificationStrategy(@Nonnull final ItemIdentificationStrategy<T> strategy) {
        checkSetterPreconditions();
        identificationStrategy = Constraint.isNotNull(strategy, "Item identification strategy can not be null");
    }

    @Override
    protected void doExecute(final @Nonnull List<Item<T>> items) throws StageProcessingException {
        // we make a defensive copy here in case logic in the delegate #doExecute makes changes
        // to the item collection and thus would cause issues if we were iterating over it directly
        final @Nonnull var collectionCopy = new ArrayList<>(items);

        for (final Item<T> item : collectionCopy) {
            assert item != null;
            final var matchingMetadata = new ClassToInstanceMultiMap<B>();

            for (final Class<? extends B> infoClass : getSelectionRequirements()) {
                if (item.getItemMetadata().containsKey(infoClass)) {
                    matchingMetadata.putAll(item.getItemMetadata().get(infoClass));
                }
            }

            if (!matchingMetadata.isEmpty()) {
                doExecute(items, item, matchingMetadata);
            }
        }
    }

    /**
     * Performs the stage's logic on the given item that contained metadata of the given type.
     * 
     * @param items current item collection
     * @param matchingItem matching item
     * @param matchingMetadata all the {@link ItemMetadata} instances that match a selection criteria
     * 
     * @throws StageProcessingException thrown if there is a problem processing the item
     */
    protected abstract void doExecute(
            @Nonnull @NonnullElements final List<Item<T>> items,
            @Nonnull final Item<T> matchingItem,
            @Nonnull @NonnullElements final ClassToInstanceMultiMap<B> matchingMetadata)
                    throws StageProcessingException;

}
