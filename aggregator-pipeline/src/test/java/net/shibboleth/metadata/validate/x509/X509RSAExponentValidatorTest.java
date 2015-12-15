
package net.shibboleth.metadata.validate.x509;

import java.security.cert.X509Certificate;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.validate.Validator;

import org.testng.Assert;
import org.testng.annotations.Test;

public class X509RSAExponentValidatorTest extends BaseX509ValidatorTest {
    
    /** Constructor sets class under test. */
    public X509RSAExponentValidatorTest() throws Exception {
        super(X509RSAExponentValidator.class);
    }

    private void testCert(final String certName,
            final Validator<X509Certificate> val,
            final int expectedErrors, final int expectedWarnings) throws Exception {
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate(certName);
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, expectedErrors, expectedWarnings);
    }

    private void testThreeCerts(final Validator<X509Certificate> val,
            final int expectedErrors3, final int expectedWarnings3,
            final int expectedErrors35, final int expectedWarnings35,
            final int expectedErrors65537, final int expectedWarnings65537) throws Exception {
        testCert("3.pem", val, expectedErrors3, expectedWarnings3); // exponent == 3
        testCert("35.pem", val, expectedErrors35, expectedWarnings35); // exponent == 35
        testCert("65537.pem", val, expectedErrors65537, expectedWarnings65537); // exponent == 65537
    }
    
    @Test
    public void testDefaults() throws Exception {
        final X509RSAExponentValidator val = new X509RSAExponentValidator();
        testThreeCerts(val, 1, 0, 0, 0, 0, 0);
    }

    @Test
    public void testNISTWarning() throws Exception {
        final X509RSAExponentValidator val = new X509RSAExponentValidator();
        val.setWarningBoundary(65537);
        testThreeCerts(val, 1, 0, 0, 1, 0, 0);
    }

    @Test
    public void testNISTError() throws Exception {
        final X509RSAExponentValidator val = new X509RSAExponentValidator();
        val.setErrorBoundary(65537);
        testThreeCerts(val, 1, 0, 1, 0, 0, 0);
    }

    @Test
    public void testWarningOnly() throws Exception {
        final X509RSAExponentValidator val = new X509RSAExponentValidator();
        val.setErrorBoundary(0);
        val.setWarningBoundary(65537);
        testThreeCerts(val, 0, 1, 0, 1, 0, 0);
    }

}
