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

package net.shibboleth.metadata.dom.source;

import java.io.InputStream;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.SimpleMetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.AbstractComponent;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.Source;
import net.shibboleth.metadata.pipeline.SourceProcessingException;

import org.opensaml.util.CloseableSupport;
import org.opensaml.util.http.HttpResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.util.xml.ParserPool;
import org.opensaml.util.xml.XMLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A pipeline source which reads an XML document from an HTTP source, parses the document, and returns the resultant
 * document (root) element as the metadata within the returned collection.
 */
@ThreadSafe
public class DomHttpSource extends AbstractComponent implements Source<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DomHttpSource.class);

    /** Resource used to fetch remote metadata. */
    private HttpResource metadataResource;

    /** Pool of parsers used to parse incoming metadata. */
    private ParserPool parserPool;

    /** Cached metadata. */
    private Document cachedMetadata;

    /**
     * Gets the resource from which metadata will be fetched.
     * 
     * @return resource from which metadata will be fetched
     */
    public HttpResource getMetadataResource() {
        return metadataResource;
    }

    /**
     * Sets the resource from which metadata will be fetched.
     * 
     * @param resource resource from which metadata will be fetched
     */
    public synchronized void setMetadataResource(final HttpResource resource) {
        if (isInitialized()) {
            return;
        }
        metadataResource = resource;
    }

    /**
     * Gets the pool of DOM parsers used to parse the XML file in to a DOM.
     * 
     * @return pool of DOM parsers used to parse the XML file in to a DOM
     */
    public ParserPool getParserPool() {
        return parserPool;
    }

    /**
     * Sets the pool of DOM parsers used to parse the XML file in to a DOM.
     * 
     * @param pool pool of DOM parsers used to parse the XML file in to a DOM
     */
    public synchronized void setParserPool(final ParserPool pool) {
        if (isInitialized()) {
            return;
        }
        parserPool = pool;
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute() throws SourceProcessingException {
        InputStream ins = null;
        
        try {
            log.debug("Attempting to fetch metadata document from '{}'", metadataResource.getLocation());

            ins = metadataResource.getInputStream();
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
        }finally{
            CloseableSupport.closeQuietly(ins);
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
    protected MetadataCollection<DomMetadata> buildMetadataCollectionFromNewData(final InputStream data)
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
        final Element clonedMetadata = (Element) cachedMetadata.getDocumentElement().cloneNode(true);

        final SimpleMetadataCollection<DomMetadata> mdc = new SimpleMetadataCollection<DomMetadata>();
        mdc.add(new DomMetadata(clonedMetadata));

        return mdc;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        if (parserPool == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", ParserPool may not be null");
        }

        if (metadataResource == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", MetadataResource may not be null");
        }

        try {
            if (!metadataResource.exists()) {
                throw new ComponentInitializationException("Unable to initialize " + getId() + ", metadata resource "
                        + metadataResource.getLocation() + " does not exist");
            }
        } catch (ResourceException e) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", error reading metadata resource " + metadataResource.getLocation() + " information", e);
        }
    }
}