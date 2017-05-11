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

package net.shibboleth.metadata.dom.saml.mdattr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An entity attribute matcher which matches a given assurance certification value.
 */
@ThreadSafe
public class AssuranceCertificationMatcher extends AbstractExactValueMatcher {

    /** Attribute Name value for assurance certifications. */
    private static final String AC_NAME = "urn:oasis:names:tc:SAML:attribute:assurance-certification";

    /** Attribute NameFormat value for assurance certifications. */
    private static final String AC_NAME_FORMAT = "urn:oasis:names:tc:SAML:2.0:attrname-format:uri";

    /**
     * Constructor.
     *
     * @param certification assurance certification to match
     * @param regAuth registration authority to match, or <code>null</code>
     */
    public AssuranceCertificationMatcher(@Nonnull final String certification, @Nullable final String regAuth) {
        super(certification, AC_NAME, AC_NAME_FORMAT, regAuth);
    }

    /**
     * Constructor.
     *
     * @param certification assurance certification to match
     */
    public AssuranceCertificationMatcher(@Nonnull final String certification) {
        this(certification, null);
    }

}
