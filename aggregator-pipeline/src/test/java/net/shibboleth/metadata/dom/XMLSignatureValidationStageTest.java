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

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.AssertSupport;
import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.WarningStatus;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import edu.vt.middleware.crypt.CryptException;
import edu.vt.middleware.crypt.util.CryptReader;

/** Unit test for {@link XMLSchemaValidationStage}. */
public class XMLSignatureValidationStageTest extends BaseDomTest {
    
    @BeforeClass
    private void init() {
        setTestingClass(XMLSignatureValidationStage.class);
    }
    
    private Certificate getSigningCertificate() throws CryptException, IOException {
        return CryptReader.readCertificate(XMLSignatureSigningStageTest.class
                .getResourceAsStream(classRelativeResource("signingCert.pem")));
    }

    /**
     * Tests verifying a file with a valid signature.
     */
    @Test
    public void testValidSignature() throws Exception {
        Element testInput = readXmlData("signed.xml");

        final List<DomElementItem> mdCol = new ArrayList<>();
        mdCol.add(new DomElementItem(testInput));

        Certificate signingCert = getSigningCertificate();

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setVerificationCertificate(signingCert);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DomElementItem result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, XMLSignatureValidationStage.class, "test");
        
        // There should not have been any errors.
        final List<ErrorStatus> errors = result.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 0);

        // There should not have been any warnings either.
        final List<WarningStatus> warnings = result.getItemMetadata().get(WarningStatus.class);
        Assert.assertEquals(warnings.size(), 0);
    }

    /**
     * Test that a metadata element with an invalid signature is labelled with an error.
     */
    @Test
    public void testInvalidSignature() throws Exception {
        Element testInput = readXmlData("badSignature.xml");

        DomElementItem item = new DomElementItem(testInput);
        final List<DomElementItem> mdCol = new ArrayList<>();
        mdCol.add(item);

        Certificate signingCert = getSigningCertificate();

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setVerificationCertificate(signingCert);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertTrue(item.getItemMetadata().containsKey(ErrorStatus.class));
    }

    /**
     * Test that metadata elements that do not contain signature are appropriately labelled when valid signatures
     * are required.
     */
    @Test
    public void testRequiredSignature() throws Exception {
        Element testInput = readXmlData("entities2.xml");

        DomElementItem item = new DomElementItem(testInput);
        
        final List<DomElementItem> mdCol = new ArrayList<>();
        mdCol.add(item);

        Certificate signingCert = getSigningCertificate();

        XMLSignatureValidationStage stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setSignatureRequired(false);
        stage.setVerificationCertificate(signingCert);
        stage.initialize();

        stage.execute(mdCol);
        Assert.assertEquals(mdCol.size(), 1);

        DomElementItem result = mdCol.iterator().next();
        AssertSupport.assertValidComponentInfo(result, 1, XMLSignatureValidationStage.class, "test");

        item = new DomElementItem(testInput);
        
        mdCol.clear();
        mdCol.add(item);
        
        stage = new XMLSignatureValidationStage();
        stage.setId("test");
        stage.setSignatureRequired(true);
        stage.setVerificationCertificate(signingCert);
        stage.initialize();
        stage.execute(mdCol);
        
        Assert.assertTrue(item.getItemMetadata().containsKey(ErrorStatus.class));
    }
}
