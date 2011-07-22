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

package net.shibboleth.metadata.dom;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.opensaml.util.CloseableSupport;
import org.opensaml.util.StringSupport;
import org.opensaml.util.net.HttpClientBuilder;
import org.opensaml.util.net.HttpResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.util.xml.ParserPool;
import org.opensaml.util.xml.XMLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import edu.vt.middleware.crypt.digest.MD5;
import edu.vt.middleware.crypt.util.HexConverter;

/**
 * A pipeline stage which reads an XML document from an HTTP source, parses the document, and places the resultant
 * document (root) element in to the provided {@link DomElementItem} collection.
 * 
 * To initialize this stage either you must provide a {@link #parserPool} and either a {@link #domResource} or
 * {@link #sourceUrl}. If both a {@link #domResource} and {@link #sourceUrl} are specified then {@link #domResource} is
 * used.
 */
@ThreadSafe
public class DomHttpSourceStage extends BaseStage<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DomHttpSourceStage.class);

    /** URL from which the XML document will be fetched. */
    private String sourceUrl;

    /** Resource used to fetch remote XML document. */
    private HttpResource domResource;

    /** Pool of parsers used to parse incoming DOM. */
    private ParserPool parserPool;

    /**
     * Whether an error parsing one source file causes this entire {@link net.shibboleth.metadata.pipeline.Stage} to
     * fail, or just excludes the material from the offending source file. Default value: {@value}
     */
    private boolean errorCausesSourceFailure = true;

    /** Cached DOM. */
    private Document cachedDom;

    /**
     * Gets the URL from which the XML document will be fetched.
     * 
     * @return URL from which the XML document will be fetched
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Sets the URL from which the XML document will be fetched.
     * 
     * @param url URL from which the XML document will be fetched
     */
    public synchronized void setSourceUrl(String url) {
        if (isInitialized()) {
            return;
        }
        sourceUrl = StringSupport.trimOrNull(url);
    }

    /**
     * Gets the resource from which the XML Document will be fetched.
     * 
     * @return resource from which the XML document will be fetched
     */
    public HttpResource getDomResource() {
        return domResource;
    }

    /**
     * Sets the resource from which the XML document will be fetched.
     * 
     * @param resource resource from which the XML document will be fetched
     */
    public synchronized void setDomResource(final HttpResource resource) {
        if (isInitialized()) {
            return;
        }
        domResource = resource;
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
    protected void doExecute(Collection<DomElementItem> itemCollection) throws StageProcessingException {
        InputStream ins = null;

        try {
            log.debug("Attempting to fetch XML document from '{}'", domResource.getLocation());

            ins = domResource.getInputStream();
            if (ins != null) {
                log.debug("New DOM Element available from '{}', processing it", domResource.getLocation());
                buildItemCollectionFromNewData(itemCollection, ins);
            } else {
                log.debug("DOM Element from '{}' unchanged since last request, using cached copy",
                        domResource.getLocation());
                buildItemCollectionFromCache(itemCollection);
            }
        } catch (ResourceException e) {
            if (errorCausesSourceFailure) {
                throw new StageProcessingException("Error retrieving XML document from " + domResource.getLocation(), e);
            } else {
                log.warn("stage {}: unable to read in XML file");
                log.debug("stage {}: HTTP resource exception", getId(), e);
            }
        } finally {
            CloseableSupport.closeQuietly(ins);
        }
    }

    /**
     * Builds an {@link DomElementItem} collection from a new XML document. Also caches DOM Element in its parsed form
     * for later use.
     * 
     * @param itemCollection collection to which the read in and parsed document element is added
     * @param data XML input file
     * 
     * @throws StageProcessingException thrown if there is a problem reading and parsing the response
     */
    protected void buildItemCollectionFromNewData(Collection<DomElementItem> itemCollection, final InputStream data)
            throws StageProcessingException {
        try {
            log.debug("Parsing XML document retrieved from '{}'", domResource.getLocation());
            cachedDom = parserPool.parse(data);
            buildItemCollectionFromCache(itemCollection);
        } catch (XMLParserException e) {
            if (errorCausesSourceFailure) {
                throw new StageProcessingException("Unable to parse returned XML document", e);
            } else {
                log.warn("stage {}: unable to parse XML document", getId());
                log.debug("stage {}: parsing exception", getId(), e);
            }
        }
    }

    /**
     * Builds the {@link DomElementItem} collection from the cached DOM Element.
     * 
     * @param itemCollection collection to which the cached document element is added
     */
    protected void buildItemCollectionFromCache(Collection<DomElementItem> itemCollection) {
        final Document clonedDocument = (Document) cachedDom.cloneNode(true);
        itemCollection.add(new DomElementItem(clonedDocument));
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        
        if (parserPool == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", ParserPool may not be null");
        }

        if (sourceUrl == null && domResource == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", either a SourceUrl or DomResource must be specified");
        }

        if (domResource == null) {
            HttpClientBuilder clientBuilder = new HttpClientBuilder();
            clientBuilder.setConnectionDisregardSslCertificate(true);
            clientBuilder.setConnectionPooling(true);
            clientBuilder.setConnectionTimeout(60000);
            clientBuilder.setHttpFollowRedirects(true);
            clientBuilder.setSocketTimeout(60000);

            String urlHash = new MD5().digest(sourceUrl.getBytes(), new HexConverter());
            String backupFilePath = System.getProperty("java.io.tmpdir") + File.pathSeparator + urlHash;
            domResource = new HttpResource(sourceUrl, clientBuilder.buildClient(), backupFilePath, false);
        } else {
            sourceUrl = domResource.getLocation();
        }

        try {
            if (!domResource.exists()) {
                throw new ComponentInitializationException("Unable to initialize " + getId() + ", DOM resource "
                        + domResource.getLocation() + " does not exist");
            }
        } catch (ResourceException e) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", error reading DOM resource " + domResource.getLocation() + " information", e);
        }
    }
}