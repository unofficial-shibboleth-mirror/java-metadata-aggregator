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

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;

public class XMLSchemaValidationStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public XMLSchemaValidationStageTest() {
        super(XMLSchemaValidationStage.class);
    }

    @Test
    public void testValidXml() throws Exception {
        XMLSchemaValidationStage stage = buildStage();

        List<Item<Element>> mdCol = buildMetdataCollection("valid.xml");
        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 1);
        Assert.assertFalse(mdCol.iterator().next().getItemMetadata().containsKey(ErrorStatus.class));
    }

    @Test
    public void testInvalidXml() throws Exception {
        XMLSchemaValidationStage stage = buildStage();
        List<Item<Element>> mdCol = buildMetdataCollection("invalid.xml");
        stage.execute(mdCol);
        stage.destroy();
        Assert.assertEquals(mdCol.size(), 1);
        Assert.assertTrue(mdCol.iterator().next().getItemMetadata().containsKey(ErrorStatus.class));
    }

    protected XMLSchemaValidationStage buildStage() throws Exception {
        final List<Resource> schemaResources = new ArrayList<>();
        schemaResources.add(getClasspathResource("schema.xsd"));

        XMLSchemaValidationStage stage = new XMLSchemaValidationStage();
        stage.setId("test");
        stage.setSchemaResources(schemaResources);
        stage.initialize();

        return stage;
    }

    protected List<Item<Element>> buildMetdataCollection(String xmlPath) throws Exception {
        final Element element = readXMLData(xmlPath);
        final List<Item<Element>> mdCol = new ArrayList<>();
        mdCol.add(new DOMElementItem(element));
        return mdCol;
    }
}
