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

import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * An abstract entity attribute matcher implementation that matches an exact
 * combination of value, name and name format. Optionally, a registration
 * authority value may also be matched against.
 *
 * @since 0.9.0
 */
@ThreadSafe
public abstract class AbstractExactValueMatcher extends AbstractEntityAttributeMatcher {

    /** The attribute value to match. */
    private final String value;

    /** The attribute <code>Name</code> to match. */
    private final String name;
    
    /** The attribute <code>NameFormat</code> to match. */
    private final String nameFormat;
    
    /** Registration authority to match against, or <code>null</code>. */
    @Nullable
    private final String registrationAuthority;

    /**
     * Constructor.
     * 
     * @param matchValue attribute value to match
     * @param matchName attribute name to match
     * @param matchNameFormat attribute name format to match
     * @param matchRegAuth entity registration authority to match, or <code>null</code>
     */
    public AbstractExactValueMatcher(@Nonnull final String matchValue,
            @Nonnull final String matchName, @Nonnull final String matchNameFormat,
            @Nullable final String matchRegAuth) {
        super();
        value = Constraint.isNotNull(matchValue, "value may not be null");
        name = Constraint.isNotNull(matchName, "name may not be null");
        nameFormat = Constraint.isNotNull(matchNameFormat, "name format may not be null");
        registrationAuthority = matchRegAuth;
    }

    @Override
    protected boolean matchAttributeValue(@Nonnull final String inputValue) {
        return value.equals(inputValue);
    }

    @Override
    protected boolean matchAttributeName(@Nonnull final String inputName) {
         return name.equals(inputName);
    }

    @Override
    protected boolean matchAttributeNameFormat(@Nonnull final String inputNameFormat) {
        return nameFormat.equals(inputNameFormat);
    }

    @Override
    protected boolean matchRegistrationAuthority(@Nullable final String inputRegistrationAuthority) {
        if (registrationAuthority == null) {
            // ignore the context's registration authority value
            return true;
        } else {
            return registrationAuthority.equals(inputRegistrationAuthority);
        }
    }

}
