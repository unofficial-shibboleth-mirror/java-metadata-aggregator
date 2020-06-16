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
import javax.annotation.concurrent.Immutable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/** A {@link ItemMetadata} that associates a tag with a given {@link Item}. */
@Immutable
public class ItemTag implements ItemMetadata {

    /** Item tag. */
    @Nonnull @NotEmpty private final String tag;

    /**
     * Constructor.
     * 
     * @param itemTag a tag for an item, must not be either null or empty
     */
    public ItemTag(@Nonnull @NotEmpty final String itemTag) {
        tag = Constraint.isNotNull(StringSupport.trimOrNull(itemTag), "Tag may not be null or empty");
    }

    /**
     * Gets the tag for the item.
     * 
     * @return tag for the item, never null or empty
     */
    @Nonnull @NotEmpty public String getTag() {
        return tag;
    }
}
