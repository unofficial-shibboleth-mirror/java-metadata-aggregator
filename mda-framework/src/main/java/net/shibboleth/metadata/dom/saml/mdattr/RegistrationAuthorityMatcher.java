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

import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.EntityAttributeContext;

/**
 * An entity attribute matcher which matches a given registration authority.
 * 
 * It can match against a specific registration authority, or against the absence of
 * one.
 *
 * @since 0.9.0
 */
@Immutable
public class RegistrationAuthorityMatcher implements Predicate<EntityAttributeContext> {
    
    /** Registration authority to match against. */
    @Nullable
    private final String registrationAuthority;
    
    /**
     * Constructor.
     * 
     * @param regAuth registration authority to match, or <code>null</code>
     */
    public RegistrationAuthorityMatcher(@Nullable final String regAuth) {
        registrationAuthority = regAuth;
    }

    @Override
    public boolean test(final EntityAttributeContext input) {
        if (registrationAuthority == null) {
            // match entities *without* a registration authority
            return null == input.getRegistrationAuthority();
        }
        assert registrationAuthority != null;
        return registrationAuthority.equals(input.getRegistrationAuthority());
    }

}
