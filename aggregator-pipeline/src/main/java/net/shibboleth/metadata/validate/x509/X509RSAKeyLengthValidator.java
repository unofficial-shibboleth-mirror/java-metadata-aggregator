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

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;

/**
 * Validator class to check RSA key lengths in X.509 certificates.
 * 
 * An instance of the class can be configured to have both a warning boundary and an
 * error boundary. The default is to give an error for any key smaller than 2048 bits,
 * with no provision for warnings. This seems the right long term default.
 * 
 * During the transition to 2048-bit keys, it may be appropriate to set the warning
 * boundary to 2048 bits and the error boundary to 1024 bits.
 */
@ThreadSafe
public class X509RSAKeyLengthValidator extends AbstractX509Validator {

    /** The RSA key length below which an error should result. Default: 2048. */
    private int errorBoundary = 2048;
    
    /** The RSA key length below which a warning should result. Default: 0 (disabled). */
    private int warningBoundary;
    
    /**
     * Constructor.
     */
    public X509RSAKeyLengthValidator() {
        super();
        setId("RSAKeyLength");
    }

    /**
     * Get the RSA key length below which an error will result.
     * 
     * @return the RSA key length below which an error will result.
     */
    public int getErrorBoundary() {
        return errorBoundary;
    }
    
    /**
     * Set the RSA key length below which an error should result.
     * 
     * @param length the RSA key length below which an error should result
     */
    public void setErrorBoundary(final int length) {
        errorBoundary = length;
    }
    
    /**
     * Get the RSA key length below which a warning will result.
     * 
     * @return the RSA key length below which a warning will result.
     */
    public int getWarningBoundary() {
        return warningBoundary;
    }
    
    /**
     * Set the RSA key length below which a warning should result.
     * 
     * @param length the RSA key length below which a warning should result
     */
    public void setWarningBoundary(final int length) {
        warningBoundary = length;
    }
    
    /** {@inheritDoc} */
    @Override
    public void doValidate(@Nonnull final X509Certificate cert, @Nonnull final Item<?> item,
            @Nonnull final String stageId) {
        final PublicKey key = cert.getPublicKey();
        if ("RSA".equals(key.getAlgorithm())) {
            final RSAPublicKey rsaKey = (RSAPublicKey) key;
            final int keyLen = rsaKey.getModulus().bitLength();
            if (keyLen < errorBoundary) {
                addError("RSA key length of " + keyLen + " bits is less than required " + errorBoundary,
                        item, stageId);
            } else if (keyLen < warningBoundary) {
                addWarning("RSA key length of " + keyLen + " bits is less than recommended " + warningBoundary,
                        item, stageId);
            }
        }
    }

}
