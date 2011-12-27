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

import net.shibboleth.metadata.util.ClassToInstanceMultiMap;
import net.shibboleth.metadata.util.ItemMetadataSupport;
import net.shibboleth.utilities.java.support.primitive.ObjectSupport;

/** A mock implementation of {@link Item}. */
public class MockItem extends AbstractItem<String> {

    /** Serial version UID. */
    private static final long serialVersionUID = 0L;

    /**
     * Constructor.
     * 
     * @param str data held by this item
     */
    public MockItem(String str) {
        setData(str);
    }

    /** {@inheritDoc} */
    public void setData(String data) {
        super.setData(data);
    }

    /**
     * Sets the metadata for this Item.
     * 
     * @param info metadata for this Item
     */
    public void setMetadataInfo(ClassToInstanceMultiMap<ItemMetadata> info) {
        getItemMetadata().clear();
        getItemMetadata().putAll(info);
    }

    /** {@inheritDoc} */
    public Item<String> copy() {
        MockItem clone = new MockItem(new String(unwrap()));
        ItemMetadataSupport.addToAll(clone, getItemMetadata().values().toArray(new ItemMetadata[] {}));
        return clone;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return unwrap().hashCode();
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof MockItem) {
            MockItem other = (MockItem) obj;
            return ObjectSupport.equals(unwrap(), other.unwrap());
        }
        return false;
    }
}