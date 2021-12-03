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

package net.shibboleth.metadata.dom.saml.mdrpi;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.FirstItemIdItemIdentificationStrategy;
import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;

/**
 * Item identification strategy for interfederation use cases.
 * 
 * The basic identifier is taken from {@link FirstItemIdItemIdentificationStrategy}.
 * 
 * The extra identifier is based on a {@link RegistrationAuthority} if one of
 * those is present.  The extra identifier is omitted if it is present in a
 * specified blacklist, and it can be mapped to a simpler value for display if
 * desired.
 *
 * @param <T> type of {@link Item} to be identified
 *
 * @since 0.9.0
 */
@ThreadSafe
public class RegistrationAuthorityItemIdentificationStrategy<T> extends FirstItemIdItemIdentificationStrategy<T> {

    /**
     * Set of registration authorities to be ignored.
     */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private Set<String> ignoredRegistrationAuthorities = Set.of();
    
    /**
     * Replacement display names for registration authorities.
     */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private Map<String, String> registrationAuthorityDisplayNames = Map.of();
    
    /**
     * Returns the set of registration authorities we are ignoring.
     * 
     * @return {@link Set} of registration authority names.
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized Collection<String> getIgnoredRegistrationAuthorities() {
        return ignoredRegistrationAuthorities;
    }

    /**
     * Set the set of registration authorities we are ignoring.
     * 
     * @param registrars {@link Set} of registration authority names to ignore.
     */
    public synchronized void setIgnoredRegistrationAuthorities(
            @Nonnull @NonnullElements @Unmodifiable final Collection<String> registrars) {
        ignoredRegistrationAuthorities = Set.copyOf(registrars);
    }

    /**
     * Returns the map of display names for registration authorities.
     * 
     * @return {@link Map} of display names for authorities.
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized Map<String, String> getRegistrationAuthorityDisplayNames() {
        return registrationAuthorityDisplayNames;
    }

    /**
     * Sets the map of display names for registration authorities.
     * 
     * @param names {@link Map} of display names for registration authorities.
     */
    public synchronized void setRegistrationAuthorityDisplayNames(
            @Nonnull @NonnullElements @Unmodifiable final Map<String, String> names) {
        registrationAuthorityDisplayNames = Map.copyOf(names);
    }
    
    /**
     * Derive a display name for an entity's registration authority, if it has one.
     * 
     * @param item {@link Item} to derive an identifier for.
     * 
     * @return registration authority name, or <code>null</code>.
     */
    @Override
    @Nullable protected String getExtraIdentifier(@Nonnull final Item<T> item) {
        final List<RegistrationAuthority> regAuths = item.getItemMetadata().get(RegistrationAuthority.class);
        
        // nothing to return if there isn't a registration authority
        if (regAuths.isEmpty()) {
            return null;
        }
        
        final String regAuth = regAuths.get(0).getRegistrationAuthority();
        
        // nothing to return if it's an ignored authority
        if (getIgnoredRegistrationAuthorities().contains(regAuth)) {
            return null;
        }
        
        // handle mapping it to a simpler form if that's available
        final String displayName = getRegistrationAuthorityDisplayNames().get(regAuth);
        if (displayName != null) {
            return displayName;
        }
        return regAuth;
    }

}
