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

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import com.google.common.base.Objects;

/**
 * Carries a unique identifier for the data carried by an {@link net.shibboleth.metadata.Item}.
 * 
 * An {@link net.shibboleth.metadata.Item} may have more than one {@link ItemId}, but should not
 * have the same {@link ItemId} as any other {@link net.shibboleth.metadata.Item} in a given
 * context.
 */
@ThreadSafe
public class ItemId implements ItemMetadata, Comparable<ItemId> {

    /** Serial version UID. */
    private static final long serialVersionUID = -3907907112463674533L;

    /** Unique ID for the Item. */
    private String id;

    /**
     * Constructor.
     * 
     * @param itemId a unique identifier for the entity, never null
     */
    public ItemId(@Nonnull @NotEmpty final String itemId) {
        id = Constraint.isNotNull(StringSupport.trimOrNull(itemId), "Item ID may not be null or empty");
    }

    /**
     * Gets a unique identifier for the data carried by the Item.
     * 
     * @return unique identifier for the data carried by the Item
     */
    @Nonnull public String getId() {
        return id;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return id.hashCode();
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ItemId)) {
            return false;
        }

        ItemId other = (ItemId) obj;
        return Objects.equal(id, other.id);
    }

    /** {@inheritDoc} */
    public int compareTo(ItemId o) {
        return id.compareTo(o.id);
    }
}