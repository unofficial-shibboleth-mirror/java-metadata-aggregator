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

import java.util.Collection;

import javax.annotation.Nonnull;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

/**
 * A {@link CollectionMergeStrategy} that adds the Item from each source, in order, by means of the
 * {@link Collection#addAll(Collection)} method on the target.
 */
public class SimpleCollectionMergeStrategy implements CollectionMergeStrategy {

    /** {@inheritDoc} */
    public void mergeCollection(@Nonnull @NonnullElements final Collection<Item<?>> target,
            @Nonnull @NonnullElements final Collection<Item<?>>... sources) {
        assert target != null : "Target collection can not be null";
        assert sources != null && sources.length > 0 : "Source collections can not be null or empty";

        for (Collection<Item<?>> source : sources) {
            target.addAll(source);
        }
    }
}