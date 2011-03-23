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

import java.io.OutputStream;
import java.util.Collection;

/**
 * Serializers convert a collection of {@link Metadata} in to an octet stream.
 * 
 * @param <MetadataType> type of metadata that can be serialized to an {@link OutputStream}
 */
public interface MetadataSerializer<MetadataType extends Metadata<?>> {

    /**
     * Serializes the metadata to the given output stream.
     * 
     * @param metadataCollection collection of metadata
     * @param output output stream to which metadata will be written
     */
    public void serialize(Collection<MetadataType> metadataCollection, OutputStream output);
}