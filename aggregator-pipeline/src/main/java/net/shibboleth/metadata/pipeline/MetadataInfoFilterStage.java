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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.MetadataInfo;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazyList;

/**
 * A {@link Stage} that filters out metadata elements if they have a specific type of {@link MetadataInfo} attached to
 * them.
 * 
 * This is useful, for example, in removing all {@link Metadata} elements which have an associated
 * {@link net.shibboleth.metadata.ErrorStatusInfo}.
 */
public class MetadataInfoFilterStage extends BaseStage<Metadata<?>> {

    /** {@link MetadataInfo} classes that, if a {@link Metadata} contains, will be filtered out. */
    private Collection<Class<MetadataInfo>> filterRequirements = Collections.emptyList();

    /**
     * Gets the {@link MetadataInfo} classes that, if a {@link Metadata} contains, will be filtered out.
     * 
     * @return {@link MetadataInfo} classes that, if a {@link Metadata} contains, will be filtered out, never null nor
     *         containing null elements
     */
    public Collection<Class<MetadataInfo>> getFilterRequirements() {
        return filterRequirements;
    }

    /**
     * Sets the {@link MetadataInfo} classes that, if a {@link Metadata} contains, will be filtered out.
     * 
     * @param requirements {@link MetadataInfo} classes that, if a {@link Metadata} contains, will be filtered out, may
     *            be null or contain null elements
     */
    public void setFilterRequirements(Collection<Class<MetadataInfo>> requirements) {
        if (isInitialized()) {
            return;
        }
        filterRequirements = Collections.unmodifiableList(CollectionSupport.addNonNull(requirements,
                new LazyList<Class<MetadataInfo>>()));
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<Metadata<?>> metadataCollection) throws StageProcessingException {
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