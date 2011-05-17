/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.opensaml.util.StringSupport;

/** A {@link ItemMetadata} that associates a tag with a given {@link Item}. */
@ThreadSafe
public class ItemTag implements ItemMetadata {

    /** Serial version UID. */
    private static final long serialVersionUID = 4727990464411083511L;

    /** Item tag. */
    private final String tag;

    /**
     * Constructor.
     * 
     * @param itemTag a tag for a metadata element
     */
    public ItemTag(final String itemTag) {
        tag = StringSupport.trimOrNull(itemTag);
        Assert.isNotNull(tag, "Tag may not be null or empty");
    }

    /**
     * Gets the tag for the metadata element.
     * 
     * @return tag for the metadata element, never null
     */
    public String getTag() {
        return tag;
    }
}