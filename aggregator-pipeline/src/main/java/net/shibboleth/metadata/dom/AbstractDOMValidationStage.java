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

package net.shibboleth.metadata.dom;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Base stage to apply a collection of validators to each object from each item.
 * 
 * @param <V> type of the object to be validated
 */ 
public abstract class AbstractDOMValidationStage<V> extends AbstractDOMTraversalStage {

    /** The list of validators to apply. */
    @Nonnull
    private List<Validator<V>> validators = Collections.emptyList();
    
    /**
     * Set the list of validators to apply to each item.
     * 
     * @param newValidators the list of validators to set
     */
    public void setValidators(@Nonnull final List<Validator<V>> newValidators) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        validators = ImmutableList.copyOf(Iterables.filter(newValidators, Predicates.notNull()));
    }
    
    /**
     * Gets the list of validators being applied to each item.
     * 
     * @return list of validators
     */
    @Nonnull
    public List<Validator<V>> getValidators() {
        return Collections.unmodifiableList(validators);
    }

    /**
     * Apply each of the configured validators in turn to the provided object.
     * 
     * @param obj object to be validated
     * @param context context for the validation
     * @throws StageProcessingException if errors occur during processing
     */
    protected void applyValidators(@Nonnull final V obj, @Nonnull final TraversalContext context)
            throws StageProcessingException {
        for (final Validator<V> validator: validators) {
            final Validator.Action action = validator.validate(obj, context.getItem(), getId());
            if (action == Validator.Action.DONE) {
                return;
            }
        }
    }
    
    @Override
    protected void doDestroy() {
        validators = null;
        super.doDestroy();
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        for (final Validator<V> validator : validators) {
            if (!validator.isInitialized()) {
                validator.initialize();
            }
        }
    }

}
