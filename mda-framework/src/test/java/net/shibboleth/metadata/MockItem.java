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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/** A mock implementation of {@link Item}. */
@NotThreadSafe
public class MockItem extends AbstractItem<String> {

    /**
     * Constructor.
     * 
     * @param str data held by this item
     */
    public MockItem(@Nonnull String str) {
        super(str);
    }

    @Override
    public @Nonnull Item<String> copy() {
        final MockItem clone = new MockItem(new String(unwrap()));
        clone.getItemMetadata().putAll(getItemMetadata());
        return clone;
    }

    @Override
    public int hashCode() {
        return unwrap().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof MockItem) {
            final MockItem other = (MockItem) obj;
            return Objects.equals(unwrap(), other.unwrap());
        }
        return false;
    }
}
