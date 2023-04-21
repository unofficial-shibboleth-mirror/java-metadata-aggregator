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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.shared.annotation.constraint.NonnullAfterInit;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.primitive.LoggerFactory;
import net.shibboleth.shared.xml.SchemaBuilder;
import net.shibboleth.shared.xml.SerializeSupport;

/**
 * A pipeline stage that XML schema validates the elements within the {@link DOMElementItem} collection.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>schemaResources</code></li>
 * </ul>
 * 
 * If DOM Elements are required to be valid, per {@link #isElementRequiredToBeSchemaValid()} and an Element is found to
 * be invalid than an {@link ErrorStatus} object is set on the element. If the Element is not required to be valid and
 * an Element is found to be invalid than an {@link WarningStatus} is set on the Element.
 */
@ThreadSafe
public class XMLSchemaValidationStage extends AbstractIteratingStage<Element> {

    /** Class logger. */
    private static final @Nonnull Logger LOG = LoggerFactory.getLogger(XMLSchemaValidationStage.class);

    /** Collection of schema resources. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<Resource> schemaResources = CollectionSupport.emptyList();

    /** Whether Elements are required to be schema valid. Default value: <code>true</code> */
    @GuardedBy("this")
    private boolean elementRequiredToBeSchemaValid = true;

    /** Schema used to validate Elements. Built during initialization. */
    @NonnullAfterInit @GuardedBy("this")
    private Schema validationSchema;

    /**
     * Gets an unmodifiable list of schema resources against which Elements are validated.
     * 
     * @return unmodifiable list of schema resources against which Elements are validated
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized List<Resource> getSchemaResources() {
        return schemaResources;
    }

    /**
     * Sets the schema resources against which Elements are validated.
     * 
     * @param resources schema resources against which Elements are validated
     */
    public synchronized void setSchemaResources(
            final @Nonnull @NonnullElements @Unmodifiable List<Resource> resources) {
        checkSetterPreconditions();
        schemaResources = CollectionSupport.copyToList(resources);
    }

    /**
     * Gets whether Elements are required to be schema valid.
     * 
     * @return whether Elements are required to be schema valid
     */
    public final synchronized boolean isElementRequiredToBeSchemaValid() {
        return elementRequiredToBeSchemaValid;
    }

    /**
     * Sets whether Elements are required to be schema valid.
     * 
     * @param isRequired whether Elements are required to be schema valid
     */
    public synchronized void setElementRequiredToBeSchemaValid(final boolean isRequired) {
        checkSetterPreconditions();
        elementRequiredToBeSchemaValid = isRequired;
    }

    /**
     * Returns the shared validation schema built during initialization.
     *
     * @return the shared validation schema
     */
    private synchronized Schema getValidationSchema() {
        return validationSchema;
    }

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        LOG.debug("{} pipeline stage schema validating DOM Element collection elements", getId());

        final Validator validator = getValidationSchema().newValidator();
        try {
            validator.validate(new DOMSource(item.unwrap()));
        } catch (final Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("DOM Element was not valid:\n{}", SerializeSupport.prettyPrintXML(item.unwrap()), e);
            }
            if (isElementRequiredToBeSchemaValid()) {
                item.getItemMetadata().put(new ErrorStatus(ensureId(), e.getMessage()));
            } else {
                item.getItemMetadata().put(new WarningStatus(ensureId(), e.getMessage()));
            }
        }
    }

    @Override
    protected synchronized void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (schemaResources.isEmpty()) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", SchemaResources may not be empty");
        }
        
        try {
            LOG.debug("{} pipeline stage building validation schema resources", getId());
            final SchemaBuilder builder = new SchemaBuilder();
            for (final Resource schemaResource : schemaResources) {
                try {
                    builder.addSchema(new StreamSource(schemaResource.getInputStream(),
                            schemaResource.getDescription()));
                } catch (final IOException e) {
                    throw new ComponentInitializationException("Unable to read schema resource " +
                            schemaResource.getDescription(), e);
                }
            }
            validationSchema = builder.buildSchema();
        } catch (final SAXException e) {
            throw new ComponentInitializationException("Unable to generate schema", e);
        }
    }
}
