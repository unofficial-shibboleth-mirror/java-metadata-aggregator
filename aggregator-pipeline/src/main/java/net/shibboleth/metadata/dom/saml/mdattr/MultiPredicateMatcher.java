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

import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * An entity attribute matcher implementation that delegates each
 * component match to a different @{link Predicate}. Each such
 * {@link Predicate} defaults to one returning <code>true</code> so that
 * in most cases a minimum number of properties need to be set.
 * 
 * The individual {@link Predicate}s operate over {@link CharSequence}
 * rather than {@link String} for generality.
 */
@ThreadSafe
public class MultiPredicateMatcher extends AbstractEntityAttributeMatcher {

    /** {@link Predicate} to use to match the context's attribute value. */
    @Nonnull
    private Predicate<CharSequence> valuePredicate = x -> true;
    
    /** {@link Predicate} to use to match the context's attribute name. */
    @Nonnull
    private Predicate<CharSequence> namePredicate = x -> true;
    
    /** {@link Predicate} to use to match the context's attribute name format. */
    @Nonnull
    private Predicate<CharSequence> nameFormatPredicate = x -> true;
    
    /** {@link Predicate} to use to match the context's registration authority. */
    @Nonnull
    private Predicate<CharSequence> registrationAuthorityPredicate = x -> true;
    
    /**
     * Gets the {@link Predicate} being used to match the context's attribute value.
     * 
     * @return the {@link Predicate} being used to match the context's attribute value
     */
    @Nonnull
    public Predicate<CharSequence> getValuePredicate() {
        return valuePredicate;
    }
    
    /**
     * Sets the {@link Predicate} to use to match the context's attribute value.
     * 
     * @param predicate new {@link Predicate} to use to match the context's attribute value
     */
    public void setValuePredicate(@Nonnull final Predicate<CharSequence> predicate) {
        valuePredicate = Constraint.isNotNull(predicate, "value predicate may not be null");
    }
    
    /**
     * Gets the {@link Predicate} being used to match the context's attribute name.
     * 
     * @return the {@link Predicate} being used to match the context's attribute name
     */
    @Nonnull
    public Predicate<CharSequence> getNamePredicate() {
        return namePredicate;
    }
    
    /**
     * Sets the {@link Predicate} to use to match the context's attribute name.
     * 
     * @param predicate new {@link Predicate} to use to match the context's attribute name
     */
    public void setNamePredicate(@Nonnull final Predicate<CharSequence> predicate) {
        namePredicate = Constraint.isNotNull(predicate, "name predicate may not be null");
    }
    
    /**
     * Gets the {@link Predicate} being used to match the context's attribute name format.
     * 
     * @return the {@link Predicate} being used to match the context's attribute name format
     */
    @Nonnull
    public Predicate<CharSequence> getNameFormatPredicate() {
        return nameFormatPredicate;
    }
    
    /**
     * Sets the {@link Predicate} to use to match the context's attribute name format.
     * 
     * @param predicate new {@link Predicate} to use to match the context's attribute name format
     */
    public void setNameFormatPredicate(@Nonnull final Predicate<CharSequence> predicate) {
        nameFormatPredicate = Constraint.isNotNull(predicate, "name format predicate may not be null");
    }
    
    /**
     * Gets the {@link Predicate} being used to match the context's registration authority.
     * 
     * @return the {@link Predicate} being used to match the context's registration authority
     */
    @Nonnull
    public Predicate<CharSequence> getRegistrationAuthorityPredicate() {
        return valuePredicate;
    }
    
    /**
     * Sets the {@link Predicate} to use to match the context's registration authority.
     * 
     * @param predicate new {@link Predicate} to use to match the context's registration authority
     */
    public void setRegistrationAuthorityPredicate(@Nonnull final Predicate<CharSequence> predicate) {
        registrationAuthorityPredicate = Constraint.isNotNull(predicate,
                "registration authority predicate may not be null");
    }
    
    @Override
    protected boolean matchAttributeValue(@Nonnull final String inputValue) {
        return valuePredicate.test(inputValue);
    }

    @Override
    protected boolean matchAttributeName(@Nonnull final String inputName) {
         return namePredicate.test(inputName);
    }

    @Override
    protected boolean matchAttributeNameFormat(@Nonnull final String inputNameFormat) {
        return nameFormatPredicate.test(inputNameFormat);
    }

    @Override
    protected boolean matchRegistrationAuthority(@Nullable final String inputRegistrationAuthority) {
        return registrationAuthorityPredicate.test(inputRegistrationAuthority);
    }

}
