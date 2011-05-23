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

package net.shibboleth.metadata.dom;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import net.shibboleth.metadata.AssertSupport;
import net.shibboleth.metadata.dom.DomElementItem;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import edu.vt.middleware.crypt.util.CryptReader;

/** {@link XMLSignatureSigningStage} unit test. */
public class XMLSignatureSigningStageTest extends BaseDomTest {

    /** Test signing with and verifying the result against a known good. */
    @Test
    public void testSigning() throws Exception {
        Element testInput = readXmlData("samlMetadata.xml");

        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(new DomElementItem(testInput));

        PrivateKey signingKey = CryptReader.readPrivateKey(XMLSignatureSigningStageTest.class
                .getResourceAsStream("/data/signingKey.pem"));
        X509Certificate signingCert = (X509Certificate) CryptReader.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream("/data/signingCert.pem"));
        ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>();
        certs.add(signingCert);

        XMLSignatureSigningStage stage = new XMLSignatureSigningStage();
        stage.setId("test");
        stage.setIncludeKeyValue(false);
        stage.setIncludeX509IssuerSerial(true);
        stage.setPrivateKey(signingKey);
        stage.setCertificates(certs);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DomElementItem result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, XMLSignatureSigningStage.class, "test");

        Element expected = readXmlData("signedSamlMetadata.xml");
        assertXmlEqual(expected, result.unwrap());
    }
}