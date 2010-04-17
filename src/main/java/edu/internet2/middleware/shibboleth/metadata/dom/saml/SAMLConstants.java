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

package edu.internet2.middleware.shibboleth.metadata.dom.saml;

import javax.xml.namespace.QName;

/** SAML related constants. */
public final class SAMLConstants {

    /** SAML Metadata namespace URI. */
    public static final String MD_NS = "urn:oasis:names:tc:SAML:2.0:metadata";
    
    /** Default SAML Metadata namespace prefix. */
    public static final String MD_PREFIX = "md:";

    /** EntitiesDescriptor element name. */
    public static final QName ENTITIES_DESCRIPTOR_NAME = new QName(MD_NS, "EntitiesDescriptor", MD_PREFIX);
    
    /** EntityDescriptor element name. */
    public static final QName ENTITY_DESCRIPTOR_NAME = new QName(MD_NS, "EntityDescriptor", MD_PREFIX);
    
    /** validUntil attribute name. */
    public static final QName VALID_UNTIL_ATTIB_NAME = new QName("validUntil");
    
    /** cacheDuration attribute name. */
    public static final QName CACHE_DURATION_ATTRIB_NAME = new QName("cacheDuration");

    /** Constructor. */
    private SAMLConstants() {
    }
}