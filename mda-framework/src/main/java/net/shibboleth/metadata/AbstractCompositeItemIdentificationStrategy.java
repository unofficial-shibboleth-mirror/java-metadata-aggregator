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
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.shared.annotation.constraint.NotEmpty;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.primitive.StringSupport;

/**
 * Abstract class implementing a generic two-part strategy for item identification.
 * 
 * Generating the two components of the identifier is delegated to subclasses.
 * The basic and extra identifiers are then combined into a composite identifier
 * for display.
 * 
 * If the basic identifier is <code>null</code>, a configured default is used.
 * 
 * If the extra identifier is <code>null</code>, only the basic identifier is used.
 *
 * @param <T> type of {@link Item} to be identified
 *
 * @since 0.9.0
 */
@ThreadSafe
public abstract class AbstractCompositeItemIdentificationStrategy<T> implements ItemIdentificationStrategy<T> {

    /** Identifier to use if an {@link Item} does not have an {@link ItemId}. Default value: "unidentified" */
    @Nonnull @NotEmpty @GuardedBy("this")
    private String noItemIdIdentifier = "unidentified";

    /**
     * Gets the identifier to use if an {@link Item} does not have an {@link ItemId}.
     * 
     * @return identifier to use if an {@link Item} does not have an {@link ItemId}
     */
    @Nonnull @NotEmpty public final synchronized String getNoItemIdIdentifier() {
        return noItemIdIdentifier;
    }

    /**
     * Sets the identifier to use if an {@link Item} does not have an {@link ItemId}.
     * 
     * @param identifier identifier to use if an {@link Item} does not have an {@link ItemId}
     */
    public synchronized void setNoItemIdIdentifier(@Nonnull @NotEmpty final String identifier) {
        noItemIdIdentifier =
                Constraint.isNotNull(StringSupport.trimOrNull(identifier), "Identifier can not be null or empty");
    }

    /**
     * Get a basic identifier for the {@link Item}.
     * 
     * @param item {@link Item} to extract a basic identifier from
     * @return a basic identifier for the {@link Item}, or <code>null</code>
     */
    @Nullable abstract String getBasicIdentifier(@Nonnull final Item<T> item);
    
    /**
     * Get an extra identifier for the {@link Item}.
     * 
     * @param item {@link Item} to extract an extra identifier from
     * @return an extra identifier for the {@link Item}, or <code>null</code>
     */
    @Nullable abstract String getExtraIdentifier(@Nonnull final Item<T> item);

    @Override
    @Nonnull public String getItemIdentifier(@Nonnull final Item<T> item) {
        Constraint.isNotNull(item, "Item can not equal null");

        final StringBuilder res = new StringBuilder();
        final String basic = getBasicIdentifier(item);
        if (basic == null) {
            res.append(getNoItemIdIdentifier());
        } else {
            res.append(basic);
        }
        
        final String extra = getExtraIdentifier(item);
        if (extra != null) {
            res.append(" (");
            res.append(extra);
            res.append(")");
        }

        final var result = res.toString();
        assert result != null;
        return result;
    }

}
