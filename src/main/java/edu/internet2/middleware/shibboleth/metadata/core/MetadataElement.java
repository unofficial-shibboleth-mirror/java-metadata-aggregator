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

/**
 * A piece of metadata with associated processing information.
 * 
 * @param <MetadataType> type of metadata element
 */
public interface MetadataElement<MetadataType> extends Serializable {

    /**
     * Gets the metadata that describes the entity.
     * 
     * @return the metadata that describes the entity
     */
    public MetadataType getEntityMetadata();

    /**
     * Gets all of the processing data attached to this element.
     * 
     * @return processing data attached to this element
     */
    public MetadataElementInfo[] getElementInfo();

    /**
     * Gets an {@link MetadataElementInfo} of a specific type.
     * 
     * @param <T> class type of element info
     * @param infoType class instance of the element info type
     * 
     * @return the typed element info
     */
    public <T extends MetadataElementInfo> T getElementInfo(Class<T> infoType);

    /**
     * Adds a given {@link MetadataElementInfo} to this element. Only {@link MetadataElementInfo} of any given type may
     * be added.
     * 
     * @param info info to be added
     * 
     * @throws IllegalArgumentException thrown if an {@link MetadataElementInfo} of that type is already attached to
     *             this element
     */
    public void addElementInfo(MetadataElementInfo info) throws IllegalArgumentException;

    /**
     * Performs a clone of the element. All member fields, except {@link MetadataElementInfo}, should be deep cloned.
     * {@link MetadataElementInfo} objects must be shared between the clone and the original.
     * 
     * @return the clone of this element
     */
    public MetadataElement<MetadataType> clone();
}