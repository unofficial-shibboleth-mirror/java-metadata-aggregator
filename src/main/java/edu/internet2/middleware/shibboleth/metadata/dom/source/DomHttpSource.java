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

package edu.internet2.middleware.shibboleth.metadata.dom.source;

import java.io.InputStream;

import org.opensaml.util.Assert;
import org.opensaml.util.http.HttpResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.util.xml.ParserPool;
import org.opensaml.util.xml.XMLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadata;
import edu.internet2.middleware.shibboleth.metadata.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.pipeline.Source;
import edu.internet2.middleware.shibboleth.metadata.pipeline.SourceProcessingException;

/**
 * A pipeline source which reads an XML document from an HTTP source, parses the document, and returns the resultant
 * document (root) element as the metadata within the returned collection.
 */
public class DomHttpSource extends AbstractComponent implements Source<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DomHttpSource.class);

    /** Resource used to fetch remote metadata. */
    private final HttpResource metadataResource;

    /** Pool of parsers used to parse incoming metadata. */
    private final ParserPool parserPool;

    /** Cached metadata. */
    private Document cachedMetadata;

    /**
     * Constructor.
     * 
     * @param sourceId unique ID of this source
     * @param resource HTTP resource from which to fetch metadata
     * @param parsers pool parsers used to parse incoming metadata
     */
    public DomHttpSource(String sourceId, HttpResource resource, ParserPool parsers) {
        super(sourceId);

        Assert.isNotNull(resource, "HTTPResource may not be null");
        metadataResource = resource;

        Assert.isNotNull(parsers, "Parser pool may not be null");
        parserPool = parsers;
    }

    /**
     * @return the cachedMetadata
     */
    public Document getCachedMetadata() {
        return cachedMetadata;
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute() throws SourceProcessingException {
        try {
            log.debug("Attempting to fetch metadata document from '{}'", metadataResource.getLocation());

            InputStream ins = metadataResource.getInputStream();
            if (ins != null) {
                log.debug("New metadata available from '{}', processing it", metadataResource.getLocation());
                return buildMetadataCollectionFromNewData(ins);
            } else {
                log.debug("Metadata from '{}' unchanged since last request, using cached copy",
                        metadataResource.getLocation());
                return buildMetadataCollectionFromCache();
            }
        } catch (ResourceException e) {
            throw new SourceProcessingException("Error retrieving metadata from " + metadataResource.getLocation(), e);
        }

    }

    /**
     * Builds a metadata collection from new matadata. Also caches metadata in its parsed form for later use.
     * 
     * @param data matadata input
     * 
     * @return the metadata collection with a single {@link DomMetadata} element containing the retrieved metadata
     * 
     * @throws SourceProcessingException thrown if there is a problem reading and parsing the response
     */
    protected MetadataCollection<DomMetadata> buildMetadataCollectionFromNewData(InputStream data)
            throws SourceProcessingException {
        try {
            log.debug("Parsing metadata retrieved from '{}'", metadataResource.getLocation());
            cachedMetadata = parserPool.parse(data);
            return buildMetadataCollectionFromCache();
        } catch (XMLParserException e) {
            throw new SourceProcessingException("Unable to parse returned metadata", e);
        }
    }

    /**
     * Builds the metadata collection from the cached metadata.
     * 
     * @return the metadata collection with a single {@link DomMetadata} element containing the retrieved metadata
     */
    protected MetadataCollection<DomMetadata> buildMetadataCollectionFromCache() {
        Element clonedMetadata = (Element) cachedMetadata.getDocumentElement().cloneNode(true);

        SimpleMetadataCollection<DomMetadata> mdc = new SimpleMetadataCollection<DomMetadata>();
        mdc.add(new DomMetadata(clonedMetadata));

        return mdc;
    }
}