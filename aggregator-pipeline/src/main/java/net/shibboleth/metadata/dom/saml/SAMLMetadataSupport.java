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

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import net.shibboleth.metadata.dom.XMLSignatureSigningStage;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.w3c.dom.Element;

/** Helper class for dealing with SAML metadata. */
@ThreadSafe
public final class SAMLMetadataSupport {

    /** SAML Metadata namespace URI. */
    public static final String MD_NS = "urn:oasis:names:tc:SAML:2.0:metadata";

    /** Default SAML Metadata namespace prefix. */
    public static final String MD_PREFIX = "md";

    /** EntitiesDescriptor element name. */
    public static final QName ENTITIES_DESCRIPTOR_NAME = new QName(MD_NS, "EntitiesDescriptor", MD_PREFIX);

    /** EntityDescriptor element name. */
    public static final QName ENTITY_DESCRIPTOR_NAME = new QName(MD_NS, "EntityDescriptor", MD_PREFIX);

    /** Extensions element name. */
    public static final QName EXTENSIONS_NAME = new QName(MD_NS, "Extensions", MD_PREFIX);

    /** validUntil attribute name. */
    public static final QName VALID_UNTIL_ATTRIB_NAME = new QName("validUntil");

    /** cacheDuration attribute name. */
    public static final QName CACHE_DURATION_ATTRIB_NAME = new QName("cacheDuration");

    /** Constructor. */
    private SAMLMetadataSupport() {

    }

    /**
     * Check if the given element is an EntityDescriptor or EntitiesDescriptor.
     * 
     * @param e element to check
     * 
     * @return true if the element is an EntityDescriptor or EntitiesDescriptor
     */
    public static boolean isEntityOrEntitiesDescriptor(@Nullable final Element e) {
        return isEntitiesDescriptor(e) || isEntityDescriptor(e);
    }

    /**
     * Checks if the given element is an EntitiesDescriptor.
     * 
     * @param e element to check
     * 
     * @return true if the element is an EntitiesDescriptor, false otherwise
     */
    public static boolean isEntitiesDescriptor(@Nullable final Element e) {
        return ElementSupport.isElementNamed(e, ENTITIES_DESCRIPTOR_NAME);
    }

    /**
     * Checks if the given element is an EntityDescriptor.
     * 
     * @param e element to check
     * 
     * @return true if the element is an EntityDescriptor, false otherwise
     */
    public static boolean isEntityDescriptor(@Nullable final Element e) {
        return ElementSupport.isElementNamed(e, ENTITY_DESCRIPTOR_NAME);
    }

    /**
     * Gets the first instance of an extension element for a given descriptor.
     * 
     * @param descriptor the entity or entities descriptor, never null
     * @param extensionName the name of the extension element, never null
     * 
     * @return the first instance of the extension element or null if either argument is null, the given element is not
     *         an EntitiesDescriptor or EntityDescriptor, or no such element exists as an extension of the descriptor
     */
    public static Element getDescriptorExtensions(@Nullable final Element descriptor,
            @Nullable final QName extensionName) {
        if (descriptor == null || extensionName == null
                || (!isEntitiesDescriptor(descriptor) && !isEntityDescriptor(descriptor))) {
            return null;
        }

        List<Element> extensions = ElementSupport.getChildElements(descriptor, EXTENSIONS_NAME);
        if (extensions.isEmpty()) {
            return null;
        }

        List<Element> results = ElementSupport.getChildElements(extensions.get(0), extensionName);
        if (results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }

    /**
     * Adds a given extension to a given descriptor. If the given descriptor does not yet have an extensions element
     * then one is added, otherwise the given extension is added as after any existing extensions.
     * 
     * @param descriptor descriptor to which the extension will be added
     * @param extension extension to be added to the descriptor
     */
    public static void addDescriptorExtension(@Nullable final Element descriptor, @Nullable final Element extension) {
        if (descriptor == null || extension == null) {
            return;
        }

        if (!isEntitiesDescriptor(descriptor) && !isEntityDescriptor(descriptor)) {
            return;
        }

        Element extensionsElement;

        Map<QName, List<Element>> descriptorChildren = ElementSupport.getIndexedChildElements(descriptor);
        List<Element> extensionsElements = descriptorChildren.get(EXTENSIONS_NAME);
        if (extensionsElements.isEmpty()) {
            extensionsElement = ElementSupport.constructElement(descriptor.getOwnerDocument(), EXTENSIONS_NAME);

            Element insertExtensionsElementBefore = null;
            List<Element> signatureElements = descriptorChildren.get(XMLSignatureSigningStage.SIGNATURE_NAME);
            if (!signatureElements.isEmpty()) {
                Element lastSignatureElement = signatureElements.get(signatureElements.size() - 1);
                insertExtensionsElementBefore = ElementSupport.getNextSiblingElement(lastSignatureElement);
            } else {
                insertExtensionsElementBefore = ElementSupport.getFirstChildElement(descriptor);
            }

            if (insertExtensionsElementBefore == null) {
                descriptor.appendChild(extensionsElement);
            } else {
                descriptor.insertBefore(extensionsElement, insertExtensionsElementBefore);
            }
        } else {
            extensionsElement = extensionsElements.get(0);
        }

        ElementSupport.appendChildElement(extensionsElement, extension);
    }
}