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

import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Item serializers convert an {@link Item} into an octet stream.
 * 
 * The caller is responsible for managing (opening, closing, etc.) the output stream
 * and orchestrating the serialization of collections of {@link Item}s.
 * 
 * @param <T> type of data contained in the item
 */
public interface ItemSerializer<T> {

    /**
     * Serializes the {@link Item} to the given output stream.
     * 
     * @param item {@link Item} to be serialized
     * @param output output stream to which the serialized {@link Item} will be written
     */
    public void serialize(@Nonnull Item<T> item, @Nonnull OutputStream output);
}