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

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.Metadata;

/**
 * A stage which adds a static collection of metadata elements to a metadata collection.
 * 
 * @param <MetadataType> the type of metadata produced by this source
 */
@ThreadSafe
public class StaticMetadataSourceStage<MetadataType extends Metadata<?>> extends BaseStage<MetadataType> {

    /** Collection of static metadata added to each metadata collection by {@link #execute(Collection)}. */
    private Collection<MetadataType> source;

    /**
     * Gets the collection of static metadata elements added to the metadata collection by this stage.
     * 
     * @return collection of static metadata elements added to the metadata collection by this stage
     */
    public Collection<MetadataType> getSourceMetadata() {
        return source;
    }

    /**
     * Sets the collection of static metadata elements added to the metadata collection by this stage.
     * 
     * @param metadatas collection of static metadata elements added to the metadata collection by this stage
     */
    public synchronized void setSourceMetadata(final Collection<MetadataType> metadatas) {
        if (isInitialized()) {
            return;
        }

        if (metadatas == null || metadatas.isEmpty()) {
            source = Collections.emptyList();
        }

        source = new ArrayList<MetadataType>();
        for (MetadataType metadata : metadatas) {
            if (metadata != null) {
                source.add(metadata);
            }
        }
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<MetadataType> metadataCollection) throws StageProcessingException {
        for (MetadataType metadata : getSourceMetadata()) {
            if (metadata != null) {
                metadataCollection.add((MetadataType) metadata.copy());
            }
        }
    }
}