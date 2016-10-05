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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Strategy that returns the first {@link ItemId} associated with an {@link Item} or, if no {@link ItemId} is
 * associated with the item, a generic identifier is returned.
 */
public class FirstItemIdItemIdentificationStrategy extends AbstractCompositeItemIdentificationStrategy {

    @Override
    @Nullable protected String getBasicIdentifier(@Nonnull final Item<?> item) {
        final List<ItemId> itemIds = item.getItemMetadata().get(ItemId.class);
        if (!itemIds.isEmpty()) {
            return itemIds.get(0).getId();
        } else {
            return null;
        }
    }

    @Override
    @Nullable protected String getExtraIdentifier(@Nonnull final Item<?> item) {
        return null;
    }

}
