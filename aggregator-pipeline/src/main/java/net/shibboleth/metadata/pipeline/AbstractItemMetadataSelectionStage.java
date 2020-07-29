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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.FirstItemIdItemIdentificationStrategy;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemIdentificationStrategy;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * An abstract {@link Stage} that selects {@link Item}s for further processing if they have a specific
 * type of {@link ItemMetadata} attached to them.
 * 
 * @param <T> the type of data included in the items being processed
 */
@ThreadSafe
public abstract class AbstractItemMetadataSelectionStage<T> extends AbstractStage<T> {

    /**
     * {@link ItemMetadata} classes that, if an item contains them, will cause the {@link Item} to be selected.
     */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private Set<Class<? extends ItemMetadata>> selectionRequirements = Set.of();

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
    public final synchronized Collection<Class<? extends ItemMetadata>> getSelectionRequirements() {
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
            @Nonnull @NonnullElements @Unmodifiable final Collection<Class<? extends ItemMetadata>> requirements) {
        throwSetterPreconditionExceptions();
        selectionRequirements = Set.copyOf(requirements);
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
        throwSetterPreconditionExceptions();
        identificationStrategy = Constraint.isNotNull(strategy, "Item identification strategy can not be null");
    }

    @Override
    protected void doExecute(final Collection<Item<T>> itemCollection) throws StageProcessingException {
        // we make a defensive copy here in case logic in the delegate #doExecute makes changes
        // to the itemCollection and thus would cause issues if we were iterating over it directly
        final ArrayList<Item<T>> collectionCopy = new ArrayList<>(itemCollection);

        for (final Item<T> item : collectionCopy) {
            final HashMap<Class<? extends ItemMetadata>, List<? extends ItemMetadata>> matchingMetadata =
                    new HashMap<>();

            for (final Class<? extends ItemMetadata> infoClass : getSelectionRequirements()) {
                if (item.getItemMetadata().containsKey(infoClass)) {
                    matchingMetadata.put(infoClass, item.getItemMetadata().get(infoClass));
                }
            }

            if (!matchingMetadata.isEmpty()) {
                doExecute(itemCollection, item, matchingMetadata);
            }
        }
    }

    @Override
    protected void doDestroy() {
        selectionRequirements = null;
        identificationStrategy = null;

        super.doDestroy();
    }

    /**
     * Performs the stage's logic on the given item that contained metadata of the given type.
     * 
     * @param itemCollection current item collection
     * @param matchingItem matching item
     * @param matchingMetadata all the {@link ItemMetadata} instances that match a selection criteria
     * 
     * @throws StageProcessingException thrown if there is a problem processing the item
     */
    protected abstract void doExecute(
            @Nonnull @NonnullElements final Collection<Item<T>> itemCollection,
            @Nonnull final Item<T> matchingItem,
            @Nonnull @NonnullElements
            final Map<Class<? extends ItemMetadata>, List<? extends ItemMetadata>> matchingMetadata)
                    throws StageProcessingException;

}
