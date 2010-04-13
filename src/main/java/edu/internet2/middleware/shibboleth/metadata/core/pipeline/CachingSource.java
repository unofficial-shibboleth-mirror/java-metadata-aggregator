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

package edu.internet2.middleware.shibboleth.metadata.core.pipeline;

import org.joda.time.DateTime;

import edu.internet2.middleware.shibboleth.metadata.core.MetadataElement;

/**
 * A {@link Source} which may cache its data for reuse later.
 * 
 * @param <ElementType> type of metadata element which is produced by this source
 */
public interface CachingSource<ElementType extends MetadataElement<?>> extends Source<ElementType> {

    /**
     * Gets whether source information is currently cached.
     * 
     * @return true if source information is currently cached, false if not
     */
    public boolean isCached();

    /**
     * Invalidates any currently cached information. Information will not be refreshed until the next request is made to
     * the source or the next time {@link #refreshCache()} is called.
     */
    public void invalidateCache();

    /** Invalidates and forces a refresh of the cache. */
    public void refreshCache();

    /**
     * Gets the date and time, in the UTC timezone, when the source information was cached.
     * 
     * @return date and time when the source information was cached, or null if no information is currently cached
     */
    public DateTime getCacheInstant();

    /**
     * Gets the date and time, in the UTC timezone, when the cached source information expires.
     * 
     * @return date and time when the cached source information expires or null if no information is currently cached
     */
    public DateTime getExpirationInstant();
}