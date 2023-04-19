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

package net.shibboleth.metadata.validate.net;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.net.InternetDomainName;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.validate.BaseValidator;
import net.shibboleth.metadata.validate.Validator;

/**
 * A validator that checks whether an {@link InternetDomainName} is under a public suffix.
 *
 * <p>
 * A domain name which is <em>not</em> under a public suffix might be a public suffix itself,
 * or might terminate in something which is not a public suffix.
 * </p>
 *
 * @since 0.10.0
 */
@ThreadSafe
public class RejectDomainNameNotUnderPublicSuffixValidator extends BaseValidator
    implements Validator<InternetDomainName> {

    @Override
    public @Nonnull Action validate(@Nonnull final InternetDomainName domain, @Nonnull final Item<?> item,
            @Nonnull final String stageId) {
        if (domain.isUnderPublicSuffix()) {
            return Action.CONTINUE;
        } else {
            addErrorMessage(domain, item, stageId);
            return Action.DONE;
        }
    }

}
