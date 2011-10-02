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

package net.shibboleth.metadata.dom.saml;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazySet;
import org.opensaml.util.xml.AttributeSupport;
import org.opensaml.util.xml.ElementSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/** A pipeline stage that will filter EntityDescriptor or EntityDescriptors based on their registration authority. */
public class EntityRegistrationAuthorityFilterStage extends BaseIteratingStage<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EntityRegistrationAuthorityFilterStage.class);

    /** Whether a descriptor is required to have registration information. Default value: false */
    private boolean requiringRegistrationInformation;

    /** Registrars which are white/black listed depending on the value of {@link #whitelistingAuthorities}. */
    private Collection<String> designatedAuthorities = new LazySet<String>();

    /** Whether {@link #designatedAuthorities} should be considered a whitelist or a blacklist. Default value: false */
    private boolean whitelistingAuthorities;

    /** Whether EntitiesDescriptor that do not contain EntityDescriptors should be removed. Default value: true */
    private boolean removingEntitylessEntitiesDescriptor = true;

    /**
     * Gets whether a descriptor is required to have registration information.
     * 
     * @return whether a descriptor is required to have registration information
     */
    public boolean isRequiringRegistrationInformation() {
        return requiringRegistrationInformation;
    }

    /**
     * Sets whether a descriptor is required to have registration information.
     * 
     * @param isRequired whether a descriptor is required to have registration information
     */
    public synchronized void setRequiringRegistrationInformation(boolean isRequired) {
        if (isInitialized()) {
            return;
        }
        requiringRegistrationInformation = isRequired;
    }

    /**
     * Gets the list of designated registration authority.
     * 
     * @return list of designated registration authority, never null
     */
    public Collection<String> getDesignatedRegistrationAuthorities() {
        return designatedAuthorities;
    }

    /**
     * Sets the list of designated registration authority.
     * 
     * @param authorities list of designated registration authority
     */
    public synchronized void setDesignatedRegistrationAuthorities(final Collection<String> authorities) {
        if (isInitialized()) {
            return;
        }

        designatedAuthorities = CollectionSupport.addNonNull(authorities, new LazySet<String>());
    }

    /**
     * Whether the list of designated registration authority should be considered a whitelist.
     * 
     * @return true if the designated registration authority should be considered a whitelist, false otherwise
     */
    public boolean isWhitelistingRegistrationAuthorities() {
        return whitelistingAuthorities;
    }

    /**
     * Sets whether the list of designated registration authority should be considered a whitelist.
     * 
     * @param whitelisting true if the designated registration authority should be considered a whitelist, false
     *            otherwise
     */
    public synchronized void setWhitelistingRegistrationAuthorities(final boolean whitelisting) {
        if (isInitialized()) {
            return;
        }

        whitelistingAuthorities = whitelisting;
    }

    /**
     * Gets whether EntitiesDescriptor that do not contain EntityDescriptors should be removed.
     * 
     * @return whether EntitiesDescriptor that do not contain EntityDescriptors should be removed
     */
    public boolean isRemovingEntitylessEntitiesDescriptor() {
        return removingEntitylessEntitiesDescriptor;
    }

    /**
     * Sets whether EntitiesDescriptor that do not contain EntityDescriptors should be removed.
     * 
     * @param remove whether EntitiesDescriptor that do not contain EntityDescriptors should be removed
     */
    public synchronized void setRemovingEntitylessEntitiesDescriptor(final boolean remove) {
        if (isInitialized()) {
            return;
        }

        removingEntitylessEntitiesDescriptor = remove;
    }

    /** {@inheritDoc} */
    protected boolean doExecute(DomElementItem item) {
        Element descriptor;
        descriptor = item.unwrap();
        if (SamlMetadataSupport.isEntitiesDescriptor(descriptor)) {
            if (processEntitiesDescriptor(descriptor)) {
                return false;
            }
        } else if (SamlMetadataSupport.isEntityDescriptor(descriptor)) {
            return !filterOutDescriptor(descriptor);
        }

        return true;
    }

    /**
     * Iterates over all child EntitiesDescriptors and EntityDescriptor to see if they should be removed. Also removed
     * EntitiesDescriptor that no longer contain EntityDescriptor descendants after filtering if
     * {@link #isRemovingEntitylessEntitiesDescriptor()}.
     * 
     * @param entitiesDescriptor EntitiesDescriptor being processed
     * 
     * @return true if the descriptor should be removed, false otherwise
     */
    protected boolean processEntitiesDescriptor(final Element entitiesDescriptor) {
        Iterator<Element> descriptorItr;
        Element descriptor;

        if (filterOutDescriptor(entitiesDescriptor)) {
            return true;
        }

        final List<Element> childEntitiesDescriptors =
                ElementSupport.getChildElements(entitiesDescriptor, SamlMetadataSupport.ENTITIES_DESCRIPTOR_NAME);
        descriptorItr = childEntitiesDescriptors.iterator();
        while (descriptorItr.hasNext()) {
            descriptor = descriptorItr.next();
            if (processEntitiesDescriptor(descriptor)) {
                entitiesDescriptor.removeChild(descriptor);
                descriptorItr.remove();
            }
        }

        final List<Element> childEntityDescriptors =
                ElementSupport.getChildElements(entitiesDescriptor, SamlMetadataSupport.ENTITY_DESCRIPTOR_NAME);
        descriptorItr = childEntityDescriptors.iterator();
        while (descriptorItr.hasNext()) {
            descriptor = descriptorItr.next();
            if (filterOutDescriptor(descriptor)) {
                entitiesDescriptor.removeChild(descriptor);
                descriptorItr.remove();
            }
        }

        if (removingEntitylessEntitiesDescriptor && childEntitiesDescriptors.isEmpty()
                && childEntityDescriptors.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * Determines if a given should be filtered out. A descriptor is filtered out if registration information is
     * required by the descriptor does not have it, registration information is present but does not contain the
     * required authority attribute, registrars are being whitelisted and the descriptor's registration authority is not
     * in the whitelist, or registrars are being blacklisted and the descriptor's registration authority is in the
     * blacklist.
     * 
     * @param descriptor the descriptor
     * 
     * @return true if the descriptor should be filtered out
     */
    protected boolean filterOutDescriptor(Element descriptor) {
        Element registrationInfoElement =
                SamlMetadataSupport.getDescriptorExtensions(descriptor, new QName(SamlMetadataSupport.RPI_NS,
                        "RegistrationInfo"));
        if (registrationInfoElement == null) {
            if (requiringRegistrationInformation) {
                log.debug(
                        "{} pipeline stage removing Item because it did not have required registration information extension",
                        getId());
                return true;
            } else {
                return false;
            }
        }

        String registrationAuthority =
                AttributeSupport.getAttributeValue(registrationInfoElement, null, "registrationAuthority");
        if (registrationAuthority == null) {
            log.debug(
                    "{} pipeline stage removing Item because it contained a registration info extension but no authority attribute",
                    getId());
            return true;
        }

        if (whitelistingAuthorities && !designatedAuthorities.contains(registrationAuthority)) {
            log.debug("{} pipeline stage removing Item because its registration authority was not on the whitelist",
                    getId());
            return true;
        }

        if (!whitelistingAuthorities && designatedAuthorities.contains(registrationAuthority)) {
            log.debug("{} pipeline stage removing Item because its registration authority was on the blacklist",
                    getId());
            return true;
        }

        return false;
    }
}