/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Item collection serializers convert a {@link Collection} of {@link Item}s into an octet stream.
 * 
 * The caller is responsible for managing (opening, closing, etc.) the output stream.
 * 
 * @param <T> type of data contained in each item
 */
public interface ItemCollectionSerializer<T> {

    /**
     * Serializes the collection of {@link Item}s to the given output stream.
     * 
     * @param items {@link Collection} of {@link Item}s to be serialized
     * @param output output stream to which the serialized form of the {@link Item}
     *  collection will be written
     * 
     * @throws IOException if an I/O exception occurs during serialization
     */
    public void serializeCollection(@Nonnull Collection<Item<T>> items, @Nonnull OutputStream output)
        throws IOException;

}
