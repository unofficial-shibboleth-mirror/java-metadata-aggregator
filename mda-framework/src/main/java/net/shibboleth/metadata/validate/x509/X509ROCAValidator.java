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

package net.shibboleth.metadata.validate.x509;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;

/**
 * Validator class to check that X.509 certificate public keys are not
 * affected by the Return of Coppersmith Attack (ROCA, CVE-2017-15361).
 *
 * @since 0.10.0
 *
 * @see <a href="https://crocs.fi.muni.cz/public/papers/rsa_ccs17">ROCA paper</a>
 */
@ThreadSafe
public class X509ROCAValidator extends AbstractX509Validator {

    /**
     * Checks to see whether a given modulus represents a key affected by the attack.
     *
     * @see <a href="https://github.com/crocs-muni/roca/blob/master/java/BrokenKey.java">original source code
     *  (dual MIT and Apache 2 licensed)</a>
     */
    @Immutable
    private static class BrokenKey {

        // Checkstyle: JavadocVariable|JavadocMethod|ConstantName OFF

        private static final int[] prims = new int[] {
            3, 5, 7, 11, 13, 17, 19, 23, 29, 31,
            37, 41, 43, 47, 53, 59, 61, 67, 71, 73,
            79, 83, 89, 97, 101, 103, 107, 109, 113, 127,
            131, 137, 139, 149, 151, 157, 163, 167,
        };

        private static final BigInteger[] primes = new BigInteger[prims.length];

        static {
            for (int i = 0; i < prims.length; i++) {
                primes[i] = BigInteger.valueOf(prims[i]);
            }
        }

        private static final BigInteger[] markers = new BigInteger[] {
            new BigInteger("6"),
            new BigInteger("30"),
            new BigInteger("126"),
            new BigInteger("1026"),
            new BigInteger("5658"),
            new BigInteger("107286"),
            new BigInteger("199410"),
            new BigInteger("8388606"),
            new BigInteger("536870910"),
            new BigInteger("2147483646"),
            new BigInteger("67109890"),
            new BigInteger("2199023255550"),
            new BigInteger("8796093022206"),
            new BigInteger("140737488355326"),
            new BigInteger("5310023542746834"),
            new BigInteger("576460752303423486"),
            new BigInteger("1455791217086302986"),
            new BigInteger("147573952589676412926"),
            new BigInteger("20052041432995567486"),
            new BigInteger("6041388139249378920330"),
            new BigInteger("207530445072488465666"),
            new BigInteger("9671406556917033397649406"),
            new BigInteger("618970019642690137449562110"),
            new BigInteger("79228162521181866724264247298"),
            new BigInteger("2535301200456458802993406410750"),
            new BigInteger("1760368345969468176824550810518"),
            new BigInteger("50079290986288516948354744811034"),
            new BigInteger("473022961816146413042658758988474"),
            new BigInteger("10384593717069655257060992658440190"),
            new BigInteger("144390480366845522447407333004847678774"),
            new BigInteger("2722258935367507707706996859454145691646"),
            new BigInteger("174224571863520493293247799005065324265470"),
            new BigInteger("696898287454081973172991196020261297061886"),
            new BigInteger("713623846352979940529142984724747568191373310"),
            new BigInteger("1800793591454480341970779146165214289059119882"),
            new BigInteger("126304807362733370595828809000324029340048915994"),
            new BigInteger("11692013098647223345629478661730264157247460343806"),
            new BigInteger("187072209578355573530071658587684226515959365500926"),
        };

        public static boolean isAffected(@Nonnull final BigInteger modulus) {
            for (int i = 0; i < primes.length; i++) {
                if (BigInteger.ONE.shiftLeft(modulus.remainder(primes[i]).intValue()).and(markers[i]).
                        equals(BigInteger.ZERO)) {
                    return false;
                }
            }
            return true;
        }

        // Checkstyle: JavadocVariable|JavadocMethod|ConstantName ON

    }

    @Override
    public void doValidate(@Nonnull final X509Certificate cert, @Nonnull final Item<?> item,
            @Nonnull final String stageId) {
        final PublicKey key = cert.getPublicKey();
        if ("RSA".equals(key.getAlgorithm())) {
            final RSAPublicKey rsaKey = (RSAPublicKey) key;
            final var modulus = rsaKey.getModulus();
            assert modulus != null;
            if (BrokenKey.isAffected(modulus)) {
                addError("RSA public key is vulnerable to ROCA", item, stageId);
            }
        }
    }

}
