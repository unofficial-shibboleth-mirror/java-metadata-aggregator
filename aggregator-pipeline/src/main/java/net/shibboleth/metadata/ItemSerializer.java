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
import java.util.Collection;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

/**
 * Serializers convert a collection of {@link Item} in to an octet stream.
 * 
 * @param <T> type of data contained in the items
 */
public interface ItemSerializer<T> {

    /**
     * Serializes the Item to the given output stream.
     * 
     * @param itemCollection collection of Items
     * @param output output stream to which the Item will be written
     */
    public void serialize(@Nonnull @NonnullElements final Collection<Item<T>> itemCollection,
            @Nonnull final OutputStream output);
}