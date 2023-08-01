/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package net.shibboleth.metadata.validate.x509.testing;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.springframework.core.io.Resource;
import org.testng.Assert;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.testing.BaseTest;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.metadata.validate.Validator;

public abstract class BaseX509ValidatorTest extends BaseTest {
    
    private @Nonnull CertificateFactory factory;

    public BaseX509ValidatorTest(final @Nonnull Class<?> clazz) throws Exception {
        super(clazz);
        var fac = CertificateFactory.getInstance("X.509");
        assert fac != null;
        factory = fac;
    }

    protected @Nonnull X509Certificate getCertificate(final String id) throws Exception {
        final Resource certResource = getClasspathResource(id);
        final X509Certificate cert =
                (X509Certificate) factory.generateCertificate(certResource.getInputStream());
        assert cert != null;
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

    protected void testCert(final String certName,
            final Validator<X509Certificate> val,
            final int expectedErrors, final int expectedWarnings) throws Exception {
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate(certName);
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, expectedErrors, expectedWarnings);
    }

}
