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

package net.shibboleth.metadata.validate.testing;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.validate.BaseValidator;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.shared.component.ComponentInitializationException;

/**
 * A {@link Validator} implementation which collects the values passed to it for
 * validation.
 * 
 * <p>This can be used in tests to record the nodes which are visited, and the
 * order in which this happens.</p>
 *
 * @param <T> type of the values to be validated
 */
public class CollectingValidator<T> extends BaseValidator implements Validator<T> {

    /** Values this validator has seen, in order. */
    private final @Nonnull List<T> values = new ArrayList<>();
    
    /**
     * Return the values recorded by this validator.
     * 
     * @return the values recorded by this validator
     */
    public @Nonnull List<T> getValues() {
        return values;
    }

    /**
     * Convenience method to return an instance of this validator.
     *
     * @param id identifier for this instance
     * @param <TT> type validated by the instance to create
     * @return new validator instance
     * @throws ComponentInitializationException if something goes wrong in initialisation
     */
    public static @Nonnull <TT> CollectingValidator<TT> getInstance(final @Nonnull String id)
            throws ComponentInitializationException {
        final var instance = new CollectingValidator<TT>();
        instance.setId(id);
        instance.initialize();
        return instance;
    }

    @Override
    public @Nonnull Action validate(@Nonnull T e, @Nonnull Item<?> item, @Nonnull String stageId)
            throws StageProcessingException {
        values.add(e);
        return Action.CONTINUE;
    }
    
}
