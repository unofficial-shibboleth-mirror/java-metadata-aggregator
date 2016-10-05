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


package net.shibboleth.metadata.validate.x509;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.springframework.core.io.Resource;
import org.testng.Assert;

import net.shibboleth.metadata.BaseTest;
import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;

public abstract class BaseX509ValidatorTest extends BaseTest {
    
    private CertificateFactory factory;

    public BaseX509ValidatorTest(final Class<?> clazz) throws Exception {
        super(clazz);
        factory = CertificateFactory.getInstance("X.509");
    }

    protected X509Certificate getCertificate(final String id) throws Exception {
        final Resource certResource = getClasspathResource(id);
        final X509Certificate cert =
                (X509Certificate) factory.generateCertificate(certResource.getInputStream());
        return cert;
    }

    protected void errorsAndWarnings(final Item<?> item,
            final int expectedErrors, final int expectedWarnings) {
        final Collection<ErrorStatus> errors = item.getItemMetadata().get(ErrorStatus.class);
        final Collection<WarningStatus> warnings = item.getItemMetadata().get(WarningStatus.class);
        //System.out.println("Errors and warnings:");
        //for (ErrorStatus err: errors) {
        //    System.out.println("Error: " + err.getComponentId() + ": " + err.getStatusMessage());
        //}
        //for (WarningStatus warn: warnings) {
        //    System.out.println("Warning: " + warn.getComponentId() + ": " + warn.getStatusMessage());
        //}
        Assert.assertEquals(errors.size(), expectedErrors, "wrong number of errors");
        Assert.assertEquals(warnings.size(), expectedWarnings, "wrong number of warnings");
    }

}
