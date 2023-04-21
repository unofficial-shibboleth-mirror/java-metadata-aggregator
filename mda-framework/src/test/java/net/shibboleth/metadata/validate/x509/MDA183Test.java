
package net.shibboleth.metadata.validate.x509;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.codec.binary.Hex;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.testing.BaseTest;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.metadata.validate.Validator;

public class MDA183Test extends BaseTest {

    /** Sequence of bytes put on the front of the string to be hashed. */
    private final byte[] openSSLprefix = {
            'M', 'o', 'd', 'u', 'l', 'u', 's', '=',
    };

    /**
     * Computes the OpenSSL digest value for the given modulus.
     *
     * <p>
     * Not used in the test, but left here for use in generating new tests.
     * </p>
     *
     * @param modulus RSA public modulus to be digested
     * @return value to be compared against the blacklist
     * @throws StageProcessingException if SHA1 digester can not be acquired, or for internal
     *      errors related to {@link ByteArrayOutputStream}
     */
    @SuppressWarnings("unused")
    private @Nonnull String openSSLDigest(@Nonnull final BigInteger modulus) throws StageProcessingException {
        try {
            // Acquire a representation of the modulus
            byte[] modulusBytes = modulus.toByteArray();
            if (modulusBytes[0] == 0) {
                // drop first 00 byte of modulus representation
                modulusBytes = Arrays.copyOfRange(modulusBytes, 1, modulusBytes.length);
            }

            // Encode the modulus into upper-case hex characters
            final char[] encodedModulus = Hex.encodeHex(modulusBytes,  false);

            // Now construct the thing we want to hash
            final ByteArrayOutputStream bb = new ByteArrayOutputStream();
            try {
                bb.write(openSSLprefix);
                for (final char c : encodedModulus) {
                    bb.write((byte) c);
                }
                bb.write('\n');
            } catch (final IOException e) {
                throw new StageProcessingException("internal error writing to ByteArrayStream", e);
            }
            //System.out.println("To be digested: " + bb.toString());

            // Make the digest
            final MessageDigest digest = MessageDigest.getInstance("SHA1");
            digest.update(bb.toByteArray());
            final byte[] bytes = digest.digest();

            // Convert the digest to a lower-case hex string
            final char [] encodedDigest = Hex.encodeHex(bytes, true);
            final String strValue = String.valueOf(encodedDigest);
            final String trimmed = strValue.substring(20);
            assert trimmed != null;
            //System.out.println("Digest: " + strValue + " trimmed " + trimmed);
            return trimmed;
        } catch (final NoSuchAlgorithmException e) {
            throw new StageProcessingException("could not create message digester", e);
        }
    }

    protected MDA183Test() {
        super(MDA183Test.class);
    }

    private Validator<X509Certificate> getValidator(final int keySize) throws Exception {
        // pick up the appropriate keylist resource
        final @Nonnull Resource keylistResource;
        switch (keySize) {
        case 1024:
            keylistResource = new ClassPathResource("net/shibboleth/metadata/keylists/rsa/legacy/compromised-1024.txt");
            break;
        case 2048:
            keylistResource = new ClassPathResource("net/shibboleth/metadata/keylists/rsa/compromised-2048.txt");
            break;
        default:
            throw new IllegalArgumentException();
        }

        // create a validator
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setId("validator-" + keySize);
        val.setBlacklistResource(keylistResource);
        val.setKeySize(keySize);
        val.initialize();

        return val;
    }

    private void verifyKey(final KeyStore ks, final @Nonnull String alias,
            final String password,
            final Validator<X509Certificate> val,
            int expectedKeySize) throws Exception {
        final Key key = ks.getKey(alias, password.toCharArray());
        Assert.assertTrue(key instanceof RSAPrivateKey);
        final RSAPrivateKey rKey = (RSAPrivateKey)key;
        Assert.assertEquals(rKey.getModulus().bitLength(), expectedKeySize);
        // The following two lines can be used to extract an appropriate digest value
        // for new keys, along with the size of the modulus.
        //System.out.println("alias " + alias + " key size " + rKey.getModulus().bitLength());
        //System.out.println("hash " + openSSLDigest(rKey.getModulus()));
        final Certificate cert = ks.getCertificate(alias);
        assert cert != null;
        final Item<String> item = new MockItem("mock");
        val.validate((X509Certificate)cert, item, alias);
        final List<ErrorStatus> errors = item.getItemMetadata().get(ErrorStatus.class);
        if (errors.size() == 0) {
            Assert.fail("certificate " + alias + " was not matched");
        }
    }

    @Test
    public void testMDA183jetty() throws Exception {
        final Validator<X509Certificate> validator1024 = getValidator(1024);
        final Validator<X509Certificate> validator2048 = getValidator(2048);

        // grab the keystore
        final Resource keystoreResource = getClasspathResource("keystore.jks");
        final KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(keystoreResource.getInputStream(), "storepwd".toCharArray());

        // verify the keys
        verifyKey(ks, "jetty", "keypwd", validator2048, 2048);
        verifyKey(ks, "mykey", "keypwd", validator1024, 1024);
    }

}
