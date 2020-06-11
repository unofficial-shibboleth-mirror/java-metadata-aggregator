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

package net.shibboleth.metadata.validate.string;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import net.shibboleth.metadata.validate.BaseValidator;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * A base class for <code>Validator</code>s that match {@link String} values against a regular expression.
 */
public abstract class BaseStringRegexValidator extends BaseValidator {

    /** Regular expression to be accepted by this validator. */
    @NonnullAfterInit
    private String regex;

    /** Compiled regular expression to use in match operations. */
    @NonnullAfterInit
    private Pattern pattern;

    /**
     * Returns the regular expression.
     *
     * @return Returns the regular expression.
     */
    @NonnullAfterInit
    public String getRegex() {
        return regex;
    }

    /**
     * Sets the regular expression to be accepted.
     *
     * @param r the regular expression to set.
     */
    public void setRegex(@Nonnull final String r) {
        throwSetterPreconditionExceptions();
        regex = r;
    }

    /**
     * Get the compiled regular expression for use in matching.
     *
     * @return the compiled {@link Pattern}
     */
    protected Pattern getPattern() {
        return pattern;
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (getRegex() == null) {
            throw new ComponentInitializationException("regular expression to be matched can not be null");
        }

        pattern = Pattern.compile(regex);
    }

}
