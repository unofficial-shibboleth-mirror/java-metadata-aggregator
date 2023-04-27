
package net.shibboleth.metadata.validate.x509;

import org.testng.annotations.Test;

import net.shibboleth.metadata.validate.x509.testing.BaseX509ValidatorTest;

public class X509ROCAValidatorTest extends BaseX509ValidatorTest {

    public X509ROCAValidatorTest() throws Exception {
        super(X509ROCAValidator.class);
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

    @Test
    public void testDSA() throws Exception {
        final var val = new X509ROCAValidator();
        val.setId("test");
        val.initialize();
        testCert("dsa.pem", val, 0, 0);
    }

}
