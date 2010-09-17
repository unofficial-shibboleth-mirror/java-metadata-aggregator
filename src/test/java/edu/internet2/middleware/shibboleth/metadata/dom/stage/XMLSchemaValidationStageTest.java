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

import org.opensaml.util.xml.StaticBasicParserPool;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.SimpleMetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadata;

public class XMLSchemaValidationStageTest {

    @Test
    public void testValidXml() throws Exception {
        StaticBasicParserPool parserPool = new StaticBasicParserPool();
        parserPool.initialize();
        Document doc = parserPool.parse(XMLSchemaValidationStageTest.class.getResourceAsStream("/data/samlMetadata.xml"));
        
        MetadataCollection<DomMetadata> mdCol = new SimpleMetadataCollection<DomMetadata>();
        mdCol.add(new DomMetadata(doc.getDocumentElement()));
        
        File schemaDir = new File(XMLSchemaValidationStageTest.class.getResource("/schemas").toURI());
        ArrayList<String> schemas = new ArrayList<String>();
        schemas.add(schemaDir.getAbsolutePath());
        
        XMLSchemaValidationStage stage = new XMLSchemaValidationStage("test", schemas);
        stage.initialize();
        
        mdCol = stage.execute(mdCol);
        assert mdCol.size() == 1;
        assert mdCol.iterator().next().getMetadata().equals(doc.getDocumentElement());
    }
    
    @Test
    public void testInvalidXml() throws Exception {
        StaticBasicParserPool parserPool = new StaticBasicParserPool();
        parserPool.initialize();
        Document doc = parserPool.parse(XMLSchemaValidationStageTest.class.getResourceAsStream("/data/invalidSamlMetadata.xml"));
        
        MetadataCollection<DomMetadata> mdCol = new SimpleMetadataCollection<DomMetadata>();
        mdCol.add(new DomMetadata(doc.getDocumentElement()));
        
        File schemaDir = new File(XMLSchemaValidationStageTest.class.getResource("/schemas").toURI());
        ArrayList<String> schemas = new ArrayList<String>();
        schemas.add(schemaDir.getAbsolutePath());
        
        XMLSchemaValidationStage stage = new XMLSchemaValidationStage("test", schemas);
        stage.initialize();
        
        mdCol = stage.execute(mdCol);
        assert mdCol.size() == 0;
    }
}