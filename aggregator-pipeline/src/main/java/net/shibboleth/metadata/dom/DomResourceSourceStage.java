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

import java.io.InputStream;
import java.util.Collection;

import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.resource.Resource;
import net.shibboleth.utilities.java.support.resource.ResourceException;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;

/**
 * A pipeline stage which reads an XML document from an {@link Resource}, parses the document, and places the resultant
 * document (root) element in to the provided {@link DomElementItem} collection.
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>parserPool</code></li>
 * <li><code>domResource</code></li>
 * </ul>
 */
@ThreadSafe
public class DomResourceSourceStage extends BaseStage<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DomResourceSourceStage.class);

    /** Resource used to fetch remote XML document. */
    private Resource domResource;

    /** Pool of parsers used to parse incoming DOM. */
    private ParserPool parserPool;

    /**
     * Whether an error parsing one source file causes this entire {@link net.shibboleth.metadata.pipeline.Stage} to
     * fail, or just excludes the material from the offending source file. Default value: <code>true</code>
     */
    private boolean errorCausesSourceFailure = true;

    /**
     * Gets the resource from which the XML Document will be fetched.
     * 
     * @return resource from which the XML document will be fetched
     */
    public Resource getDomResource() {
        return domResource;
    }

    /**
     * Sets the resource from which the XML document will be fetched.
     * 
     * @param resource resource from which the XML document will be fetched
     */
    public synchronized void setDomResource(final Resource resource) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
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
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        
        errorCausesSourceFailure = causesFailure;
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<DomElementItem> itemCollection) throws StageProcessingException {
        InputStream ins = null;

        try {
            log.debug("Attempting to fetch XML document from '{}'", domResource.getLocation());

            ins = domResource.getInputStream();
            if (ins == null) {
                log.debug("Resource at location '{}' did not produce any data to parse, nothing left to do",
                        domResource.getLocation());
            } else {
                log.debug("DOM Element from '{}' unchanged since last request, using cached copy",
                        domResource.getLocation());
                populateItemCollection(itemCollection, ins);
            }
        } catch (ResourceException e) {
            if (errorCausesSourceFailure) {
                throw new StageProcessingException("Error retrieving XML document from " + domResource.getLocation(), e);
            } else {
                log.warn("stage {}: unable to read in XML file");
                log.debug("stage {}: HTTP resource exception", getId(), e);
            }
        } finally {
            Closeables.closeQuietly(ins);
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
    protected void populateItemCollection(Collection<DomElementItem> itemCollection, final InputStream data)
            throws StageProcessingException {
        try {
            log.debug("Parsing XML document retrieved from '{}'", domResource.getLocation());
            itemCollection.add(new DomElementItem(parserPool.parse(data)));
        } catch (XMLParserException e) {
            if (errorCausesSourceFailure) {
                throw new StageProcessingException("Unable to parse returned XML document", e);
            } else {
                log.warn("stage {}: unable to parse XML document", getId());
                log.debug("stage {}: parsing exception", getId(), e);
            }
        }
    }

    /** {@inheritDoc} */
    protected void doDestroy() {
        domResource.destroy();
        domResource = null;
        parserPool = null;
        
        super.doDestroy();
    }
    
    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (parserPool == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", ParserPool may not be null");
        }

        if (domResource == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", either a DomResource must be specified");
        }
        
        if(!domResource.isInitialized()){
            domResource.initialize();
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