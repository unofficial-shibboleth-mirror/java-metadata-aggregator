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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.AbstractComponent;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.Stage;
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


/** A pipeline stage that XML schema validates the elements within the metadata collection. */
@ThreadSafe
public class XMLSchemaValidationStage extends AbstractComponent implements Stage<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XMLSchemaValidationStage.class);

    /** Collection of schema resources. */
    private List<Resource> schemaResources = Collections.emptyList();

    /** Schema used to validate the metadata. */
    private Schema validationSchema;

    /**
     * Gets an unmodifiable list of schema resources against which data is validated.
     * 
     * @return unmodifiable list of schema resources against which data is validated
     */
    public List<Resource> getSchemaFiles() {
        return schemaResources;
    }

    /**
     * Sets the schema resources against which data is validated.
     * 
     * @param resources schema resources against which data is validated
     */
    public synchronized void setSchemaResources(final List<Resource> resources) {
        if (isInitialized()) {
            return;
        }
        schemaResources = Collections.unmodifiableList(CollectionSupport
                .addNonNull(resources, new LazyList<Resource>()));
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(final MetadataCollection<DomMetadata> metadataCollection)
            throws StageProcessingException {
        log.debug("{} pipeline stage schema validating metadata collection elements", getId());

        final Validator validator = validationSchema.newValidator();

        final Iterator<DomMetadata> mdItr = metadataCollection.iterator();
        DomMetadata metadata;
        while (mdItr.hasNext()) {
            metadata = mdItr.next();
            try {
                validator.validate(new DOMSource(metadata.getMetadata()));
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Metadata element was not valid:\n{}",
                            SerializeSupport.prettyPrintXML(metadata.getMetadata()), e);
                    mdItr.remove();
                }
            }
        }

        return metadataCollection;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        if (schemaResources == null || schemaResources.isEmpty()) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", SchemaResources may not be empty");
        }

        try {
            log.debug("{} pipeline stage building validation schema resources", getId());
            validationSchema = SchemaBuilder.buildSchema(SchemaLanguage.XML,
                    schemaResources.toArray(new Resource[schemaResources.size()]));
        } catch (SAXException e) {
            throw new ComponentInitializationException("Unable to generate schema", e);
        }
    }
}