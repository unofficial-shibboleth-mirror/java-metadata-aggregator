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

package net.shibboleth.metadata.dom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.utilities.java.support.component.ComponentSupport;

/**
 * A stage which removes all evidence of a given collection of XML namespaces from each metadata item.
 * 
 * The stage can operate either to blacklist (the default) or whitelist the collection of namespaces.
 * 
 * Elements, attributes and namespace prefix definitions associated with a given namespace will be removed
 * or retained depending on the {@link #whitelisting} property.
 * 
 * Attributes without an explicit namespace prefix will never be removed by this stage.
 */
@ThreadSafe
public class NamespacesStrippingStage extends AbstractNamespacesStrippingStage {

    /**
     * XML namespaces to whitelist or blacklist.
     */
    private final Set<String> namespaces = new HashSet<>();

    /**
     * Whether we are whitelisting or blacklisting (default: blacklisting).
     */
    private boolean whitelisting;

    /**
     * Gets the collection of namespaces being blacklisted or whitelisted.
     * 
     * @return collection of namespaces being removed
     */
    @Nonnull
    public Collection<String> getNamespaces() {
        final Set<String> result = new HashSet<>();
        result.addAll(namespaces);
        return result;
    }
    
    /**
     * Sets the collection of namespaces to blacklist or whitelist.
     * 
     * @param nss collection of namespaces
     */
    public void setNamespaces(@Nonnull final Collection<String> nss) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        namespaces.clear();
        namespaces.addAll(nss);
    }

    /**
     * Indicate whether the stage is whitelisting namespaces or blacklisting (the default).
     * 
     * @return <code>true</code> for whitelisting, <code>false</code> for blacklisting (the default)
     */
    public boolean isWhitelisting() {
        return whitelisting;
    }

    /**
     * Set whether the stage is whitelisting namespaces.
     * 
     * @param wl <code>true</code> for whitelisting, <code>false</code> for blacklisting
     */
    public void setWhitelisting(final boolean wl) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        whitelisting = wl;
    }
    
    @Override
    protected boolean removingNamespace(final String namespace) {
        return whitelisting ^ namespaces.contains(namespace);
    }

    @Override
    protected void doDestroy() {
        namespaces.clear();

        super.doDestroy();
    }

}
