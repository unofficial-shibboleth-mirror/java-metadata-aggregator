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

package net.shibboleth.metadata.validate;

import javax.annotation.Nonnull;

import net.shibboleth.metadata.Item;

/**
 * A {@link Validator} which rejects any value, returning
 * {@link net.shibboleth.metadata.validate.Validator.Action#DONE}
 * to terminate any validator sequence.
 *
 * An error status is added using the value of the given property as a format string.
 * The message defaults to a simple rejection message including the object's string value.
 *
 * @param <V> type of the object to be validated
 */
public class RejectAllValidator<V> extends BaseValidator implements Validator<V> {

    @Override
    public Action validate(@Nonnull final V e, @Nonnull final Item<?> item, @Nonnull final String stageId) {
        addErrorMessage(e, item, stageId);
        return Action.DONE;
    }

}
