/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.metadata.validate.ValidatorSequence;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.annotation.constraint.Unmodifiable;
import net.shibboleth.shared.component.ComponentInitializationException;

/**
 * An abstract stage extending {@link AbstractDOMTraversalStage} to manage a collection of
 * {@link net.shibboleth.metadata.validate.Validator}s to individual values.
 *
 * @param <V> type of the values to be validated
 * @param <C> the context to carry through the traversal
 *
 * @since 0.9.0
 */
@ThreadSafe
public abstract class AbstractDOMValidationStage<V, C extends DOMTraversalContext>
    extends AbstractDOMTraversalStage<C> {

    /**
     * The validator sequence to apply.
     *
     * <p>
     * Thread safety: as a <code>final</code> field, access to <code>validators</code>
     * does not need to be synchronised for thread safety. The referenced object
     * is itself thread-safe.
     * </p>
     */
    @Nonnull @NonnullElements @Unmodifiable
    private final ValidatorSequence<V> validators = new ValidatorSequence<>();

    /**
     * Set the list of validators to apply to each item.
     * 
     * @param newValidators the list of validators to set
     */
    public void setValidators(@Nonnull @NonnullElements @Unmodifiable final List<Validator<V>> newValidators) {
        validators.setValidators(newValidators);
    }

    /**
     * Gets the list of validators being applied to each item.
     * 
     * @return list of validators
     */
    @Nonnull @NonnullElements @Unmodifiable
    public final List<Validator<V>> getValidators() {
        return validators.getValidators();
    }

    /**
     * Apply each of the configured validators in turn to the provided object.
     * 
     * @param obj object to be validated
     * @param context context for the validation
     * @throws StageProcessingException if errors occur during processing
     */
    protected void applyValidators(@Nonnull final V obj, @Nonnull final C context)
            throws StageProcessingException {
        final var myId = getId();
        assert myId != null;
        validators.validate(obj, context.getItem(), myId);
    }
    
    @Override
    protected void doDestroy() {
        /*
         * We created and initialized the validators object,
         * so we should destroy it.
         */
        validators.destroy();
        super.doDestroy();
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        final var myId = getId();
        assert myId != null;
        validators.setId(myId);
        validators.initialize();
    }

}
