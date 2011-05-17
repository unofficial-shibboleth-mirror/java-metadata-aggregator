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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import net.shibboleth.metadata.dom.DomElementItem;

import org.opensaml.util.resource.FilesystemResource;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.xml.BasicParserPool;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

public class XMLSchemaValidationStageTest {

    @Test
    public void testValidXml() throws Exception {
        XMLSchemaValidationStage stage = buildStage();

        Collection<DomElementItem> mdCol = buildMetdataCollection("/data/samlMetadata/entitiesDescriptor1.xml");
        stage.execute(mdCol);
        assert mdCol.size() == 1;
    }

    @Test
    public void testInvalidXml() throws Exception {
        XMLSchemaValidationStage stage = buildStage();
        Collection<DomElementItem> mdCol = buildMetdataCollection("/data/samlMetadata/invalidSamlMetadata.xml.xml");
        stage.execute(mdCol);
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

    protected Collection<DomElementItem> buildMetdataCollection(String xmlPath) throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        Document doc = parserPool.parse(XMLSchemaValidationStageTest.class.getResourceAsStream(xmlPath));

        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(new DomElementItem(doc.getDocumentElement()));

        return mdCol;
    }
}