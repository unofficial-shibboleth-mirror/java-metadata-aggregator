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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.utilities.java.support.resource.FilesystemResource;
import net.shibboleth.utilities.java.support.resource.Resource;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

public class XMLSchemaValidationStageTest {

    @Test
    public void testValidXml() throws Exception {
        XMLSchemaValidationStage stage = buildStage();

        Collection<DomElementItem> mdCol = buildMetdataCollection("/data/xmlSchemaValidationStageValidInput.xml");
        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);
    }

    @Test
    public void testInvalidXml() throws Exception {
        XMLSchemaValidationStage stage = buildStage();
        Collection<DomElementItem> mdCol = buildMetdataCollection("/data/xmlSchemaValidationStageInvalidInput.xml");
        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);
        Assert.assertTrue(mdCol.iterator().next().getItemMetadata().containsKey(ErrorStatus.class));
    }

    protected XMLSchemaValidationStage buildStage() throws Exception {
        String schemaFile =
                new File(XMLSchemaValidationStageTest.class.getResource("/data/xmlSchemaValidationStage.xsd").toURI())
                        .getAbsolutePath();
        final List<Resource> schemaResources = new ArrayList<>();
        schemaResources.add(new FilesystemResource(schemaFile));

        XMLSchemaValidationStage stage = new XMLSchemaValidationStage();
        stage.setId("test");
        stage.setSchemaResources(schemaResources);
        stage.initialize();

        return stage;
    }

    protected Collection<DomElementItem> buildMetdataCollection(String xmlPath) throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        Document doc = parserPool.parse(XMLSchemaValidationStageTest.class.getResourceAsStream(xmlPath));

        final List<DomElementItem> mdCol = new ArrayList<>();
        mdCol.add(new DomElementItem(doc.getDocumentElement()));

        return mdCol;
    }
}