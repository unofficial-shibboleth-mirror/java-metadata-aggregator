
package net.shibboleth.metadata.validate.x509;

import java.security.cert.X509Certificate;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.validate.Validator;

import org.testng.annotations.Test;

public class X509RSAKeyLengthValidatorTest extends BaseX509ValidatorTest {
    
    /** Constructor sets class under test. */
    public X509RSAKeyLengthValidatorTest() throws Exception {
        super(X509RSAKeyLengthValidator.class);
    }

    @Test
    public void testDefaults2048() throws Exception {
        final Item<String> item = new MockItem("foo");
        final Validator<X509Certificate> val = new X509RSAKeyLengthValidator();
        final X509Certificate cert = getCertificate("2048.pem");
        val.validate(cert, item, "stage");
        errorsAndWarnings(item, 0, 0);
    }

    @Test
    public void testDefaults1024() throws Exception {
        final Item<String> item = new MockItem("foo");
        final Validator<X509Certificate> val = new X509RSAKeyLengthValidator();
        final X509Certificate cert = getCertificate("1024.pem");
        val.validate(cert, item, "stage");
        errorsAndWarnings(item, 1, 0);
    }

    @Test
    public void testWarningOn1024() throws Exception {
        final Item<String> item = new MockItem("foo");
        final X509RSAKeyLengthValidator val = new X509RSAKeyLengthValidator();
        val.setErrorBoundary(1024);
        val.setWarningBoundary(2048);
        final X509Certificate cert = getCertificate("1024.pem");
        val.validate(cert, item, "stage");
        errorsAndWarnings(item, 0, 1);
    }

}
