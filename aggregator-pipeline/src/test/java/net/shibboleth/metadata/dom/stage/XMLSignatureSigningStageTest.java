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

import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.SimpleMetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.dom.stage.XMLSignatureSigningStage;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opensaml.util.xml.BasicParserPool;
import org.opensaml.util.xml.SerializeSupport;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import edu.vt.middleware.crypt.util.CryptReader;

/**
 *
 */
public class XMLSignatureSigningStageTest {

    @Test
    public void testSigning() throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        Document doc = parserPool.parse(XMLSchemaValidationStageTest.class
                .getResourceAsStream("/data/samlMetadata.xml"));

        MetadataCollection<DomMetadata> mdCol = new SimpleMetadataCollection<DomMetadata>();
        mdCol.add(new DomMetadata(doc.getDocumentElement()));

        Security.addProvider(new BouncyCastleProvider());
        PrivateKey signingKey = CryptReader.readPemPrivateKey(
                XMLSchemaValidationStageTest.class.getResourceAsStream("/data/signingKey.pem"), null);
        X509Certificate signingCert = (X509Certificate) CryptReader.readCertificate(XMLSchemaValidationStageTest.class
                .getResourceAsStream("/data/signingCert.pem"));
        ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>();
        certs.add(signingCert);

        XMLSignatureSigningStage stage = new XMLSignatureSigningStage();
        stage.setId("test");
        stage.setIncludeKeyValue(false);
        stage.setIncludeX509IssuerSerial(true);
        stage.setPrivKey(signingKey);
        stage.setCertificates(certs);
        stage.initialize();

        mdCol = stage.execute(mdCol);
        System.out.println(SerializeSupport.prettyPrintXML(mdCol.iterator().next().getMetadata()));
    }
}
