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
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.SchemaBuilder;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;

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
    private final Logger log = LoggerFactory.getLogger(XMLSchemaValidationStage.class);

    /** Collection of schema resources. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<Resource> schemaResources = List.of();

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
            @Nullable @NonnullElements @Unmodifiable final List<Resource> resources) {
        throwSetterPreconditionExceptions();
        schemaResources = List.copyOf(resources);
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
        throwSetterPreconditionExceptions();
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
        log.debug("{} pipeline stage schema validating DOM Element collection elements", getId());

        final Validator validator = getValidationSchema().newValidator();
        try {
            validator.validate(new DOMSource(item.unwrap()));
        } catch (final Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("DOM Element was not valid:\n{}", SerializeSupport.prettyPrintXML(item.unwrap()), e);
            }
            if (isElementRequiredToBeSchemaValid()) {
                item.getItemMetadata().put(new ErrorStatus(getId(), e.getMessage()));
            } else {
                item.getItemMetadata().put(new WarningStatus(getId(), e.getMessage()));
            }
        }
    }

    @Override
    protected void doDestroy() {
        schemaResources = null;
        validationSchema = null;
        
        super.doDestroy();
    }
    
    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (schemaResources.isEmpty()) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", SchemaResources may not be empty");
        }
        
        try {
            log.debug("{} pipeline stage building validation schema resources", getId());
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
