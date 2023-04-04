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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.component.ComponentInitializationException;

/**
 * A {@link Validator} implementation which encapsulates the functionality of stepping
 * through a sequence of other validators.
 *
 * The {@link #validate} method of this class returns the
 * {@link net.shibboleth.metadata.validate.Validator.Action#DONE} value
 * to indicate that one of the called validators returned that value.
 *
 * @param <V> type of the object to be validated
 *
 * @since 0.10.0
 */
@ThreadSafe
public class ValidatorSequence<V> extends BaseValidator implements Validator<V> {

    /** The list of validators to apply. */
    @Nonnull @NonnullElements @Unmodifiable @GuardedBy("this")
    private List<Validator<V>> validators = CollectionSupport.emptyList();

    /**
     * Set the list of validators to apply to each item.
     * 
     * @param newValidators the list of validators to set
     */
    public synchronized void setValidators(
            @Nonnull @NonnullElements @Unmodifiable final List<Validator<V>> newValidators) {
        checkSetterPreconditions();
        validators = CollectionSupport.copyToList(newValidators);
    }

    /**
     * Gets the list of validators being applied to each item.
     * 
     * @return list of validators
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final synchronized List<Validator<V>> getValidators() {
        return validators;
    }

    @Override
    public @Nonnull Action validate(@Nonnull final V value, @Nonnull final Item<?> item, @Nonnull final String stageId)
            throws StageProcessingException {
        for (final Validator<V> validator: getValidators()) {
            final Action action = validator.validate(value, item, stageId);
            if (action == Action.DONE) {
                return action;
            }
        }
        return Action.CONTINUE;
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        for (final Validator<V> validator : getValidators()) {
            if (!validator.isInitialized()) {
                validator.initialize();
            }
        }
    }

}
