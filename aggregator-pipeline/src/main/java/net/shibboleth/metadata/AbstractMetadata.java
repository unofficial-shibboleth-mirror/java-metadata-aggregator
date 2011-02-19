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

package net.shibboleth.metadata;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.util.ClassToInstanceMultiMap;

/**
 * Base implementation of a metadata element.
 * 
 * @param <MetadataType> type of metadata element
 */
@ThreadSafe
public abstract class AbstractMetadata<MetadataType> implements Metadata<MetadataType> {

    /** Serial version UID. */
    private static final long serialVersionUID = 7706700116965553475L;

    /** The actual metadata. */
    private MetadataType metadata;

    /** Additional information associated with the metadata. */
    private final ClassToInstanceMultiMap<MetadataInfo> elementInfo;

    /** Constructor. */
    protected AbstractMetadata() {
        elementInfo = new ClassToInstanceMultiMap<MetadataInfo>(true);
    }

    /** {@inheritDoc} */
    public MetadataType getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata represented by this element.
     * 
     * @param entityMetadata the metadata
     */
    protected synchronized void setMetadata(final MetadataType entityMetadata) {
        metadata = entityMetadata;
    }

    /** {@inheritDoc} */
    public ClassToInstanceMultiMap<MetadataInfo> getMetadataInfo() {
        return elementInfo;
    }
}