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

package edu.internet2.middleware.shibboleth.metadata.util;

import edu.internet2.middleware.shibboleth.metadata.Metadata;
import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.MetadataInfo;

/** Helper class for dealing with {@link MetadataInfo} operations. */
public class MetadataInfoHelper {

    /**
     * Adds all the give {@link MetadataInfo} items to each {@link Metadata} element in the given collection.
     * 
     * @param metadataCollection collection of {@link Metadata} elements
     * @param infos collection of {@link MetadataInfo} items to be added to each {@link Metadata} element of the given
     *            collection
     */
    public static void addToAll(MetadataCollection<?> metadataCollection, MetadataInfo... infos) {
        if (metadataCollection == null || infos == null || infos.length == 0) {
            return;
        }

        for (Metadata<?> metadata : metadataCollection) {
            addToAll(metadata, infos);
        }
    }

    /**
     * Adds all the given {@link MetadataInfo} items to the given {@link Metadata} element.
     * 
     * @param metadata element to which {@link MetadataInfo} will be added
     * @param infos {@link MetadataInfo} to be added to the metadata element
     */
    public static void addToAll(Metadata<?> metadata, MetadataInfo... infos) {
        if (metadata == null || infos == null || infos.length == 0) {
            return;
        }

        for (MetadataInfo info : infos) {
            metadata.getMetadataInfo().put(info);
        }
    }
}