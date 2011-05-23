/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.HashSet;
import java.util.List;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;

import org.opensaml.util.Assert;
import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazyList;

/**
 * This {@link Stage} allows the joining of multiple pipeline outputs into a single {@link Collection} that can then be
 * used as the input source for another pipeline.
 * 
 * This source works producing a {@link Collection} by means of the registered
 * {@link net.shibboleth.metadata.pipeline.PipelineJoinerStage.ItemCollectionFactory} . Then each of its registered
 * {@link Pipeline} is invoked in turn (no ordering is guaranteed and pipelines may execute concurrently). After each
 * pipeline has completed the results are merged in to the Item collection given to this stage by means of the an
 * {@link CollectionMergeStrategy}.
 */
@ThreadSafe
public class PipelineJoinerStage extends BaseStage<Item<?>> {

    /**
     * The factory used to create the item returned by this source. Default implementation is
     * {@link SimpleItemCollectionFacotry}.
     */
    private ItemCollectionFactory collectionFactory = new SimpleItemCollectionFacotry();

    /** Strategy used to merge all the joined pipeline results in to the final Item collection. */
    private CollectionMergeStrategy mergeStrategy = new SimpleCollectionMergeStrategy();

    /** Pipelines whose results become the output of this source. */
    private List<Pipeline<Item<?>>> joinedPipelines = new LazyList<Pipeline<Item<?>>>();

    /**
     * Gets the unmodifiable set of pipelines used by this source.
     * 
     * @return unmodifiable set of pipelines used by this source
     */
    public List<Pipeline<Item<?>>> getJoinedPipelines() {
        return joinedPipelines;
    }

    /**
     * Sets the pipelines joined by this source.
     * 
     * @param pipelines pipelines joined by this source
     */
    public synchronized void setJoinedPipelines(final List<Pipeline<Item<?>>> pipelines) {
        if (isInitialized()) {
            return;
        }
        joinedPipelines = Collections.unmodifiableList(CollectionSupport.addNonNull(pipelines,
                new LazyList<Pipeline<Item<?>>>()));
    }

    /**
     * Gets the factory used to create the {@link Item} collection produced by this source.
     * 
     * @return factory used to create the {@link Item} collection produced by this source
     */
    public ItemCollectionFactory getCollectionFactory() {
        return collectionFactory;
    }

    /**
     * Sets the factory used to create the {@link Item} collection produced by this source.
     * 
     * @param factory factory used to create the {@link Item} collection produced by this source
     */
    public synchronized void setCollectionFactory(final ItemCollectionFactory factory) {
        if (isInitialized()) {
            return;
        }
        Assert.isNotNull(factory, "Collection factory may not be null");
        collectionFactory = factory;
    }

    /**
     * Gets the strategy used to merge all the joined pipeline results in to the final Item collection.
     * 
     * @return strategy used to merge all the joined pipeline results in to the final Item collection, never null
     */
    public CollectionMergeStrategy getCollectionMergeStrategy() {
        return mergeStrategy;
    }

    /**
     * Sets the strategy used to merge all the joined pipeline results in to the final Item collection.
     * 
     * @param strategy strategy used to merge all the joined pipeline results in to the final Item collection, never
     *            null
     */
    public synchronized void setCollectionMergeStrategy(final CollectionMergeStrategy strategy) {
        if (isInitialized()) {
            return;
        }
        Assert.isNotNull(strategy, "Collection merge strategy may not be null");
        mergeStrategy = strategy;
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<Item<?>> itemCollection) throws StageProcessingException {
        final ArrayList<Collection<Item<?>>> pipelineResults = new ArrayList<Collection<Item<?>>>();
        Collection<Item<?>> pipelineResult;
        for (Pipeline<Item<?>> pipeline : joinedPipelines) {
            try {
                pipelineResult = collectionFactory.newCollection();
                pipeline.execute(pipelineResult);
                pipelineResults.add(pipelineResult);
            } catch (PipelineProcessingException e) {
                throw new StageProcessingException(e);
            }
        }

        mergeStrategy.mergeCollection(itemCollection, pipelineResults.toArray(new Collection[pipelineResults.size()]));
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        for (Pipeline<Item<?>> pipeline : joinedPipelines) {
            if (!pipeline.isInitialized()) {
                pipeline.initialize();
            }
        }
    }

    /** Factory used to create the {@link Collection} that will be passed in to each child pipeline. */
    public static interface ItemCollectionFactory {

        /**
         * Creates the {@link Collection}.
         * 
         * @return the {@link Collection}
         */
        public Collection<Item<?>> newCollection();
    }

    /**
     * Implementation {@link net.shibboleth.metadata.pipeline.PipelineJoinerStage.ItemCollectionFactory} that produces
     * {@link ArrayList} instances.
     */
    public static class SimpleItemCollectionFacotry implements ItemCollectionFactory {

        /** {@inheritDoc} */
        public Collection<Item<?>> newCollection() {
            return new ArrayList<Item<?>>();
        }
    }

    /**
     * Strategy used to merge the results of each child pipeline in to the collection of Items given to this stage.
     */
    public static interface CollectionMergeStrategy {

        /**
         * Merges the results of each child pipeline in to the collection of Item given to this stage.
         * 
         * @param target collection in to which all the Items should be merged, never null
         * @param sources collections of Items to be merged in to the target, never null not containing any null
         *            elements
         */
        public void mergeCollection(Collection<Item<?>> target, Collection<Item<?>>... sources);
    }

    /**
     * A {@link CollectionMergeStrategy} that adds the Item from each source, in order, by means of the
     * {@link Collection#addAll(Collection)} method on the target.
     */
    public static class SimpleCollectionMergeStrategy implements CollectionMergeStrategy {

        /** {@inheritDoc} */
        public void mergeCollection(Collection<Item<?>> target, Collection<Item<?>>... sources) {
            for (Collection<Item<?>> source : sources) {
                target.addAll(source);
            }
        }
    }

    /**
     * A merge strategy that adds source items to the target collection if none of the Items in the target collection
     * have the same {@link ItemId} as source item. If the source item does not contain a {@link ItemId} it is always
     * added to the target collection.
     */
    public static class DeduplicatingItemIdMergeStrategy implements CollectionMergeStrategy {

        /** {@inheritDoc} */
        public void mergeCollection(Collection<Item<?>> target, Collection<Item<?>>... sources) {
            List<ItemId> itemIds;
            HashSet<ItemId> presentItemIds = new HashSet<ItemId>();

            for (Item item : target) {
                itemIds = item.getItemMetadata().get(ItemId.class);
                if (itemIds != null) {
                    presentItemIds.addAll(itemIds);
                }
            }

            for (Collection<Item<?>> source : sources) {
                merge(presentItemIds, target, source);
            }
        }

        /**
         * Adds source items to the target collection if none of the Items in the target collection have the same
         * {@link ItemId} as source item. If the source item does not contain a {@link ItemId} it is always added to the
         * target collection.
         * 
         * @param presentItemIds IDs that are already present in the target collection
         * @param target the collection to which items will be merged in to
         * @param sourceItems the collection of items to be merged in to the target
         */
        private void merge(HashSet<ItemId> presentItemIds, Collection<Item<?>> target, Collection<Item<?>> sourceItems) {
            boolean itemAlreadyPresent;
            List<ItemId> itemIds;
            for (Item sourceItem : sourceItems) {
                itemIds = sourceItem.getItemMetadata().get(ItemId.class);
                if (itemIds == null || itemIds.isEmpty()) {
                    target.add(sourceItem);
                    continue;
                }

                itemAlreadyPresent = false;
                for (ItemId itemId : itemIds) {
                    if (presentItemIds.contains(itemId)) {
                        itemAlreadyPresent = true;
                        break;
                    }
                }

                if (!itemAlreadyPresent) {
                    target.add(sourceItem);
                    presentItemIds.addAll(itemIds);
                }
            }
        }
    }
}