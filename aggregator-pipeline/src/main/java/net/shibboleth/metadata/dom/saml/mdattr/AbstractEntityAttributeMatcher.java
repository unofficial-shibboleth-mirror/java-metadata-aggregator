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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.EntityAttributeContext;

/**
 * Abstract implementation of {@link Predicate} over {@link EntityAttributeContext} using
 * the template method pattern. The {@link #test} method is broken down into matches
 * against the four components of the {@link EntityAttributeContext}. All four sub-matches
 * must succeed for the {@link Predicate} to be <code>true</code>
 * 
 * Where an implementation wishes to ignore a component (most commonly,
 * {@link EntityAttributeContext#getRegistrationAuthority}) it can simply return <code>true</code>
 * in that template method.
 *
 * @since 0.9.0
 */
@ThreadSafe
public abstract class AbstractEntityAttributeMatcher implements Predicate<EntityAttributeContext> {

    /**
     * Match the attribute value component of the {@link EntityAttributeContext}.
     * 
     * @param inputValue value component of the input context
     * @return <code>true</code> if and only if the value component matches
     */
    protected abstract boolean matchAttributeValue(@Nonnull final String inputValue);

    /**
     * Match the name component of the {@link EntityAttributeContext}.
     * 
     * @param inputName name component of the input context
     * @return <code>true</code> if and only if the name component matches
     */
    protected abstract boolean matchAttributeName(@Nonnull final String inputName);

    /**
     * Match the name format component of the {@link EntityAttributeContext}.
     * 
     * @param inputNameFormat name format component of the input context
     * @return <code>true</code> if and only if the name format component matches
     */
    protected abstract boolean matchAttributeNameFormat(@Nonnull final String inputNameFormat);

    /**
     * Match the registration authority component of the {@link EntityAttributeContext}.
     * 
     * @param inputRegistrationAuthority registration authority component of the input context
     * @return <code>true</code> if and only if the registration authority component matches
     */
    protected abstract boolean matchRegistrationAuthority(@Nullable final String inputRegistrationAuthority);

    @Override
    public boolean test(@Nonnull final EntityAttributeContext input) {
        return matchRegistrationAuthority(input.getRegistrationAuthority()) &&
                matchAttributeNameFormat(input.getNameFormat()) &&
                matchAttributeName(input.getName()) &&
                matchAttributeValue(input.getValue());
    }

}
