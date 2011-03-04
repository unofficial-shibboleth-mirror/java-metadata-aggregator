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

import java.security.Security;
import java.security.cert.X509Certificate;

import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.SimpleMetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opensaml.util.xml.BasicParserPool;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import edu.vt.middleware.crypt.util.CryptReader;

/**
 *
 */
public class XMLSignatureValidationStageTest {

    @Test
    public void testValidSignature() throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        Document doc = parserPool.parse(XMLSignatureSigningStageTest.class
                .getResourceAsStream("/data/signedSamlMetadata.xml"));

        MetadataCollection<DomMetadata> mdCol = new SimpleMetadataCollection<DomMetadata>();
        mdCol.add(new DomMetadata(doc.getDocumentElement()));

        Security.addProvider(new BouncyCastleProvider());
        X509Certificate signingCert = (X509Certificate) CryptReader.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream("/data/signingCert.pem"));

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setVerificationKey(signingCert);
        stage.initialize();

        mdCol = stage.execute(mdCol);
    }
    
    @Test
    public void testInvalidSignature() throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        Document doc = parserPool.parse(XMLSignatureSigningStageTest.class
                .getResourceAsStream("/data/badSignatureSamlMetadata.xml"));

        MetadataCollection<DomMetadata> mdCol = new SimpleMetadataCollection<DomMetadata>();
        mdCol.add(new DomMetadata(doc.getDocumentElement()));

        Security.addProvider(new BouncyCastleProvider());
        X509Certificate signingCert = (X509Certificate) CryptReader.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream("/data/signingCert.pem"));

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setVerificationKey(signingCert);
        stage.initialize();

        mdCol = stage.execute(mdCol);
        Assert.assertTrue(mdCol.isEmpty());
    }

    @Test
    public void testRequiredSignature() throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        Document doc = parserPool.parse(XMLSignatureSigningStageTest.class
                .getResourceAsStream("/data/samlMetadata.xml"));

        MetadataCollection<DomMetadata> mdCol = new SimpleMetadataCollection<DomMetadata>();
        mdCol.add(new DomMetadata(doc.getDocumentElement()));

        Security.addProvider(new BouncyCastleProvider());
        X509Certificate signingCert = (X509Certificate) CryptReader.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream("/data/signingCert.pem"));

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setSignatureRequired(false);
        stage.setVerificationKey(signingCert);
        stage.initialize();

        mdCol = stage.execute(mdCol);

        stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setSignatureRequired(true);
        stage.setVerificationKey(signingCert);
        stage.initialize();
        mdCol = stage.execute(mdCol);
        Assert.assertTrue(mdCol.isEmpty());
    }
}
