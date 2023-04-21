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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.pipeline.AbstractStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.shared.annotation.constraint.NonnullAfterInit;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.primitive.LoggerFactory;
import net.shibboleth.shared.primitive.StringSupport;

/**
 * An abstract stage which applies an XSL transformation to each element in the {@link DOMElementItem} collection.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>xslResource</code></li>
 * </ul>
 */
@ThreadSafe
public abstract class AbstractXSLProcessingStage extends AbstractStage<Element> {

    /** Class logger. */
    private static final @Nonnull Logger LOG = LoggerFactory.getLogger(AbstractXSLProcessingStage.class);

    /** Resource that provides the XSL document. */
    @NonnullAfterInit @GuardedBy("this")
    private Resource xslResource;

    /**
     * XSL template used to transform <code>Element</code>s.
     * 
     * <p>
     * A single shared <code>Templates</code> object is constructed from the
     * <code>xslResource</code>, <code>transformAttributes</code>,
     * <code>transformFeatures</code> and any <code>uriResolver</code>
     * during initialisation of the stage.
     * </p>
     */
    @NonnullAfterInit @GuardedBy("this")
    private Templates xslTemplate;

    /** Attributes set on the {@link Transformer} used by this stage. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private Map<String, Object> transformAttributes = CollectionSupport.emptyMap();

    /** Features set on the {@link Transformer} used by this stage. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private Map<String, Boolean> transformFeatures = CollectionSupport.emptyMap();

    /**
     * Collection of named parameters to make available to the transform.
     * 
     * If not set, an empty collection.
     */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private Map<String, Object> transformParameters = CollectionSupport.emptyMap();

    /** {@link URIResolver} to use in the transformer. Default value: <code>null</code>. */
    @Nullable @GuardedBy("this")
    private URIResolver uriResolver;

    /**
     * Gets the resource that provides the XSL document.
     * 
     * @return resource that provides the XSL document
     */
    @Nullable public final synchronized Resource getXSLResource() {
        return xslResource;
    }

    /**
     * Sets the resource that provides the XSL document.
     * 
     * @param resource resource that provides the XSL document
     */
    public synchronized void setXSLResource(@Nonnull final Resource resource) {
        checkSetterPreconditions();
        xslResource = Constraint.isNotNull(resource, "XSL resource can not be null");
    }

    /**
     * Get the shared templates object.
     *
     * @return the shared templates object
     */
    @NonnullAfterInit private synchronized Templates getXSLTemplate() {
        return xslTemplate;
    }

    /**
     * Gets the unmodifiable collection of attributes used by the XSLT transformer.
     * 
     * @return unmodifiable collection of attributes used by the XSLT transformer, never null nor containing null keys
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized Map<String, Object> getTransformAttributes() {
        return transformAttributes;
    }

    /**
     * Sets the collection of attributes used by the XSLT transformer.
     * 
     * @param attributes collection of attributes used by the XSLT transformer, may be null or contain null keys
     */
    public synchronized void setTransformAttributes(
            @Nonnull @NonnullElements @Unmodifiable final Map<String, Object> attributes) {
        checkSetterPreconditions();
        transformAttributes = CollectionSupport.copyToMap(attributes);
    }

    /**
     * Gets the unmodifiable collection of features used by the XSLT transformer.
     * 
     * @return unmodifiable collection of features used by the XSLT transformer, never null nor containing null keys
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized Map<String, Boolean> getTransformFeatures() {
        return transformFeatures;
    }

    /**
     * Sets the collection of features used by the XSLT transformer.
     * 
     * @param features collection of features used by the XSLT transformer, may be null or contain null keys
     */
    public synchronized void setTransformFeatures(
            @Nonnull @NonnullElements @Unmodifiable final Map<String, Boolean> features) {
        checkSetterPreconditions();
        transformFeatures = CollectionSupport.copyToMap(features);
    }

    /**
     * Gets the unmodifiable collection of parameters used by the XSLT transformer.
     * 
     * @return parameters used by the XSLT transformer, never null nor containing null keys
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized Map<String, Object> getTransformParameters() {
        return transformParameters;
    }

    /**
     * Sets the named parameters for the transform.
     * 
     * @param parameters parameters for the transform, may be null or contain null keys
     */
    public synchronized void setTransformParameters(
            @Nonnull @NonnullElements @Unmodifiable final Map<String, Object> parameters) {
        checkSetterPreconditions();
        transformParameters = CollectionSupport.copyToMap(parameters);
    }

    /**
     * Gets the {@link URIResolver} set for this transform, if any.
     * 
     * @return the {@link URIResolver}, or <code>null</code>
     */
    @Nullable public final synchronized URIResolver getURIResolver() {
        return uriResolver;
    }

    /**
     * Set the {@link URIResolver} for this transform, or <code>null</code> to specify none.
     * 
     * @param resolver the {@link URIResolver} to use, or <code>null</code>
     */
    public synchronized void setURIResolver(@Nullable final URIResolver resolver) {
        checkSetterPreconditions();
        uriResolver = resolver;
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<Element>> items)
            throws StageProcessingException {
        try {
            /*
             * Construct a per-execution Transformer from the shared template.
             * Although a single Transformer (with parameters) could in principle be
             * used sequentially by multiple executions, Transformers are not thread-safe
             * and sharing one across threads would require locking against that instance
             * for the whole duration of doExecute, or pooling.
             * 
             * What we're doing here allows executions to overlap, at the cost of building
             * a Transformer for each execution.
             */
            final Transformer transformer = getXSLTemplate().newTransformer();
            assert transformer != null;

            // Set each of the transform's parameters
            for (final Map.Entry<String, Object> entry : getTransformParameters().entrySet()) {
                transformer.setParameter(entry.getKey(), entry.getValue());
            }

            executeTransformer(transformer, items);
        } catch (final TransformerConfigurationException e) {
            throw new RuntimeException("XSL transformation engine misconfigured", e);
        }
    }

    /**
     * Executes the XSLT transform on the given collection of Items.
     * 
     * @param transformer The transform to be applied to each Item. Already has all {@link #transformParameters} set.
     * @param items the Items to which the transform should be applied
     * 
     * @throws StageProcessingException thrown if there is a problem applying the transform to Items
     * @throws TransformerConfigurationException thrown if there is a problem with the Transform itself
     */
    protected abstract void executeTransformer(@Nonnull final Transformer transformer,
            @Nonnull @NonnullElements final Collection<Item<Element>> items) throws StageProcessingException,
            TransformerConfigurationException;

    @Override
    protected synchronized void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (xslResource == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", XslResource must not be null");
        }

        /*
         * Construct the shared (thread-safe) Templates instance. Note that because
         * doInitialize() is called with the stage's monitor held, we can refer to
         * fields directly and do not need to use getters.
         */
        try {
            final TransformerFactory tfactory = TransformerFactory.newInstance();

            for (final Entry<String, Object> attribute : transformAttributes.entrySet()) {
                tfactory.setAttribute(attribute.getKey(), attribute.getValue());
            }

            for (final Entry<String, Boolean> features : transformFeatures.entrySet()) {
                tfactory.setFeature(features.getKey(), features.getValue());
            }

            if (uriResolver != null) {
                tfactory.setURIResolver(uriResolver);
            }

            LOG.debug("{} pipeline stage compiling XSL file {}", getId(), xslResource);
            xslTemplate = tfactory.newTemplates(new StreamSource(xslResource.getInputStream(), 
                                                xslResource.getURL().toExternalForm()));
        } catch (final TransformerConfigurationException e) {
            throw new ComponentInitializationException("XSL transformation engine misconfigured", e);
        } catch (final IOException e) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", error reading XslResource " + xslResource.getDescription() + " information", e);
        }
    }

    /**
     * {@link Transformer} {@link ErrorListener} that sets an {@link ErrorStatus}, {@link WarningStatus} or
     * {@link InfoStatus} on its {@link Item} depending on the {@link TransformerException} message.
     *
     * <ul>
     * <li><p>If the message begins with
     * {@value net.shibboleth.metadata.dom.AbstractXSLProcessingStage.StatusInfoAppendingErrorListener#ERROR_PREFIX}
     * the remainder of the error message is used as the message for the added {@link ErrorStatus}.</p></li>
     *
     * <li><p>If the message begins with
     * {@value net.shibboleth.metadata.dom.AbstractXSLProcessingStage.StatusInfoAppendingErrorListener#WARN_PREFIX}
     * the remainder of the error message is used as the message for the added {@link WarningStatus}.</p></li>
     *
     * <li><p>If the message begins with
     * {@value net.shibboleth.metadata.dom.AbstractXSLProcessingStage.StatusInfoAppendingErrorListener#INFO_PREFIX}
     * the remainder of the error message is used as the message for the added {@link InfoStatus}.</p></li>
     * </ul>
     *
     * <p>If the message does not
     * begin with any of the prefixes the exception is re-thrown to be handed by the {@link Transformer}.</p>
     *
     * <p>This listener works well in conjunction with &lt;xsl:message&gt;.</p>
     */
    @Immutable
    public class StatusInfoAppendingErrorListener implements ErrorListener {

        /** Prefix used by messages that result in an {@link ErrorStatus}. */
        public static final String ERROR_PREFIX = "[ERROR]";

        /** Prefix used by messages that result in an {@link WarningStatus}. */
        public static final String WARN_PREFIX = "[WARN]";

        /** Prefix used by messages that result in an {@link InfoStatus}. */
        public static final String INFO_PREFIX = "[INFO]";

        /** Item to which the status info will be appended. */
        private final Item<?> item;

        /**
         * Constructor.
         * 
         * @param receivingItem that Item to which the status info will be appended
         */
        public StatusInfoAppendingErrorListener(@Nonnull final Item<?> receivingItem) {
            item = receivingItem;
        }

        @Override
        public void error(final TransformerException e) throws TransformerException {
            assert e != null;
            parseAndAppendStatusInfo(e);
        }

        @Override
        public void fatalError(final TransformerException e) throws TransformerException {
            assert e != null;
            parseAndAppendStatusInfo(e);
        }

        @Override
        public void warning(final TransformerException e) throws TransformerException {
            assert e != null;
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
            final String errorMessage = StringSupport.trimOrNull(e.getMessage());
            if (errorMessage == null) {
                throw e;
            }

            final String statusMessage;
            if (errorMessage.startsWith(ERROR_PREFIX)) {
                statusMessage = StringSupport.trim(errorMessage.substring(ERROR_PREFIX.length()));
                item.getItemMetadata().put(new ErrorStatus(ensureId(), statusMessage));
            } else if (errorMessage.startsWith(WARN_PREFIX)) {
                statusMessage = StringSupport.trim(errorMessage.substring(WARN_PREFIX.length()));
                item.getItemMetadata().put(new WarningStatus(ensureId(), statusMessage));
            } else if (errorMessage.startsWith(INFO_PREFIX)) {
                statusMessage = StringSupport.trim(errorMessage.substring(INFO_PREFIX.length()));
                item.getItemMetadata().put(new InfoStatus(ensureId(), statusMessage));
            } else {
                throw e;
            }

        }
    }
}
