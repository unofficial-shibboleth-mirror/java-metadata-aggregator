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

/** SAML related constants. */
public final class SAMLConstants {

    /** SAML Metadata Namespace URI. */
    public static final String MD_NS = "urn:oasis:names:tc:SAML:2.0:metadata";

    /** EntitiesDescriptor element local (tag) name. */
    public static final String ENTITIES_DESCRIPTOR_LOCAL_NAME = "EntitiesDescriptor";

    /** EntityDescriptor element local (tag) name. */
    public static final String ENTITY_DESCRIPTOR_LOCAL_NAME = "EntityDescriptor";
    
    /** validUntil attribute local name. */
    public static final String VALID_UNTIL_ATTIB_LOCAL_NAME = "validUntil";
    
    /** cacheDuration attribute local name. */
    public static final String CACHE_DURATION_ATTRIB_LOCAL_NAME = "cacheDuration";

    /** Constructor. */
    private SAMLConstants() {
    }
}