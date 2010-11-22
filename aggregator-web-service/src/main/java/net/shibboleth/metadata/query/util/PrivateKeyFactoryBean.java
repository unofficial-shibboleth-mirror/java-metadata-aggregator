/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.query.util;

import java.security.PrivateKey;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opensaml.util.resource.Resource;
import org.springframework.beans.factory.FactoryBean;

import edu.vt.middleware.crypt.util.CryptReader;

/** Spring bean factory for producing a {@link PrivateKey} from a {@link Resource}. */
public class PrivateKeyFactoryBean implements FactoryBean<PrivateKey> {

    /** Resource providing the PEM encoded private key. */
    private Resource privKeyRes;

    /** Password for the private key. */
    private String privKeyPass;

    /**
     * Sets the resource providing the PEM encoded private key.
     * 
     * @param res resource providing the PEM encoded private key, never null
     */
    public void setPrivateKeyResource(final Resource res) {
        privKeyRes = res;
    }

    /**
     * Sets the password for the private key.
     * 
     * @param password password for the private key, may be null if key is not encrypted
     */
    public void setPrivateKeyPassword(final String password) {
        privKeyPass = password;
    }

    /** {@inheritDoc} */
    public PrivateKey getObject() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        if (privKeyPass == null) {
            return CryptReader.readPemPrivateKey(privKeyRes.getInputStream(), null);
        } else {
            return CryptReader.readPemPrivateKey(privKeyRes.getInputStream(), privKeyPass.toCharArray());
        }
    }

    /** {@inheritDoc} */
    public Class<?> getObjectType() {
        return PrivateKey.class;
    }

    /** {@inheritDoc} */
    public boolean isSingleton() {
        return true;
    }
}