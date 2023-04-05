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

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.shared.logic.Constraint;

/**
 * Validator class to check RSA public exponent values in X.509 certificates.
 * 
 * An instance of the class can be configured to have both a warning boundary and an
 * error boundary. The default is to give an error for any exponent less than or equal to
 * three, with no provision for warnings.
 * 
 * This NIST recommendation is for at least 65537 (2**16+1) but it's not obvious where
 * this came from so doesn't seem worth insisting on by default.
 *
 * @since 0.9.0
 */
@ThreadSafe
public class X509RSAExponentValidator extends AbstractX509Validator {

    /** The RSA public exponent value below which an error should result. Default: 5. */
    @Nonnull @GuardedBy("this")
    private BigInteger errorBoundary = bigInteger(5);
    
    /** The RSA public exponent value below which a warning should result. Default: 0 (disabled). */
    @Nonnull @GuardedBy("this")
    private BigInteger warningBoundary = bigInteger(0);

    /**
     * Private method to wrap construction of {@link BigInteger} literals.
     *
     * @param value value to be converted to {@link BigInteger}
     * @return the converted {@link BigInteger}
     */
    private static final @Nonnull BigInteger bigInteger(final long value) {
        final var bi = BigInteger.valueOf(value);
        assert bi != null;
        return bi;
    }

    /**
     * Get the RSA public exponent below which an error will result.
     * 
     * @return the RSA public exponent below which an error will result.
     */
    public final synchronized BigInteger getErrorBoundary() {
        return errorBoundary;
    }
    
    /**
     * Set the RSA public exponent below which an error should result.
     * 
     * @param length the RSA public exponent below which an error should result
     */
    public synchronized void setErrorBoundary(@Nonnull final BigInteger length) {
        Constraint.isGreaterThanOrEqual(0, length.compareTo(bigInteger(0)), "boundary value must not be negative");
        errorBoundary = length;
    }

    /**
     * Set the RSA public exponent below which an error should result.
     * 
     * @param length the RSA public exponent below which an error should result
     */
    public void setErrorBoundary(final long length) {
        setErrorBoundary(bigInteger(length));
    }
    
    /**
     * Get the RSA public exponent below which a warning will result.
     * 
     * @return the RSA public exponent below which a warning will result.
     */
    public final synchronized BigInteger getWarningBoundary() {
        return warningBoundary;
    }
    
    /**
     * Set the RSA public exponent below which a warning should result.
     * 
     * @param length the RSA public exponent below which a warning should result
     */
    public synchronized void setWarningBoundary(@Nonnull final BigInteger length) {
        Constraint.isGreaterThanOrEqual(0, length.compareTo(BigInteger.ZERO), "boundary value must not be negative");
        warningBoundary = length;
    }

    /**
     * Set the RSA public exponent below which a warning should result.
     * 
     * @param length the RSA public exponent below which a warning should result
     */
    public synchronized void setWarningBoundary(final long length) {
        setWarningBoundary(bigInteger(length));
    }
    
    @Override
    public void doValidate(@Nonnull final X509Certificate cert, @Nonnull final Item<?> item,
            @Nonnull final String stageId) {
        final PublicKey key = cert.getPublicKey();
        if ("RSA".equals(key.getAlgorithm())) {
            final RSAPublicKey rsaKey = (RSAPublicKey) key;
            final BigInteger exponent = rsaKey.getPublicExponent();
            if (!exponent.testBit(0)) {
                addError("RSA public exponent of " + exponent + " must be odd", item, stageId);
            } else if (exponent.compareTo(getErrorBoundary()) < 0) {
                addError("RSA public exponent of " + exponent + " is less than required " + getErrorBoundary(),
                        item, stageId);
            } else if (exponent.compareTo(getWarningBoundary()) < 0) {
                addWarning("RSA public exponent of " + exponent + " is less than recommended " + getWarningBoundary(),
                        item, stageId);
            }
        }
    }

}
