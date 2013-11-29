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
import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.resource.FilesystemResource;
import net.shibboleth.utilities.java.support.resource.Resource;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class XMLSchemaValidationStageTest extends BaseDOMTest {

    @BeforeClass
    private void init() {
        setTestingClass(XMLSchemaValidationStage.class);
    }

    @Test
    public void testValidXml() throws Exception {
        XMLSchemaValidationStage stage = buildStage();

        Collection<Item<Element>> mdCol = buildMetdataCollection("valid.xml");
        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);
        Assert.assertFalse(mdCol.iterator().next().getItemMetadata().containsKey(ErrorStatus.class));
    }

    @Test
    public void testInvalidXml() throws Exception {
        XMLSchemaValidationStage stage = buildStage();
        Collection<Item<Element>> mdCol = buildMetdataCollection("invalid.xml");
        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);
        Assert.assertTrue(mdCol.iterator().next().getItemMetadata().containsKey(ErrorStatus.class));
    }

    protected XMLSchemaValidationStage buildStage() throws Exception {
        String schemaFile =
                new File(getClasspathResource("schema.xsd").getURI())
                        .getAbsolutePath();
        final List<Resource> schemaResources = new ArrayList<>();
        schemaResources.add(new FilesystemResource(schemaFile));

        XMLSchemaValidationStage stage = new XMLSchemaValidationStage();
        stage.setId("test");
        stage.setSchemaResources(schemaResources);
        stage.initialize();

        return stage;
    }

    protected Collection<Item<Element>> buildMetdataCollection(String xmlPath) throws Exception {
        final Element element = readXMLData(xmlPath);
        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(new DOMElementItem(element));
        return mdCol;
    }
}