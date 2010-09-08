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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.util.Assert;
import org.opensaml.util.xml.Attributes;
import org.opensaml.util.xml.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadata;

/** Helper class for dealing with SAML metadata. */
public class MetadataHelper {

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

    /**
     * Builds a SAML EntitiesDescriptor element from a collection of EntitiesDescriptor or EntityDescriptor elements.
     * 
     * @param metadataCollection collection containing the EntitiesDescriptor or EntityDescriptor elements, other
     *            elements will be ignored
     * 
     * @return the constructed EntitiesDescriptor
     */
    public static Element buildEntitiesDescriptor(MetadataCollection<DomMetadata> metadataCollection) {

        Document owningDocument = metadataCollection.iterator().next().getMetadata().getOwnerDocument();

        Element entitiesDescriptor = Elements.constructElement(owningDocument, ENTITIES_DESCRIPTOR_NAME);
        Elements.setDocumentElement(owningDocument, entitiesDescriptor);

        Element descriptor;
        for (DomMetadata metadata : metadataCollection) {
            descriptor = metadata.getMetadata();
            if (isEntitiesDescriptor(descriptor) || isEntityDescriptor(descriptor)) {
                Elements.appendChildElement(entitiesDescriptor, descriptor);
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
    public static void addValidUntil(Element metadata, long duration) {
        Assert.isGreaterThan(0, duration, "Validity duration must be 1 or greater.");
        DateTime now = new DateTime(ISOChronology.getInstanceUTC()).plus(duration);
        XMLGregorianCalendar validUntil = xmlDatatypeFactory.newXMLGregorianCalendar(now.toGregorianCalendar());
        Attributes.appendAttribute(metadata, VALID_UNTIL_ATTIB_NAME, validUntil.toString());
    }

    /**
     * Adds a cacheDuration attribute to the given element.
     * 
     * @param metadata element to which the attribute will be added
     * @param duration cache duration of the element in milliseconds
     */
    public static void addCacheDuration(Element metadata, long duration) {
        Assert.isGreaterThan(0, duration, "Cache duration must be 1 or greater.");
        Duration xmlDuration = xmlDatatypeFactory.newDuration(duration);
        Attributes.appendAttribute(metadata, CACHE_DURATION_ATTRIB_NAME, xmlDuration.toString());
    }

    /**
     * Checks if the given element is an EntitiesDescriptor.
     * 
     * @param e element to check
     * @return true if the element is an EntitiesDescriptor, false otherwise
     */
    public static boolean isEntitiesDescriptor(Element e) {
        return Elements.isElementNamed(e, ENTITIES_DESCRIPTOR_NAME);
    }

    /**
     * Checks if the given element is an EntityDescriptor.
     * 
     * @param e element to check
     * @return true if the element is an EntityDescriptor, false otherwise
     */
    public static boolean isEntityDescriptor(Element e) {
        return Elements.isElementNamed(e, ENTITY_DESCRIPTOR_NAME);
    }

    static {
        try {
            xmlDatatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            // nothing to do, this is required to be supported by JAXP 1.3
        }
    }
}