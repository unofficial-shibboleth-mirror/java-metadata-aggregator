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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.resource.Resource;
import net.shibboleth.utilities.java.support.resource.ResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * A pipeline stage which applies and XSLT to each element in the {@link DOMElementItem} collection.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>xslResource</code></li>
 * </ul>
 */
@ThreadSafe
public abstract class AbstractXSLProcessingStage extends BaseStage<Element> {

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
    
    /** {@link URIResolver} to use in the transformer. Default value: <code>null</code>. */
    @Nullable private URIResolver uriResolver;

    /**
     * Gets the resource that provides the XSL document.
     * 
     * @return resource that provides the XSL document
     */
    @Nullable public Resource getXSLResource() {
        return xslResource;
    }

    /**
     * Sets the resource that provides the XSL document.
     * 
     * @param resource resource that provides the XSL document
     */
    public synchronized void setXSLResource(@Nonnull final Resource resource) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        xslResource = Constraint.isNotNull(resource, "XSL resource can not be null");
    }

    /**
     * Gets the unmodifiable collection of attributes used by the XSLT transformer.
     * 
     * @return unmodifiable collection of attributes used by the XSLT transformer, never null nor containing null keys
     */
    @Nonnull @NonnullElements @Unmodifiable public Map<String, Object> getTransformAttributes() {
        return transformAttributes;
    }

    /**
     * Sets the collection of attributes used by the XSLT transformer.
     * 
     * @param attributes collection of attributes used by the XSLT transformer, may be null or contain null keys
     */
    public synchronized void setTransformAttributes(@Nullable @NullableElements final Map<String, Object> attributes) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        if (attributes == null || attributes.isEmpty()) {
            transformAttributes = Collections.emptyMap();
        }

        final HashMap<String, Object> newAttributes = new HashMap<>();
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
    @Nonnull @NonnullElements @Unmodifiable public Map<String, Boolean> getTransformFeatures() {
        return transformFeatures;
    }

    /**
     * Sets the collection of features used by the XSLT transformer.
     * 
     * @param features collection of features used by the XSLT transformer, may be null or contain null keys
     */
    public synchronized void setTransformFeatures(@Nullable @NullableElements final Map<String, Boolean> features) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        if (features == null || features.isEmpty()) {
            transformFeatures = Collections.emptyMap();
        }

        final HashMap<String, Boolean> newFeatures = new HashMap<>();
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
    @Nonnull @NonnullElements @Unmodifiable public Map<String, Object> getTransformParameters() {
        return transformParameters;
    }

    /**
     * Sets the named parameters for the transform.
     * 
     * @param parameters parameters for the transform, may be null or contain null keys
     */
    public synchronized void setTransformParameters(@Nullable @NullableElements final Map<String, Object> parameters) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        if (parameters == null) {
            transformParameters = Collections.emptyMap();
            return;
        }

        final HashMap<String, Object> newParams = new HashMap<>();
        for (String paramName : parameters.keySet()) {
            if (paramName != null) {
                newParams.put(paramName, parameters.get(paramName));
            }
        }

        transformParameters = Collections.unmodifiableMap(newParams);
    }

    /**
     * Gets the {@link URIResolver} set for this transform, if any.
     * 
     * @return the {@link URIResolver}, or <code>null</code>
     */
    @Nullable public URIResolver getURIResolver() {
        return uriResolver;
    }

    /**
     * Set the {@link URIResolver} for this transform, or <code>null</code> to
     * specify none.
     * 
     * @param resolver the {@link URIResolver} to use, or <code>null</code>
     */
    public synchronized void setURIResolver(@Nullable final URIResolver resolver) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        uriResolver = resolver;
    }

    /** {@inheritDoc} */
    protected void doExecute(@Nonnull @NonnullElements final Collection<Item<Element>> itemCollection)
            throws StageProcessingException {
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
    protected abstract void executeTransformer(@Nonnull final Transformer transformer,
            @Nonnull @NonnullElements final Collection<Item<Element>> itemCollection) throws StageProcessingException,
            TransformerConfigurationException;

    /** {@inheritDoc} */
    protected void doDestroy() {
        xslResource.destroy();
        xslResource = null;
        xslTemplate = null;
        transformAttributes = null;
        transformFeatures = null;
        transformParameters = null;
        uriResolver = null;

        super.doDestroy();
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (xslResource == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", XslResource must not be null");
        }

        if (!xslResource.isInitialized()) {
            xslResource.initialize();
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
            
            if (uriResolver != null) {
                tfactory.setURIResolver(uriResolver);
            }

            log.debug("{} pipeline stage compiling XSL file {}", getId(), xslResource);
            xslTemplate =
                    tfactory.newTemplates(new StreamSource(xslResource.getInputStream(), xslResource.getLocation()));
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
     * as the message for the added {@link WarningStatus}. If the message begins with {@value #INFO_PREFIX} the
     * remainder of the error message is used as the message for the added {@link InfoStatus}. If the message does not
     * begin with either prefix the exception is re-thrown to be handed by the {@link Transformer}.
     * 
     * This listener works well in conjunction with &lt;xsl:message&gt;
     */
    public class StatusInfoAppendingErrorListener implements ErrorListener {

        /** Prefix used by messages that result in an {@link ErrorStatus}. */
        public static final String ERROR_PREFIX = "[ERROR]";

        /** Prefix used by messages that result in an {@link WarningStatus}. */
        public static final String WARN_PREFIX = "[WARN]";

        /** Prefix used by messages that result in an {@link InfoStatus}. */
        public static final String INFO_PREFIX = "[INFO]";

        /** Item to which the status info will be appended. */
        private Item<?> item;

        /**
         * Constructor.
         * 
         * @param receivingItem that Item to which the status info will be appended
         */
        public StatusInfoAppendingErrorListener(@Nonnull final Item<?> receivingItem) {
            item = receivingItem;
        }

        /** {@inheritDoc} */
        public void error(@Nonnull final TransformerException e) throws TransformerException {
            parseAndAppendStatusInfo(e);
        }

        /** {@inheritDoc} */
        public void fatalError(@Nonnull final TransformerException e) throws TransformerException {
            parseAndAppendStatusInfo(e);
        }

        /** {@inheritDoc} */
        public void warning(@Nonnull final TransformerException e) throws TransformerException {
            parseAndAppendStatusInfo(e);
        }

        /**
         * Parses the error message and appends the appropriate status info to the Item.
         * 
         * @param e the error to parse
         * 
         * @throws TransformerException thrown if the error does not contain the appropriate message prefix
         */
        private void parseAndAppendStatusInfo(@Nonnull final TransformerException e) throws TransformerException {
            String errorMessage = StringSupport.trimOrNull(e.getMessage());
            if (errorMessage == null) {
                throw e;
            }

            String statusMessage;
            if (errorMessage.startsWith(ERROR_PREFIX)) {
                statusMessage = StringSupport.trim(errorMessage.substring(ERROR_PREFIX.length()));
                item.getItemMetadata().put(new ErrorStatus(getId(), statusMessage));
            } else if (errorMessage.startsWith(WARN_PREFIX)) {
                statusMessage = StringSupport.trim(errorMessage.substring(WARN_PREFIX.length()));
                item.getItemMetadata().put(new WarningStatus(getId(), statusMessage));
            } else if (errorMessage.startsWith(INFO_PREFIX)) {
                statusMessage = StringSupport.trim(errorMessage.substring(INFO_PREFIX.length()));
                item.getItemMetadata().put(new InfoStatus(getId(), statusMessage));
            } else {
                throw e;
            }

        }
    }
}