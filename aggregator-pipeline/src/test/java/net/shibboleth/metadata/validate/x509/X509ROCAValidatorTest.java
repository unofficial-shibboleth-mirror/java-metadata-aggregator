
package net.shibboleth.metadata.validate.x509;

import java.security.cert.X509Certificate;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.validate.Validator;

import org.testng.Assert;
import org.testng.annotations.Test;

public class X509ROCAValidatorTest extends BaseX509ValidatorTest {

    /** Constructor sets class under test. */
    public X509ROCAValidatorTest() throws Exception {
        super(X509ROCAValidator.class);
    }

    private void testCert(final String certName,
            final Validator<X509Certificate> val,
            final int expectedErrors, final int expectedWarnings) throws Exception {
        final Item<String> item = new MockItem("foo");
        final X509Certificate cert = getCertificate(certName);
        Assert.assertEquals(val.validate(cert, item, "stage"), Validator.Action.CONTINUE);
        errorsAndWarnings(item, expectedErrors, expectedWarnings);
    }

    @Test
    public void test() throws Exception {
        final X509ROCAValidator val = new X509ROCAValidator();
        testCert("cert01.pem", val, 0, 0);
        testCert("cert02.pem", val, 0, 0);
        testCert("cert03.pem", val, 0, 0);
        testCert("cert04.pem", val, 1, 0);
        testCert("cert05.pem", val, 1, 0);
        testCert("cert06.pem", val, 0, 0);
    }

}
