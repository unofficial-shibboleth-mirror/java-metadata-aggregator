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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.ds.XMLDSIGSupport;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.xml.ElementSupport;

/** Helper class for dealing with SAML metadata. */
@ThreadSafe
public final class SAMLMetadataSupport {

    /** SAML Metadata namespace URI. */
    public static final @Nonnull String MD_NS = "urn:oasis:names:tc:SAML:2.0:metadata";

    /** Default SAML Metadata namespace prefix. */
    public static final @Nonnull String MD_PREFIX = "md";

    /** EntitiesDescriptor element name. */
    public static final @Nonnull QName ENTITIES_DESCRIPTOR_NAME = new QName(MD_NS, "EntitiesDescriptor", MD_PREFIX);

    /** EntityDescriptor element name. */
    public static final @Nonnull QName ENTITY_DESCRIPTOR_NAME = new QName(MD_NS, "EntityDescriptor", MD_PREFIX);

    /** Extensions element name. */
    public static final @Nonnull QName EXTENSIONS_NAME = new QName(MD_NS, "Extensions", MD_PREFIX);

    /** validUntil attribute name. */
    public static final @Nonnull QName VALID_UNTIL_ATTRIB_NAME = new QName("validUntil");

    /** cacheDuration attribute name. */
    public static final @Nonnull QName CACHE_DURATION_ATTRIB_NAME = new QName("cacheDuration");

    /**
     * QName of the RoleDescriptor element.
     *
     * @since 0.10.0
     */
    public static final @Nonnull QName ROLE_DESCRIPTOR_NAME = new QName(MD_NS, "RoleDescriptor");

    /**
     * QName of the IDPSSODescriptor element.
     *
     * @since 0.10.0
     */
    public static final @Nonnull QName IDP_SSO_DESCRIPTOR_NAME = new QName(MD_NS, "IDPSSODescriptor");

    /**
     * QName of the SPSSODescriptor element.
     *
     * @since 0.10.0
     */
    public static final @Nonnull QName SP_SSO_DESCRIPTOR_NAME = new QName(MD_NS, "SPSSODescriptor");

    /**
     * QName of the AuthnAuthorityDescriptor element.
     *
     * @since 0.10.0
     */
    public static final @Nonnull QName AUTHN_AUTHORITY_DESCRIPTOR_NAME = new QName(MD_NS,
            "AuthnAuthorityDescriptor");

    /**
     * QName of the AttributeAuthorityDescriptor element.
     *
     * @since 0.10.0
     */
    public static final @Nonnull QName ATTRIBUTE_AUTHORITY_DESCRIPTOR_NAME = new QName(MD_NS,
            "AttributeAuthorityDescriptor");

    /**
     * QName of the PDPDescriptor element.
     *
     * @since 0.10.0
     */
    public static final @Nonnull QName PDP_DESCRIPTOR_NAME = new QName(MD_NS, "PDPDescriptor");

    /**
     * QName of the Organization element.
     *
     * @since 0.10.0
     */
    public static final @Nonnull QName ORGANIZATION_NAME = new QName(MD_NS, "Organization", MD_PREFIX);

    /**
     * QName of the OrganizationName element.
     *
     * @since 0.10.0
     */
    public static final @Nonnull QName ORGANIZATIONNAME_NAME = new QName(MD_NS, "OrganizationName", MD_PREFIX);

    /**
     * QName of the OrganizationDisplayName element.
     *
     * @since 0.10.0
     */
    public static final @Nonnull QName ORGANIZATIONDISPLAYNAME_NAME =
            new QName(MD_NS, "OrganizationDisplayName", MD_PREFIX);

    /**
     * QName of the OrganizationURL element.
     *
     * @since 0.10.0
     */
    public static final @Nonnull QName ORGANIZATIONURL_NAME = new QName(MD_NS, "OrganizationURL", MD_PREFIX);

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
    public static boolean isEntityOrEntitiesDescriptor(@Nonnull final Element e) {
        return isEntitiesDescriptor(e) || isEntityDescriptor(e);
    }

    /**
     * Checks if the given element is an EntitiesDescriptor.
     * 
     * @param e element to check
     * 
     * @return true if the element is an EntitiesDescriptor, false otherwise
     */
    public static boolean isEntitiesDescriptor(@Nonnull final Element e) {
        return ElementSupport.isElementNamed(e, ENTITIES_DESCRIPTOR_NAME);
    }

    /**
     * Checks if the given element is an EntityDescriptor.
     * 
     * @param e element to check
     * 
     * @return true if the element is an EntityDescriptor, false otherwise
     */
    public static boolean isEntityDescriptor(@Nonnull final Element e) {
        return ElementSupport.isElementNamed(e, ENTITY_DESCRIPTOR_NAME);
    }

    /**
     * Gets a list of all instances of an extension element for a given descriptor.
     * 
     * An empty list is returned if the descriptor has no extensions, or if it
     * has no extensions of the requested type.
     * 
     * @param descriptor the descriptor, never <code>null</code>
     * @param extensionName name of the extension element, never <code>null</code>
     * @return a possibly empty list of all extension instances
     */
    @Nonnull
    public static List<Element> getDescriptorExtensionList(@Nonnull final Element descriptor,
            @Nonnull final QName extensionName) {
        Constraint.isNotNull(descriptor, "descriptor may not be null");
        Constraint.isNotNull(extensionName, "extension name may not be null");

        // Locate the Extensions element
        final List<Element> extensions = ElementSupport.getChildElements(descriptor, EXTENSIONS_NAME);
        if (extensions.isEmpty()) {
            return extensions;
        }
        
        final var exten = extensions.get(0);
        assert exten != null;
        return ElementSupport.getChildElements(exten, extensionName);
    }

    /**
     * Gets the first instance of an extension element for a given descriptor.
     * 
     * @param descriptor the descriptor, never <code>null</code>
     * @param extensionName the name of the extension element, never <code>null</code>
     * 
     * @return the first instance of the extension element or <code>null</code> if
     *          no such element exists as an extension of the descriptor
     *
     * @since 0.10.0
     */
    public static Element getDescriptorExtension(@Nonnull final Element descriptor,
            @Nonnull final QName extensionName) {
        final List<Element> results = getDescriptorExtensionList(descriptor, extensionName);
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

        final @Nonnull Element extensionsElement;

        final Map<QName, List<Element>> descriptorChildren = ElementSupport.getIndexedChildElements(descriptor);
        final List<Element> extensionsElements = descriptorChildren.get(EXTENSIONS_NAME);
        if (extensionsElements.isEmpty()) {
            final var ownerDocument = descriptor.getOwnerDocument();
            assert ownerDocument != null;
            extensionsElement = ElementSupport.constructElement(ownerDocument, EXTENSIONS_NAME);

            Element insertExtensionsElementBefore = null;
            final List<Element> signatureElements = descriptorChildren.get(XMLDSIGSupport.SIGNATURE_NAME);
            if (!signatureElements.isEmpty()) {
                final Element lastSignatureElement = signatureElements.get(signatureElements.size() - 1);
                assert lastSignatureElement != null;
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
            final var exten = extensionsElements.get(0);
            assert exten != null;
            extensionsElement = exten;
        }

        ElementSupport.appendChildElement(extensionsElement, extension);
    }
}
