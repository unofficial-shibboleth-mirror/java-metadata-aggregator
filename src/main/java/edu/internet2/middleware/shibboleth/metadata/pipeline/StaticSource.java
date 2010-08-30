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

package edu.internet2.middleware.shibboleth.metadata.pipeline;

import org.opensaml.util.Assert;

import edu.internet2.middleware.shibboleth.metadata.Metadata;
import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.SimpleMetadataCollection;

/** Adapts a collection of metadata to the {@link Source} interface. */
public class StaticSource<MetadataType extends Metadata<?>> extends AbstractComponent implements Source<MetadataType> {

    /** Metadata returned by this source. */
    private final MetadataCollection<MetadataType> source;

    /**
     * Constructor.
     * 
     * @param id ID of the source
     * @param wrappedSource metadata returned by this source
     */
    public StaticSource(String id, MetadataCollection<MetadataType> wrappedSource) {
        super(id);

        Assert.isNotNull(wrappedSource, "Source MetadataCollection may not be null");
        source = wrappedSource;
    }

    /**
     * Constructor.
     * 
     * @param id ID of the source
     * @param wrappedSource metadata returned by this source
     */
    public StaticSource(String id, MetadataType... metadatas) {
        super(id);
        source = new SimpleMetadataCollection<MetadataType>();
        for (MetadataType md : metadatas) {
            source.add(md);
        }
    }

    /** {@inheritDoc} */
    public MetadataCollection<MetadataType> execute() throws SourceProcessingException {
        return source;
    }
}