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

import java.util.Collection;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.SimpleMetadataCollection;

import org.opensaml.util.collections.CollectionSupport;


/**
 * Adapts a collection of metadata to the {@link Source} interface. Each metadata element is cloned each time
 * {@link #execute()} is invoked.
 */
@ThreadSafe
public class StaticSource<MetadataType extends Metadata<?>> extends AbstractComponent implements Source<MetadataType> {

    /** Metadata returned by this source. */
    private MetadataCollection<MetadataType> source = new SimpleMetadataCollection<MetadataType>();

    /**
     * Gets the collection of metadata elements "produced" by this source.
     * 
     * @return collection of metadata elements "produced" by this source
     */
    public Collection<MetadataType> getSourceMetadata() {
        return source;
    }

    /**
     * Sets the collection of metadata elements "produced" by this source.
     * 
     * @param metadatas collection of metadata elements "produced" by this source
     */
    public synchronized void setSourceMetadata(final Collection<MetadataType> metadatas) {
        if (isInitialized()) {
            return;
        }
        source = CollectionSupport.addNonNull(metadatas, new SimpleMetadataCollection<MetadataType>());
    }

    /** {@inheritDoc} */
    public MetadataCollection<MetadataType> execute() throws SourceProcessingException {
        return source.copy();
    }
}