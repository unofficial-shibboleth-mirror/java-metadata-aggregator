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

package net.shibboleth.metadata.validate.string;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.validate.BaseValidator;
import net.shibboleth.shared.annotation.constraint.NonnullAfterInit;
import net.shibboleth.shared.component.ComponentInitializationException;

/**
 * A base class for <code>Validator</code>s that match {@link String} values.
 *
 * @since 0.10.0
 */
@ThreadSafe
public abstract class BaseStringValueValidator extends BaseValidator {

    /** Value to be accepted by this validator. */
    @NonnullAfterInit @GuardedBy("this") private String value;

    /**
     * Returns the value.
     *
     * @return Returns the value.
     */
    @NonnullAfterInit public final synchronized String getValue() {
        return value;
    }

    /**
     * Sets the value to be matched.
     *
     * @param v the value to set.
     */
    public synchronized void setValue(@Nonnull final String v) {
        value = v;
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (getValue() == null) {
            throw new ComponentInitializationException("value to be matched can not be null");
        }
    }

}
