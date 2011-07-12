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

import java.util.Collections;
import java.util.List;

import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazyList;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.xml.SchemaBuilder;
import org.opensaml.util.xml.SchemaBuilder.SchemaLanguage;
import org.opensaml.util.xml.SerializeSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * A pipeline stage that XML schema validates the elements within the {@link DomElementItem} collection.
 * 
 * If DOM Elements are required to be valid, per {@link #isElementRequiredToBeSchemaValid()} and an Element is found to
 * be invalid than an {@link ErrorStatus} object is set on the element. If the Element is not required to be valid and
 * an Element is found to be invalid than an {@link WarningStatus} is set on the Element.
 */
@ThreadSafe
public class XMLSchemaValidationStage extends BaseIteratingStage<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XMLSchemaValidationStage.class);

    /** Collection of schema resources. */
    private List<Resource> schemaResources = Collections.emptyList();

    /** Whether Elements are required to be schema valid. Default value: {@value} */
    private boolean elementRequiredToBeSchemaValid = true;

    /** Schema used to validate Elements. */
    private Schema validationSchema;

    /**
     * Gets an unmodifiable list of schema resources against which Elements are validated.
     * 
     * @return unmodifiable list of schema resources against which Elements are validated
     */
    public List<Resource> getSchemaResources() {
        return schemaResources;
    }

    /**
     * Sets the schema resources against which Elements are validated.
     * 
     * @param resources schema resources against which Elements are validated
     */
    public synchronized void setSchemaResources(final List<Resource> resources) {
        if (isInitialized()) {
            return;
        }
        schemaResources =
                Collections.unmodifiableList(CollectionSupport.addNonNull(resources, new LazyList<Resource>()));
    }

    /**
     * Gets whether Elements are required to be schema valid.
     * 
     * @return whether Elements are required to be schema valid
     */
    public boolean isElementRequiredToBeSchemaValid() {
        return elementRequiredToBeSchemaValid;
    }

    /**
     * Sets whether Elements are required to be schema valid.
     * 
     * @param isRequired whether Elements are required to be schema valid
     */
    public synchronized void setElementRequiredToBeSchemaValid(boolean isRequired) {
        if (isInitialized()) {
            return;
        }
        elementRequiredToBeSchemaValid = isRequired;
    }

    /** {@inheritDoc} */
    protected boolean doExecute(DomElementItem item) throws StageProcessingException {
        log.debug("{} pipeline stage schema validating DOM Element collection elements", getId());

        final Validator validator = validationSchema.newValidator();
        try {
            validator.validate(new DOMSource(item.unwrap()));
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("DOM Element was not valid:\n{}", SerializeSupport.prettyPrintXML(item.unwrap()), e);
            }
            if (elementRequiredToBeSchemaValid) {
                item.getItemMetadata().put(new ErrorStatus(getId(), e.getMessage()));
            } else {
                item.getItemMetadata().put(new WarningStatus(getId(), e.getMessage()));
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (schemaResources == null || schemaResources.isEmpty()) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", SchemaResources may not be empty");
        }

        try {
            log.debug("{} pipeline stage building validation schema resources", getId());
            validationSchema =
                    SchemaBuilder.buildSchema(SchemaLanguage.XML,
                            schemaResources.toArray(new Resource[schemaResources.size()]));
        } catch (SAXException e) {
            throw new ComponentInitializationException("Unable to generate schema", e);
        }
    }
}