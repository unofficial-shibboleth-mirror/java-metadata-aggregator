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

package edu.internet2.middleware.shibboleth.metadata.dom.stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import net.jcip.annotations.ThreadSafe;

import org.opensaml.util.Assert;
import org.opensaml.util.xml.SchemaBuilder;
import org.opensaml.util.xml.SchemaBuilder.SchemaLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadata;
import edu.internet2.middleware.shibboleth.metadata.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.pipeline.ComponentInitializationException;
import edu.internet2.middleware.shibboleth.metadata.pipeline.Stage;
import edu.internet2.middleware.shibboleth.metadata.pipeline.StageProcessingException;

/**
 * A pipeline stage that XML schema validates the elements within the metadata collection.
 */
@ThreadSafe
public class XMLSchemaValidationStage extends AbstractComponent implements Stage<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(XMLSchemaValidationStage.class);

    /** File paths to schema files. */
    private String[] schemaFiles;

    /** Schema used to validate the metadata. */
    private Schema validationSchema;

    /**
     * Constructor.
     * 
     * @param stageId unique stage ID
     * @param schemas filesystem paths to the schema files
     */
    public XMLSchemaValidationStage(String stageId, List<String> schemas) {
        super(stageId);
        Assert.isNotNull(schemas, "Schema files may not be null");
        schemaFiles = schemas.toArray(new String[schemas.size()]);
    }

    /**
     * Gets an unmodifiable list of schema files against which data is validated.
     * 
     * @return unmodifiable list of schema files against which data is validated
     */
    public List<String> getSchemaFiles() {
        return Collections.unmodifiableList(Arrays.asList(schemaFiles));
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(MetadataCollection<DomMetadata> metadataCollection)
            throws StageProcessingException {
        log.debug("{} pipeline stage schema validating metadata collection elements", getId());
        SimpleMetadataCollection<DomMetadata> mec = new SimpleMetadataCollection<DomMetadata>();

        Validator validator = validationSchema.newValidator();
        DOMResult result;
        for (DomMetadata metadata : metadataCollection) {
            try {
                result = new DOMResult();
                validator.validate(new DOMSource(metadata.getMetadata()), result);
                mec.add(new DomMetadata((Element) result.getNode()));
            } catch (SAXException e) {
                throw new StageProcessingException("Metadata failed validation", e);
            } catch (IOException e) {
                throw new StageProcessingException("Metadata failed validation", e);
            }
        }

        return mec;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        try {
            log.debug("{} pipeline stage building validation schema from files {}", getId(), schemaFiles);
            validationSchema = SchemaBuilder.buildSchema(SchemaLanguage.XML, schemaFiles);
        } catch (SAXException e) {
            throw new ComponentInitializationException("Unable to generate schema", e);
        }
    }
}