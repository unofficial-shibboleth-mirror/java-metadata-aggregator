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

import java.util.regex.Matcher;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.validate.Validator;

/**
 * A <code>Validator</code> that rejects {@link String} values matching a regular expression.
 *
 * This validator returns {@link net.shibboleth.metadata.validate.Validator.Action#DONE}
 * if the entire value is matched by the regular expression, thus terminating any validator sequence.
 *
 * @since 0.10.0
 */
@ThreadSafe
public class RejectStringRegexValidator extends BaseStringRegexValidator implements Validator<String> {

    @Override
    public Action validate(@Nonnull final String e, @Nonnull final Item<?> item, @Nonnull final String stageId) {
        final Matcher matcher = getPattern().matcher(e);
        if (matcher.matches()) {
            addErrorMessage(e, item, stageId);
            return Action.DONE;
        }
        return Action.CONTINUE;
    }
}
