/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
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
import java.util.Iterator;

import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.MetadataInfo;
import net.shibboleth.metadata.util.MetadataInfoHelper;

import org.opensaml.util.Assert;

/**
 * A base class for {@link Stage} implementations.
 * 
 * @param <MetadataType> type of metadata elements this stage operates upon
 */
public abstract class BaseStage<MetadataType extends Metadata<?>> extends AbstractComponent implements
        Stage<MetadataType> {

    /** Filter run prior to processing the metadata collection. */
    private MetadataFilteringStrategy preProcessFilter = new NoOpFilteringStrategy();

    /** Filter run after processing the metadata collection. */
    private MetadataFilteringStrategy postProcessFilter = new NoOpFilteringStrategy();

    /**
     * Gets the filter run after processing the metadata collection.
     * 
     * @return filter run after processing the metadata collection, never null
     */
    public MetadataFilteringStrategy getPostProcessFilter() {
        return postProcessFilter;
    }

    /**
     * Sets the filter run after processing the metadata collection.
     * 
     * @param filter filter run after processing the metadata collection, never null
     */
    public synchronized void setPostProcessFilter(MetadataFilteringStrategy filter) {
        if (isInitialized()) {
            return;
        }
        Assert.isNotNull(filter, "Post-processing filter can not be null");
        postProcessFilter = filter;
    }

    /**
     * Gets the filter run prior to processing the metadata collection.
     * 
     * @return filter run prior to processing the metadata collection
     */
    public MetadataFilteringStrategy getPreProcessFilter() {
        return preProcessFilter;
    }

    /**
     * Sets the filter run prior to processing the metadata collection.
     * 
     * @param filter filter run prior to processing the metadata collection
     */
    public synchronized void setPreProcessFilter(MetadataFilteringStrategy filter) {
        if (isInitialized()) {
            return;
        }
        Assert.isNotNull(filter, "Pre-processing filter can not be null");
        preProcessFilter = filter;
    }

    /**
     * Creates an {@link ComponentInfo}, delegates actual work on the collection to {@link #doExecute(Collection)}, adds
     * the {@link ComponentInfo} to all the resultant metadata elements and then sets its completion time.
     * 
     * {@inheritDoc}
     */
    public void execute(Collection<MetadataType> metadataCollection) throws StageProcessingException {
        final ComponentInfo compInfo = new ComponentInfo(this);

        preProcessFilter.filterMetadata(metadataCollection);
        doExecute(metadataCollection);
        postProcessFilter.filterMetadata(metadataCollection);

        MetadataInfoHelper.addToAll(metadataCollection, compInfo);
        compInfo.setCompleteInstant();
    }

    /**
     * Performs the stage process on the given metadata collection.
     * 
     * @param metadataCollection collection to be processed
     * 
     * @throws StageProcessingException thrown if there is an unrecoverable problem when processing the stage
     */
    protected abstract void doExecute(Collection<MetadataType> metadataCollection) throws StageProcessingException;

    /**
     * A strategy for filtering a metadata collection before or after it has been processed.
     * 
     * Implementations of this strategy <strong>should not</strong> modify the content of any element within the
     * collection. It should only remove elements from the collections.
     */
    public static interface MetadataFilteringStrategy<MetadataType extends Metadata<?>> {

        /**
         * Filters elements out of the collection.
         * 
         * @param metadataCollection collection to be filtered
         */
        public void filterMetadata(Collection<MetadataType> metadataCollection);
    }

    /** Implementation of {@link MetadataFilteringStrategy} that performs no filtering. */
    public static class NoOpFilteringStrategy implements MetadataFilteringStrategy<Metadata<?>> {

        /** {@inheritDoc} */
        public void filterMetadata(Collection<Metadata<?>> metadataCollection) {
            // nothing to do
        }
    }

    /**
     * A {@link MetadataFilteringStrategy} implementation that filters out metadata elements if they have a specific
     * type of {@link MetadataInfo} attached to them.
     * 
     * This is useful, for example, in removing all {@link Metadata} elements which have an associated
     * {@link net.shibboleth.metadata.ErrorStatusInfo}.
     */
    public static class MetadataInfoFilteringStrategy implements MetadataFilteringStrategy<Metadata<?>> {

        /** {@link MetadataInfo} classes that, if a {@link Metadata} contains, will be filtered out. */
        private Collection<Class<MetadataInfo>> filterRequirements;

        /**
         * Constructor.
         * 
         * @param metadataInfoClasses classes that, if a {@link Metadata} contains, will be filtered out; never null
         */
        public MetadataInfoFilteringStrategy(Collection<Class<MetadataInfo>> metadataInfoClasses) {
            Assert.isNotNull(metadataInfoClasses, "Metadata info classes may not be null");
            filterRequirements = new ArrayList<Class<MetadataInfo>>();
            for (Class<MetadataInfo> metadataInfoClass : metadataInfoClasses) {
                if (metadataInfoClass != null) {
                    filterRequirements.add(metadataInfoClass);
                }
            }
        }

        /**
         * Gets the classes that, if a {@link Metadata} contains, will be filtered out.
         * 
         * @return classes that, if a {@link Metadata} contains, will be filtered out
         */
        public Collection<Class<MetadataInfo>> getFilterRequirements() {
            return filterRequirements;
        }

        /** {@inheritDoc} */
        public void filterMetadata(Collection<Metadata<?>> metadataCollection) {
            Metadata<?> metadata;
            Iterator<Metadata<?>> metadataIterator = metadataCollection.iterator();
            while (metadataIterator.hasNext()) {
                metadata = metadataIterator.next();
                for (Class infoClass : filterRequirements) {
                    if (metadata.getMetadataInfo().containsKey(infoClass)) {
                        metadataIterator.remove();
                        break;
                    }
                }
            }
        }
    }
}