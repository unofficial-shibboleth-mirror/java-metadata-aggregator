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

import javax.annotation.Nonnull;

import com.google.common.net.InternetDomainName;

import net.shibboleth.metadata.validate.BaseAsValidator;
import net.shibboleth.metadata.validate.Validator;

/**
 * A <code>Validator</code> that checks {@link String} values as domain names by converting the
 * value to an {@link InternetDomainName} and applying a sequence of validators to that value.
 *
 * <p>
 * This validator fails (and returns {@link net.shibboleth.metadata.validate.Validator.Action#DONE}) if the
 * value can not be converted to an {@link InternetDomainName}.
 * </p>
 *
 * <p>
 * Otherwise, the validator applies the sequence of validators to the {@link InternetDomainName} and returns
 * the value of that sequence.
 * </p>
 *
 * @since 0.10.0
 */
public class AsDomainNameStringValidator extends BaseAsValidator<String, InternetDomainName>
    implements Validator<String> {

    @Override
    protected @Nonnull InternetDomainName convert(@Nonnull final String domain) throws IllegalArgumentException {
        final var result = InternetDomainName.from(domain);
        assert result != null;
        return result;
    }

}
