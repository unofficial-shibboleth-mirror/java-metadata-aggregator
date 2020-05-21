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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

/**
 * A pipeline stage which reads an XML document from an {@link Resource}, parses the document, and places the resultant
 * document (root) element in to the provided {@link DOMElementItem} collection.
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>parserPool</code></li>
 * <li><code>domResource</code></li>
 * </ul>
 */
@ThreadSafe
public class DOMResourceSourceStage extends AbstractStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DOMResourceSourceStage.class);

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
    @Nullable public Resource getDOMResource() {
        return domResource;
    }

    /**
     * Sets the resource from which the XML document will be fetched.
     * 
     * @param resource resource from which the XML document will be fetched
     */
    public synchronized void setDOMResource(@Nonnull final Resource resource) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        domResource = Constraint.isNotNull(resource, "DOM resource can not be null");
    }

    /**
     * Gets the pool of DOM parsers used to parse the XML file in to a DOM.
     * 
     * @return pool of DOM parsers used to parse the XML file in to a DOM
     */
    @Nullable public ParserPool getParserPool() {
        return parserPool;
    }

    /**
     * Sets the pool of DOM parsers used to parse the XML file in to a DOM.
     * 
     * @param pool pool of DOM parsers used to parse the XML file in to a DOM
     */
    public synchronized void setParserPool(@Nonnull final ParserPool pool) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        parserPool = Constraint.isNotNull(pool, "Parser pool can not be null");
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
    @Override protected void doExecute(@Nonnull @NonnullElements final Collection<Item<Element>> itemCollection)
            throws StageProcessingException {

        log.debug("Attempting to fetch XML document from '{}'", domResource.getDescription());

        try (InputStream ins = domResource.getInputStream()) {
            populateItemCollection(itemCollection, ins);
        } catch (final IOException e) {
            if (errorCausesSourceFailure) {
                throw new StageProcessingException("Error retrieving XML document from " +
                        domResource.getDescription(), e);
            } else {
                log.warn("stage {}: unable to read in XML file", getId());
                log.debug("stage {}: HTTP resource exception", getId(), e);
            }
        }
    }

    /**
     * Builds an {@link DOMElementItem} collection from a new XML document. Also caches DOM Element in its parsed form
     * for later use.
     * 
     * @param itemCollection collection to which the read in and parsed document element is added
     * @param data XML input file
     * 
     * @throws StageProcessingException thrown if there is a problem reading and parsing the response
     */
    protected void populateItemCollection(@Nonnull @NonnullElements final Collection<Item<Element>> itemCollection,
            final InputStream data) throws StageProcessingException {
        try {
            log.debug("Parsing XML document retrieved from '{}'", domResource.getDescription());
            itemCollection.add(new DOMElementItem(parserPool.parse(data)));
        } catch (final XMLParserException e) {
            if (errorCausesSourceFailure) {
                throw new StageProcessingException(getId() + " unable to parse returned XML document " +
                        domResource.getDescription(), e);
            } else {
                log.warn("stage {}: unable to parse XML document", getId());
                log.debug("stage {}: parsing exception", getId(), e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        domResource = null;
        parserPool = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (parserPool == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", ParserPool may not be null");
        }

        if (domResource == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", either a DomResource must be specified");
        }
    }

}
