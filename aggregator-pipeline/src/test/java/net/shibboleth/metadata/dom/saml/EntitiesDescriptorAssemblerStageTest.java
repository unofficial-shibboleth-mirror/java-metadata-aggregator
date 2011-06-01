/*
 * Licensed to the University Corporation for Advanced Internet Development, Inc.
 * under one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache 
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

package net.shibboleth.metadata.dom.saml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import net.shibboleth.metadata.dom.BaseDomTest;
import net.shibboleth.metadata.dom.DomElementItem;

import org.opensaml.util.xml.SerializeSupport;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class EntitiesDescriptorAssemblerStageTest extends BaseDomTest {

    @Test
    public void testAssemblingWithoutName() throws Exception {
        Collection<DomElementItem> metadataCollection = buildMetadataCollection();
        EntitiesDescriptorAssemblerStage stage = new EntitiesDescriptorAssemblerStage();
        stage.setId("foo");
        stage.initialize();
        stage.execute(metadataCollection);

        Document result = metadataCollection.iterator().next().unwrap().getOwnerDocument();
        String serializedResult = SerializeSupport.nodeToString(result);
        result = getParserPool().parse(new StringReader(serializedResult));

        Element expectedResult = readXmlData("samlMetadata/entitiesDescriptor2.xml");

        assertXmlEqual(expectedResult, result);
    }

    protected Collection<DomElementItem> buildMetadataCollection() throws Exception {
        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();

        Element descriptor = readXmlData("samlMetadata/entityDescriptor1.xml");
        metadataCollection.add(new DomElementItem(descriptor));

        descriptor = readXmlData("samlMetadata/entityDescriptor2.xml");
        metadataCollection.add(new DomElementItem(descriptor));

        Element fooElement = getParserPool().newDocument().createElement("foo");
        metadataCollection.add(new DomElementItem(fooElement));

        return metadataCollection;
    }
}