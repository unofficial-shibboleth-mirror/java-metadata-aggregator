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

package net.shibboleth.metadata.dom;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.opensaml.util.StringSupport;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A pipeline stage which applies and XSLT to each element in the {@link DomElementItem} collection. */
@ThreadSafe
public abstract class AbstractXSLProcessingStage extends BaseStage<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractXSLProcessingStage.class);

    /** Resource that provides the XSL document. */
    private Resource xslResource;

    /** XSL template used to transform Elements. */
    private Templates xslTemplate;

    /** Attributes set on the {@link Transformer} used by this stage. */
    private Map<String, Object> transformAttributes = Collections.emptyMap();

    /** Features set on the {@link Transformer} used by this stage. */
    private Map<String, Boolean> transformFeatures = Collections.emptyMap();

    /**
     * Collection of named parameters to make available to the transform.
     * 
     * If not set, an empty collection.
     */
    private Map<String, Object> transformParameters = Collections.emptyMap();

    /**
     * Gets the resource that provides the XSL document.
     * 
     * @return resource that provides the XSL document
     */
    public Resource getXslResource() {
        return xslResource;
    }

    /**
     * Sets the resource that provides the XSL document.
     * 
     * @param resource resource that provides the XSL document
     */
    public synchronized void setXslResource(final Resource resource) {
        if (isInitialized()) {
            return;
        }
        xslResource = resource;
    }

    /**
     * Gets the unmodifiable collection of attributes used by the XSLT transformer.
     * 
     * @return unmodifiable collection of attributes used by the XSLT transformer, never null nor containing null keys
     */
    public Map<String, Object> getTransformAttributes() {
        return transformAttributes;
    }

    /**
     * Sets the collection of attributes used by the XSLT transformer.
     * 
     * @param attributes collection of attributes used by the XSLT transformer, may be null or contain null keys
     */
    public synchronized void setTransformAttributes(Map<String, Object> attributes) {
        if (isInitialized()) {
            return;
        }

        if (attributes == null || attributes.isEmpty()) {
            transformAttributes = Collections.emptyMap();
        }

        HashMap<String, Object> newAttributes = new HashMap<String, Object>();
        for (String attributeName : attributes.keySet()) {
            if (attributeName != null) {
                newAttributes.put(attributeName, attributes.get(attributeName));
            }
        }

        transformAttributes = Collections.unmodifiableMap(newAttributes);
    }

    /**
     * Gets the unmodifiable collection of features used by the XSLT transformer.
     * 
     * @return unmodifiable collection of features used by the XSLT transformer, never null nor containing null keys
     */
    public Map<String, Boolean> getTransformFeatures() {
        return transformFeatures;
    }

    /**
     * Sets the collection of features used by the XSLT transformer.
     * 
     * @param features collection of features used by the XSLT transformer, may be null or contain null keys
     */
    public synchronized void setTransformFeatures(Map<String, Boolean> features) {
        if (isInitialized()) {
            return;
        }

        if (features == null || features.isEmpty()) {
            transformFeatures = Collections.emptyMap();
        }

        HashMap<String, Boolean> newFeatures = new HashMap<String, Boolean>();
        for (String featuresName : features.keySet()) {
            if (featuresName != null) {
                newFeatures.put(featuresName, features.get(featuresName));
            }
        }

        transformFeatures = Collections.unmodifiableMap(newFeatures);
    }

    /**
     * Gets the unmodifiable collection of parameters used by the XSLT transformer.
     * 
     * @return parameters used by the XSLT transformer, never null nor containing null keys
     */
    public Map<String, Object> getTransformParameters() {
        return transformParameters;
    }

    /**
     * Sets the named parameters for the transform.
     * 
     * @param parameters parameters for the transform, may be null or contain null keys
     */
    public synchronized void setTransformParameters(Map<String, Object> parameters) {
        if (isInitialized()) {
            return;
        }

        if (parameters == null) {
            transformParameters = Collections.emptyMap();
            return;
        }

        HashMap<String, Object> newParams = new HashMap<String, Object>();
        for (String paramName : parameters.keySet()) {
            if (paramName != null) {
                newParams.put(paramName, parameters.get(paramName));
            }
        }

        transformParameters = Collections.unmodifiableMap(newParams);
    }

    /** {@inheritDoc} */
    protected void doExecute(final Collection<DomElementItem> itemCollection) throws StageProcessingException {

        try {
            final Transformer transformer = xslTemplate.newTransformer();
            for (Map.Entry<String, Object> entry : transformParameters.entrySet()) {
                transformer.setParameter(entry.getKey(), entry.getValue());
            }

            executeTransformer(transformer, itemCollection);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("XSL transformation engine misconfigured", e);
        }
    }

    /**
     * Executes the XSLT transform on the given collection of Items.
     * 
     * @param transformer The transform to be applied to each Item. Already has all {@link #transformParameters} set.
     * @param itemCollection the Items to which the transform should be applied
     * 
     * @throws StageProcessingException thrown if there is a problem applying the transform to Items
     * @throws TransformerConfigurationException thrown if there is a problem with the Transform itself
     */
    protected abstract void executeTransformer(Transformer transformer, Collection<DomElementItem> itemCollection)
            throws StageProcessingException, TransformerConfigurationException;

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        if (xslResource == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", XslResource must not be null");
        }

        try {
            if (!xslResource.exists()) {
                throw new ComponentInitializationException("Unable to initialize " + getId() + ", XslResource "
                        + xslResource.getLocation() + " does not exist");
            }

            final TransformerFactory tfactory = TransformerFactory.newInstance();

            for (Entry<String, Object> attribute : transformAttributes.entrySet()) {
                tfactory.setAttribute(attribute.getKey(), attribute.getValue());
            }

            for (Entry<String, Boolean> features : transformFeatures.entrySet()) {
                tfactory.setFeature(features.getKey(), features.getValue());
            }

            log.debug("{} pipeline stage compiling XSL file {}", getId(), xslResource);
            xslTemplate = tfactory.newTemplates(new StreamSource(xslResource.getInputStream()));
        } catch (TransformerConfigurationException e) {
            throw new ComponentInitializationException("XSL transformation engine misconfigured", e);
        } catch (ResourceException e) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", error reading XslResource " + xslResource.getLocation() + " information", e);
        }
    }

    /**
     * {@link Transformer} {@link ErrorListener} that sets an {@link ErrorStatus} or {@link WarningStatus} on its
     * {@link Item} depending on the {@link TransformerException} message. If the message begins with
     * {@value #ERROR_PREFIX} the remainder of the error message is used as the message for the added
     * {@link ErrorStatus}. If the message begins with {@value #WARN_PREFIX} the remainder of the error message is used
     * as the message for the added {@link WarningStatus}. If the message does not begin with either prefix the
     * exception is re-thrown to be handed by the {@link Transformer}.
     * 
     * This listener works well in conjunction with &lt;xsl:message&gt;
     */
    public class StatusInfoAppendingErrorListener implements ErrorListener {

        /** Prefix used by messages that result in an {@link ErrorStatus}. */
        public static final String ERROR_PREFIX = "[ERROR]";

        /** Prefix used by messages that result in an {@link WarningStatus}. */
        public static final String WARN_PREFIX = "[WARN]";

        /** Item to which the status info will be appended. */
        private Item<?> item;

        /**
         * Constructor.
         * 
         * @param receivingItem that Item to which the status info will be appended
         */
        public StatusInfoAppendingErrorListener(Item<?> receivingItem) {
            item = receivingItem;
        }

        /** {@inheritDoc} */
        public void error(TransformerException e) throws TransformerException {
            parseAndAppendStatusInfo(e);
        }

        /** {@inheritDoc} */
        public void fatalError(TransformerException e) throws TransformerException {
            parseAndAppendStatusInfo(e);
        }

        /** {@inheritDoc} */
        public void warning(TransformerException e) throws TransformerException {
            parseAndAppendStatusInfo(e);
        }

        /**
         * Parses the error message and appends the appropriate status info to the Item.
         * 
         * @param e the error to parse
         * 
         * @throws TransformerException thrown if the error does not contain the appropriate message prefix
         */
        private void parseAndAppendStatusInfo(TransformerException e) throws TransformerException {
            String errorMessage = StringSupport.trimOrNull(e.getMessage());
            if (errorMessage == null) {
                throw e;
            }

            String statusMessage;
            if (errorMessage.startsWith(ERROR_PREFIX)) {
                statusMessage = StringSupport.trim(errorMessage.substring(ERROR_PREFIX.length()));
                item.getItemMetadata().put(new ErrorStatus(getId(), statusMessage));
            } else if (errorMessage.startsWith(WARN_PREFIX)) {
                statusMessage = StringSupport.trim(errorMessage.substring(ERROR_PREFIX.length()));
                item.getItemMetadata().put(new WarningStatus(getId(), statusMessage));
            } else {
                throw e;
            }

        }
    }
}