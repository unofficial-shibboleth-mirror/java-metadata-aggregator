
package net.shibboleth.metadata.validate.x509;

import java.security.cert.X509Certificate;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.metadata.validate.Validator.Action;
import net.shibboleth.metadata.validate.x509.testing.BaseX509ValidatorTest;

public class X509DSADetectorTest extends BaseX509ValidatorTest {

    /**
     * Constructor sets class under test.
     * 
     * @throws Exception if something goes wrong
     */
    public X509DSADetectorTest() throws Exception {
        super(X509DSADetector.class);
    }

    /*
     * Test against an example DSA certificate with the default setting of the
     * error property.
     */
    @Test
    public void testDSA1error() throws Exception {
        final Item<String> item = new MockItem("foo");
        final X509DSADetector val = new X509DSADetector();
        val.setId("DSA");
        Assert.assertTrue(val.isError());
        Assert.assertSame(val.getAction(), Action.DONE);
        val.initialize();
        final X509Certificate cert = getCertificate("dsa1.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Action.DONE);
        errorsAndWarnings(item, 1, 0);
    }

    /*
     * Test against an example DSA certificate with the action property set to
     * CONTINUE.
     */
    @Test
    public void testDSA1continue() throws Exception {
        final Item<String> item = new MockItem("foo");
        final X509DSADetector val = new X509DSADetector();
        val.setId("DSA");
        val.setAction(Action.CONTINUE);
        val.initialize();
        final X509Certificate cert = getCertificate("dsa1.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Action.CONTINUE);
        errorsAndWarnings(item, 1, 0);
    }

    /*
     * Test against an example DSA certificate with the error property set to
     * false, so that a warning is used instead.
     */
    @Test
    public void testDSA1warning() throws Exception {
        final Item<String> item = new MockItem("foo");
        final X509DSADetector val = new X509DSADetector();
        val.setId("DSA");
        val.setError(false);
        val.initialize();
        final X509Certificate cert = getCertificate("dsa1.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Action.DONE);
        errorsAndWarnings(item, 0, 1);
    }

    /*
     * Test against an RSA certificate; it should be ignored, with a CONTINUE action.
     */
    @Test
    public void testRSA() throws Exception {
        final Item<String> item = new MockItem("foo");
        final X509DSADetector val = new X509DSADetector();
        val.setId("DSA");
        val.initialize();
        final X509Certificate cert = getCertificate("rsa.pem");
        Assert.assertEquals(val.validate(cert, item, "stage"), Action.CONTINUE);
        errorsAndWarnings(item, 0, 0);
    }

}
