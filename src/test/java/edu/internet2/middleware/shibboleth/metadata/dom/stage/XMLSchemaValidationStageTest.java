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

import java.io.File;
import java.util.ArrayList;

import org.opensaml.util.resource.FilesystemResource;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.xml.BasicParserPool;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadata;

public class XMLSchemaValidationStageTest {

    @Test
    public void testValidXml() throws Exception {
        XMLSchemaValidationStage stage = buildStage();
        MetadataCollection<DomMetadata> mdCol = stage.execute(buildMetdataCollection("/data/samlMetadata.xml"));
        assert mdCol.size() == 1;
    }

    @Test
    public void testInvalidXml() throws Exception {
        XMLSchemaValidationStage stage = buildStage();
        MetadataCollection<DomMetadata> mdCol = stage.execute(buildMetdataCollection("/data/invalidSamlMetadata.xml"));
        assert mdCol.size() == 0;
    }

    protected XMLSchemaValidationStage buildStage() throws Exception {
        ArrayList<Resource> schemas = new ArrayList<Resource>();
        File schemaDir = new File(XMLSchemaValidationStageTest.class.getResource("/schemas").toURI());
        for (String dirFile : schemaDir.list()) {
            if (dirFile.endsWith(".xsd")) {
                schemas.add(new FilesystemResource(dirFile));
            }
        }

        XMLSchemaValidationStage stage = new XMLSchemaValidationStage();
        stage.setId("test");
        stage.setSchemaResources(schemas);
        stage.initialize();

        return stage;
    }

    protected SimpleMetadataCollection<DomMetadata> buildMetdataCollection(String xmlPath) throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        Document doc = parserPool.parse(XMLSchemaValidationStageTest.class.getResourceAsStream(xmlPath));

        SimpleMetadataCollection<DomMetadata> mdCol = new SimpleMetadataCollection<DomMetadata>();
        mdCol.add(new DomMetadata(doc.getDocumentElement()));

        return mdCol;
    }
}