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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemCollectionSerializer;
import net.shibboleth.metadata.dom.saml.mdattr.MDAttrSupport;
import net.shibboleth.metadata.dom.saml.mdui.MDUISupport;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.component.AbstractInitializableComponent;
import net.shibboleth.shared.xml.ElementSupport;

/**
 * A collection serializer that generates an equivalent of the Shibboleth SP's discovery
 * feed output from a collection of entity descriptors.
 *
 * @since 0.10.0
 */
public class DiscoFeedCollectionSerializer extends AbstractInitializableComponent
    implements ItemCollectionSerializer<Element> {

    /** Whether to pretty-print the resulting JSON. Default: <code>false</code> */
    @GuardedBy("this") private boolean prettyPrinting;

    /**
     * Whether to include legacy display names if none are found in the
     * <code>mdui:UIInfo</code> element.
     * 
     * Default: <code>false</code>
     */
    @GuardedBy("this") private boolean includingLegacyDisplayNames;
    
    /** Whether to include entity attributes. Default: <code>false</code> */
    @GuardedBy("this") private boolean includingEntityAttributes;

    /**
     * Returns whether output is being pretty-printed.
     * 
     * @return whether output is being pretty-printed
     */
    public final synchronized boolean isPrettyPrinting() {
        return prettyPrinting;
    }

    /**
     * Sets whether to pretty-print the output.
     * 
     * @param pretty whether to pretty-print the output
     */
    public synchronized void setPrettyPrinting(final boolean pretty) {
        prettyPrinting = pretty;
    }

    /**
     * Returns whether output includes legacy display names.
     * 
     * @return whether output includes legacy display names
     */
    public final synchronized boolean isIncludingLegacyDisplayNames() {
        return includingLegacyDisplayNames;
    }

    /**
     * Sets whether to include legacy display names.
     * 
     * @param includeLegacyDisplayNames whether to include legacy display names
     */
    public synchronized void setIncludingLegacyDisplayNames(final boolean includeLegacyDisplayNames) {
        includingLegacyDisplayNames = includeLegacyDisplayNames;
    }

    /**
     * Returns whether output includes entity attributes.
     * 
     * @return whether output includes entity attributes
     */
    public final synchronized boolean isIncludingEntityAttributes() {
        return includingEntityAttributes;
    }

    /**
     * Sets whether to include entity attributes.
     * 
     * @param includeEntityAttributes whether to include entity attributes.
     */
    public synchronized void setIncludingEntityAttributes(final boolean includeEntityAttributes) {
        includingEntityAttributes = includeEntityAttributes;
    }

    /**
     * Find the first <code>mdui:UIInfo</code> {@link Element} across a list of
     * <code>md:IDPSSODescriptor</code>s.
     *
     * @param idpDescriptors list of <code>md:IDPSSODescriptor</code>s
     *
     * @return the first <code>mdui:UIInfo</code> element, or <code>null</code> if none are found
     */
    @Nullable
    private Element findFirstUIInfo(@Nonnull @NonnullElements final List<Element> idpDescriptors) {
        for (final var idpDescriptor : idpDescriptors) {
            final var uiInfo = SAMLMetadataSupport.getDescriptorExtension(idpDescriptor, MDUISupport.UIINFO_NAME);
            if (uiInfo != null) {
                return uiInfo;
            }
        }
        return null;
    }

    /**
     * Write a list of element values and language tags to the output
     * as a JSON array under a given key.
     * 
     * @param gen the {@link JsonGenerator} to write the output to
     * @param elements list of {@link Element}s to write out
     * @param key key for the JSON array within the containing object
     */
    private void writeValueLangList(@Nonnull final JsonGenerator gen,
            @Nonnull @NonnullElements final List<Element> elements,
            @Nonnull final String key) {
        gen.writeStartArray(key);
        for (final var element : elements) {
            gen.writeStartObject();
                gen.write("value", element.getTextContent());
                gen.write("lang", element.getAttribute("xml:lang"));
            gen.writeEnd();
        }
        gen.writeEnd();
    }

    /**
     * Write a value/language list for elements within a <code>mdui:UIInfo</code> container.
     * 
     * @param gen the {@link JsonGenerator} to write the output to
     * @param uiInfo the <code>mdui:UIInfo</code> element
     * @param elementName name of the elements to collect from
     * @param key key for the JSON array within the containing object
     */
    private void writeValueLangList(@Nonnull final JsonGenerator gen,
            @Nonnull final Element uiInfo,
            @Nonnull final QName elementName,
            @Nonnull final String key) {
        final var elements = ElementSupport.getChildElements(uiInfo, elementName);
        if (!elements.isEmpty()) {
            writeValueLangList(gen, elements, key);
        }        
    }
    
    /**
     * Write out an entity's entity attributes.
     *
     * @param gen the {@link JsonGenerator} to write the output to
     * @param entity the <code>md:EntityDescriptor</code> element
     */
    private void writeEntityAttributes(@Nonnull final JsonGenerator gen, @Nonnull final Element entity) {
        final var ext = SAMLMetadataSupport.getDescriptorExtension(entity, MDAttrSupport.ENTITY_ATTRIBUTES_NAME);
        if (ext != null) {
            final var attributes = ElementSupport.getChildElements(ext, SAMLSupport.ATTRIBUTE_NAME);
            if (!attributes.isEmpty()) {
                gen.writeStartArray("EntityAttributes");
                    for (final var attribute : attributes) {
                        final var values = ElementSupport.getChildElements(attribute, SAMLSupport.ATTRIBUTE_VALUE_NAME);
                        if (!values.isEmpty()) {
                            gen.writeStartObject();
                                gen.write("name", attribute.getAttribute("Name"));
                                gen.writeStartArray("values");
                                    for (final var value : values) {
                                        gen.write(value.getTextContent());
                                    }
                                gen.writeEnd();
                            gen.writeEnd();
                        }
                    }
                gen.writeEnd();
            }
        }
    }

    /**
     * Write the entity's logos to the output.
     *
     * @param gen the {@link JsonGenerator} to write the output to
     * @param uiInfo the <code>mdui:UIInfo</code> element
     */
    private void writeLogos(@Nonnull final JsonGenerator gen, @Nonnull final Element uiInfo) {
        final var logos = ElementSupport.getChildElements(uiInfo, MDUISupport.LOGO_NAME);
        if (!logos.isEmpty()) {
            gen.writeStartArray("Logos");
                for (final var logo: logos) {
                    gen.writeStartObject();
                        gen.write("value", logo.getTextContent());
                        gen.write("height", logo.getAttribute("height"));
                        gen.write("width", logo.getAttribute("width"));
                        // xml:lang is optional on mdui:Logo elements
                        final var lang = logo.getAttributeNode("xml:lang");
                        if (lang != null) {
                            gen.write("lang", lang.getTextContent());
                        }
                    gen.writeEnd();
                }
            gen.writeEnd();
        }
    }

    /**
     * Write the list of display names to the output.
     *
     * If we have a <code>mdui:UIInfo</code>, we may be able to find some display names
     * there. If we can find no <code>mdui:DisplayName</code> elements, we instead
     * find legacy display names in the <code>md:Organization</code> element.
     *
     * @param gen the {@link JsonGenerator} to write the output to
     * @param entity <code>md:EntityDescriptor</code> element
     * @param uiInfo <code>mdui:UIInfo</code> if available, or <code>null</code>
     */
    private void writeDisplayNames(@Nonnull final JsonGenerator gen, @Nonnull final Element entity,
            @Nullable final Element uiInfo) {
        // Attempt to find display names in the mdui:UIInfo element
        if (uiInfo != null) {
            final var displayNames = ElementSupport.getChildElements(uiInfo, MDUISupport.DISPLAYNAME_NAME);
            if (!displayNames.isEmpty()) {
                writeValueLangList(gen, displayNames, "DisplayNames");
                // We have found our display names
                return;
            }
        }
        
        // Attempt to find display names elsewhere
        if (isIncludingLegacyDisplayNames()) {
            final var org = ElementSupport.getFirstChildElement(entity, SAMLMetadataSupport.ORGANIZATION_NAME);
            if (org != null) {
                final var displayNames =
                        ElementSupport.getChildElements(org, SAMLMetadataSupport.ORGANIZATIONDISPLAYNAME_NAME);
                if (!displayNames.isEmpty()) {
                    writeValueLangList(gen, displayNames, "DisplayNames");
                }
            }
        }
    }

    @Override
    public void serializeCollection(@Nonnull @NonnullElements final Collection<Item<Element>> items,
            @Nonnull final OutputStream output) throws IOException {
        checkComponentActive();
        final Map<String, String> generatorConfig = new HashMap<>();
        if (isPrettyPrinting()) {
            generatorConfig.put(JsonGenerator.PRETTY_PRINTING, "true");
        }
        final var factory = Json.createGeneratorFactory(generatorConfig);
        final JsonGenerator gen = factory.createGenerator(output);
        gen.writeStartArray();
            for (final Item<Element> item : items) {
                final Element entity = item.unwrap();
                if (SAMLMetadataSupport.isEntityDescriptor(entity)) {
                    final var idpDescriptors =
                            ElementSupport.getChildElements(entity, SAMLMetadataSupport.IDP_SSO_DESCRIPTOR_NAME);
                    if (!idpDescriptors.isEmpty()) {
                        gen.writeStartObject();
                            gen.write("entityID", entity.getAttributeNS(null, "entityID"));
                            final var uiInfo = findFirstUIInfo(idpDescriptors);
                            writeDisplayNames(gen, entity, uiInfo);
                            if (uiInfo != null) {
                                writeValueLangList(gen, uiInfo, MDUISupport.DESCRIPTION_NAME, "Descriptions");
                                writeValueLangList(gen, uiInfo, MDUISupport.KEYWORDS_NAME, "Keywords");
                                writeValueLangList(gen, uiInfo, MDUISupport.INFORMATIONURL_NAME, "InformationURLs");
                                writeValueLangList(gen, uiInfo,
                                        MDUISupport.PRIVACYSTATEMENTURL_NAME, "PrivacyStatementURLs");
                                writeLogos(gen, uiInfo);
                            }
                            if (isIncludingEntityAttributes()) {
                                writeEntityAttributes(gen, entity);
                            }
                        gen.writeEnd();
                    }
                }
            }
        gen.writeEnd();
        gen.close();
    }

}
