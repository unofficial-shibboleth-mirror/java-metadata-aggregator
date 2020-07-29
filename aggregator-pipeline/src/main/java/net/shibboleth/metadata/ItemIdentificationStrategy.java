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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Strategy for determining a human-readable identifier for a given {@link Item}.
 * 
 * <p>
 * All implementations of this interface <strong>must</strong> be thread-safe.
 * </p>
 *
 * @param <T> type of {@link Item} to be identified
 */
@ThreadSafe
public interface ItemIdentificationStrategy<T> {

    /**
     * Gets an identifier for the item.
     * 
     * @param item the item
     * 
     * @return the identifier, never <code>null</code>
     */
    @Nonnull String getItemIdentifier(@Nonnull Item<T> item);

}
