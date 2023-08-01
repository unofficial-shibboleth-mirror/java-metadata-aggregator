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

package net.shibboleth.metadata.validate;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.shared.component.ComponentInitializationException;

/**
 * An abstract base class for {@link Validator} implementations which validate a value of
 * one type "as" another type: for example, to validate a <code>String</code> "as" an
 * <code>InternetDomainName</code>.
 *
 * <p>The implementation calls a template method in the implementation subclass to perform the
 * conversion. If the conversion succeeds, a sequence of {@link Validator}s are applied to
 * that new value.</p>
 *
 * <p>If the value cannot be converted to the new type, the template method is expected to
 * throw {@link IllegalArgumentException}. In this case, behaviour depends on the
 * {@link #conversionRequired} property.</p>
 *
 * <p>If {@link #conversionRequired} is <code>true</code> (the default) then an error
 * status will be applied to the {@link Item}, and the validator will return
 * {@link net.shibboleth.metadata.validate.Validator.Action#DONE}.</p>
 *
 * <p>If {@link #conversionRequired} is <code>false</code> then the validator
 * will simply return {@link net.shibboleth.metadata.validate.Validator.Action#CONTINUE}
 * so that subsequent validators may still be applied. This allows several "as" validators
 * to be applied in sequence, each taking a different approach.</p>
 *
 * @param <V> type of the original value
 * @param <A> type of the new value to which validators should be applied
 *
 * @since 0.10.0
 */
@ThreadSafe
public abstract class BaseAsValidator<V, A> extends BaseValidator implements Validator<V> {

    /** The validator sequence to apply. */
    @GuardedBy("this")
    private @Nonnull ValidatorSequence<A> validators = new ValidatorSequence<>();

    /** Whether conversion to the new type must succeed. Default: <code>true</code> */
    private boolean conversionRequired = true;

    /**
     * Set the list of validators to apply to each item.
     * 
     * @param newValidators the list of validators to set
     */
    public synchronized void setValidators(@Nonnull final List<Validator<A>> newValidators) {
        validators.setValidators(newValidators);
    }

    /**
     * Gets the list of validators being applied to each item.
     * 
     * @return list of validators
     */
    @Nonnull
    public synchronized List<Validator<A>> getValidators() {
        return validators.getValidators();
    }

    /**
     * Set whether conversion to the new type is required to succeed.
     *
     * @param required <code>true</code> if the conversion is required to succeed
     */
    public void setConversionRequired(final boolean required) {
        conversionRequired = required;
    }

    /**
     * Returns whether conversion to the new type is required to succeed.
     *
     * @return <code>true</code> if the conversion is required to succeed
     */
    public boolean isConversionRequired() {
        return conversionRequired;
    }

    /**
     * Apply each of the configured validators in turn to the provided object.
     *
     * @param value object to be validated
     * @param item the {@link Item} context for the validation
     *
     * @return the result of applying the validators to the value
     *
     * @throws StageProcessingException if errors occur during processing
     */
    protected @Nonnull Action applyValidators(@Nonnull final A value, @Nonnull final Item<?> item)
            throws StageProcessingException {
        return validators.validate(value, item, ensureId());
    }

    /**
     * Convert from the old value type to the new.
     *
     * @param from a value of the old type
     * @return a value of the new type
     * @throws IllegalArgumentException if a conversion can not be performed
     */
    protected abstract @Nonnull A convert(@Nonnull final V from) throws IllegalArgumentException;

    @Override
    public @Nonnull Action validate(@Nonnull final V t, @Nonnull final Item<?> item, @Nonnull final String stageId)
            throws StageProcessingException {
        try {
            final A v = convert(t);
            return applyValidators(v, item);
        } catch (final IllegalArgumentException e) {
            if (isConversionRequired()) {
                addErrorMessage(t, item, stageId);
                return Action.DONE;
            } else {
                return Action.CONTINUE;
            }
        }
    }

    @Override
    protected void doDestroy() {
        validators.destroy();
        super.doDestroy();
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        validators.setId(ensureId());
        validators.initialize();
    }

}
