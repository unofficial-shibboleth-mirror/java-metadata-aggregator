/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
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

package net.shibboleth.metadata.dom.saml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.dom.stage.XMLSignatureSigningStageTest;

import org.opensaml.util.xml.BasicParserPool;
import org.opensaml.util.xml.SerializeSupport;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class EntitiesDescriptorAssemblerStageTest {

    @Test
    public void testAssemblingWithoutName() throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        Collection<DomMetadata> metadataCollection = buildMetadataCollection();

        EntitiesDescriptorAssemblerStage stage = new EntitiesDescriptorAssemblerStage();
        stage.setId("foo");
        stage.initialize();

        stage.execute(metadataCollection);
        Document result = metadataCollection.iterator().next().getMetadata().getOwnerDocument();

        String serializedResult = SerializeSupport.nodeToString(result);
        result = parserPool.parse(new StringReader(serializedResult));

        Document expectedResult = parserPool.parse(XMLSignatureSigningStageTest.class
                .getResourceAsStream("/data/entitiesDescriptor.xml"));

        Assert.assertTrue(result.getDocumentElement().isEqualNode(expectedResult.getDocumentElement()));

        // TODO figure out how to do an equality match against /data/entitiesDescriptor.xml
    }

    protected Collection<DomMetadata> buildMetadataCollection() throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        ArrayList<DomMetadata> metadataCollection = new ArrayList<DomMetadata>();

        Element descriptor = parserPool.parse(
                SetCacheDurationStageTest.class.getResourceAsStream("/data/entityDescriptor1.xml"))
                .getDocumentElement();
        metadataCollection.add(new DomMetadata(descriptor));

        descriptor = parserPool.parse(
                SetCacheDurationStageTest.class.getResourceAsStream("/data/entityDescriptor2.xml"))
                .getDocumentElement();
        metadataCollection.add(new DomMetadata(descriptor));

        Element fooElement = parserPool.newDocument().createElement("foo");
        metadataCollection.add(new DomMetadata(fooElement));

        return metadataCollection;
    }
}