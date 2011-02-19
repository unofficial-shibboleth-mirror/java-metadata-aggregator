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

package net.shibboleth.metadata.dom.saml;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.util.Assert;
import org.opensaml.util.xml.AttributeSupport;
import org.opensaml.util.xml.ElementSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Helper class for dealing with SAML metadata. */
@ThreadSafe
public final class MetadataHelper {

    /** SAML Metadata namespace URI. */
    public static final String MD_NS = "urn:oasis:names:tc:SAML:2.0:metadata";

    /** Default SAML Metadata namespace prefix. */
    public static final String MD_PREFIX = "md";

    /** EntitiesDescriptor element name. */
    public static final QName ENTITIES_DESCRIPTOR_NAME = new QName(MD_NS, "EntitiesDescriptor", MD_PREFIX);

    /** EntityDescriptor element name. */
    public static final QName ENTITY_DESCRIPTOR_NAME = new QName(MD_NS, "EntityDescriptor", MD_PREFIX);

    /** validUntil attribute name. */
    public static final QName VALID_UNTIL_ATTIB_NAME = new QName("validUntil");

    /** cacheDuration attribute name. */
    public static final QName CACHE_DURATION_ATTRIB_NAME = new QName("cacheDuration");

    /** Factory used to create XML data types. */
    private static DatatypeFactory xmlDatatypeFactory;

    /** Constructor. */
    private MetadataHelper() {

    }

    /**
     * Builds a SAML EntitiesDescriptor element from a collection of EntitiesDescriptor or EntityDescriptor elements.
     * 
     * @param metadataCollection collection containing the EntitiesDescriptor or EntityDescriptor elements, other
     *            elements will be ignored
     * 
     * @return the constructed EntitiesDescriptor
     */
    public static Element buildEntitiesDescriptor(final MetadataCollection<DomMetadata> metadataCollection) {
        final Document owningDocument = metadataCollection.iterator().next().getMetadata().getOwnerDocument();

        final Element entitiesDescriptor = ElementSupport.constructElement(owningDocument, ENTITIES_DESCRIPTOR_NAME);
        ElementSupport.setDocumentElement(owningDocument, entitiesDescriptor);

        Element descriptor;
        for (DomMetadata metadata : metadataCollection) {
            descriptor = metadata.getMetadata();
            if (isEntitiesDescriptor(descriptor) || isEntityDescriptor(descriptor)) {
                ElementSupport.appendChildElement(entitiesDescriptor, descriptor);
            }
        }

        return entitiesDescriptor;
    }

    /**
     * Adds a validUntil attribute to the given element. The validUntil value is determined as the time now plus the
     * given lifetime.
     * 
     * @param metadata element to which the attribute will be added
     * @param duration lifetime of the element in milliseconds
     */
    public static void addValidUntil(final Element metadata, final long duration) {
        Assert.isGreaterThan(0, duration, "Validity duration must be 1 or greater.");
        final DateTime now = new DateTime(ISOChronology.getInstanceUTC()).plus(duration);
        final XMLGregorianCalendar validUntil = xmlDatatypeFactory.newXMLGregorianCalendar(now.toGregorianCalendar());
        AttributeSupport.appendAttribute(metadata, VALID_UNTIL_ATTIB_NAME, validUntil.toString());
    }

    /**
     * Adds a cacheDuration attribute to the given element.
     * 
     * @param metadata element to which the attribute will be added
     * @param duration cache duration of the element in milliseconds
     */
    public static void addCacheDuration(final Element metadata, final long duration) {
        Assert.isGreaterThan(0, duration, "Cache duration must be 1 or greater.");
        final Duration xmlDuration = xmlDatatypeFactory.newDuration(duration);
        AttributeSupport.appendAttribute(metadata, CACHE_DURATION_ATTRIB_NAME, xmlDuration.toString());
    }

    /**
     * Checks if the given element is an EntitiesDescriptor.
     * 
     * @param e element to check
     * @return true if the element is an EntitiesDescriptor, false otherwise
     */
    public static boolean isEntitiesDescriptor(final Element e) {
        return ElementSupport.isElementNamed(e, ENTITIES_DESCRIPTOR_NAME);
    }

    /**
     * Checks if the given element is an EntityDescriptor.
     * 
     * @param e element to check
     * @return true if the element is an EntityDescriptor, false otherwise
     */
    public static boolean isEntityDescriptor(final Element e) {
        return ElementSupport.isElementNamed(e, ENTITY_DESCRIPTOR_NAME);
    }

    static {
        try {
            xmlDatatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("JVM does not support creation of javax.xml.datatype.DatatypeFactory");
        }
    }
}