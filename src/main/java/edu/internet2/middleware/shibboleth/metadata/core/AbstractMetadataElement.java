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

package edu.internet2.middleware.shibboleth.metadata.core;

import net.jcip.annotations.ThreadSafe;

/**
 * Base implementation of a metadata element.
 * 
 * @param <MetadataType> type of metadata element
 */
@ThreadSafe
public abstract class AbstractMetadataElement<MetadataType> implements MetadataElement<MetadataType> {

    /** The actual metadata. */
    private MetadataType metadata;

    /** {@inheritDoc} */
    public MetadataType getEntityMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata represented by this element.
     * 
     * @param entityMetadata the metadata
     */
    protected void setEntityMetadata(MetadataType entityMetadata) {
        metadata = entityMetadata;
    }

    /** {@inheritDoc} */
    public MetadataElementInfo[] getElementInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public <T extends MetadataElementInfo> T getElementInfo(Class<T> infoType) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public void addElementInfo(MetadataElementInfo info) throws IllegalArgumentException {
        // TODO Auto-generated method stub

    }
}