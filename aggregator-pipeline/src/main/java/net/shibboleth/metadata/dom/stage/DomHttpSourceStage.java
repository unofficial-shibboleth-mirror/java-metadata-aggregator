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

package net.shibboleth.metadata.dom.stage;

import java.io.InputStream;
import java.util.Collection;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.StageProcessingException;

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
 * A pipeline stage which reads an XML document from an HTTP source, parses the document, and places the resultant
 * document (root) element in to the provided metadata collection.
 */
@ThreadSafe
public class DomHttpSourceStage extends BaseStage<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DomHttpSourceStage.class);

    /** Resource used to fetch remote metadata. */
    private HttpResource metadataResource;

    /** Pool of parsers used to parse incoming metadata. */
    private ParserPool parserPool;

    /**
     * Whether an error parsing one source file causes this entire {@link net.shibboleth.metadata.pipeline.Stage} to
     * fail, or just excludes the material from the offending source file. Default value: {@value}
     */
    private boolean errorCausesSourceFailure = true;
    
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
    
    /**
     * Gets whether an error reading and parsing the XML file causes this stage to fail.
     * 
     * @return whether an error reading and parsing the XML file causes this stage to fail
     */
    public boolean getErrorCausesSourceFailure() {
        return errorCausesSourceFailure;
    }

    /**
     * Sets whether an error reading and parsing the XML file causes this stage to fail.
     * 
     * @param causesFailure whether an error reading and parsing the XML file causes this stage to fail
     */
    public synchronized void setErrorCausesSourceFailure(final boolean causesFailure) {
        if (isInitialized()) {
            return;
        }
        errorCausesSourceFailure = causesFailure;
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<DomMetadata> metadataCollection) throws StageProcessingException {
        InputStream ins = null;

        try {
            log.debug("Attempting to fetch metadata document from '{}'", metadataResource.getLocation());

            ins = metadataResource.getInputStream();
            if (ins != null) {
                log.debug("New metadata available from '{}', processing it", metadataResource.getLocation());
                buildMetadataCollectionFromNewData(metadataCollection, ins);
            } else {
                log.debug("Metadata from '{}' unchanged since last request, using cached copy",
                        metadataResource.getLocation());
                buildMetadataCollectionFromCache(metadataCollection);
            }
        } catch (ResourceException e) {
            if(errorCausesSourceFailure){
                throw new StageProcessingException("Error retrieving metadata from " + metadataResource.getLocation(), e);
            }else{
                log.warn("stage {}: unable to read in XML file");
                log.debug("stage {}: HTTP resource exception", getId(), e);
            }
        } finally {
            CloseableSupport.closeQuietly(ins);
        }
    }

    /**
     * Builds a metadata collection from new matadata. Also caches metadata in its parsed form for later use.
     * 
     * @param metadataCollection metadata collection to which the read in and parsed document element is added
     * @param data XML input file
     * 
     * @throws StageProcessingException thrown if there is a problem reading and parsing the response
     */
    protected void buildMetadataCollectionFromNewData(Collection<DomMetadata> metadataCollection, final InputStream data)
            throws StageProcessingException {
        try {
            log.debug("Parsing metadata retrieved from '{}'", metadataResource.getLocation());
            cachedMetadata = parserPool.parse(data);
            buildMetadataCollectionFromCache(metadataCollection);
        } catch (XMLParserException e) {
            if(errorCausesSourceFailure){
                throw new StageProcessingException("Unable to parse returned metadata", e);
            }else{
                log.warn("stage {}: unable to parse XML document", getId());
                log.debug("stage {}: parsing exception", getId(), e);
            }
        }
    }

    /**
     * Builds the metadata collection from the cached metadata.
     * 
     * @param metadataCollection metadata collection to which the cached document element is added
     */
    protected void buildMetadataCollectionFromCache(Collection<DomMetadata> metadataCollection) {
        final Element clonedMetadata = (Element) cachedMetadata.getDocumentElement().cloneNode(true);
        metadataCollection.add(new DomMetadata(clonedMetadata));
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