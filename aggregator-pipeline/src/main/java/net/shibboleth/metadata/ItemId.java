/*
 * Licensed to the University Corporation for Advanced Internet Development, Inc.
 * under one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache 
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

import net.jcip.annotations.ThreadSafe;

import org.opensaml.util.Assert;
import org.opensaml.util.ObjectSupport;
import org.opensaml.util.StringSupport;

/** Carries a unique identifier for the data carried by an Item. */
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
    public ItemId(final String itemId) {
        id = StringSupport.trimOrNull(itemId);
        Assert.isNotNull(id, "Item ID may not be null or empty");
    }

    /**
     * Gets a unique identifier for the data carried by the Item.
     * 
     * @return unique identifier for the data carried by the Item
     */
    public String getId() {
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
        return ObjectSupport.equals(id, other.id);
    }

    /** {@inheritDoc} */
    public int compareTo(ItemId o) {
        return id.compareTo(o.id);
    }
}