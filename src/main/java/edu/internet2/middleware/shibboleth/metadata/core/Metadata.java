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

import java.io.Serializable;

import edu.internet2.middleware.shibboleth.metadata.util.ClassToInstanceMultiMap;

/**
 * A piece of metadata with associated processing information.
 * 
 * @param <MetadataType> type of metadata element
 */
public interface Metadata<MetadataType> extends Serializable {

    /**
     * Gets the metadata.
     * 
     * @return the metadata
     */
    public MetadataType getMetadata();

    /**
     * Gets all of the processing data attached to this element.
     * 
     * @return processing data attached to this element
     */
    public ClassToInstanceMultiMap<MetadataInfo> getMetadataInfo();

    /**
     * Performs a copy of the element. All member fields, except {@link MetadataInfo}, should be deep cloned.
     * {@link MetadataInfo} objects must be shared between the clone and the original.
     * 
     * @param <T> the type of metadata element returned
     * 
     * @return the clone of this element
     */
    public Metadata<MetadataType> copy();
}