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

package net.shibboleth.metadata.pipeline;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.component.DestructableComponent;
import net.shibboleth.shared.component.IdentifiedComponent;
import net.shibboleth.shared.component.InitializableComponent;

/**
 * A stage in a {@link Pipeline} that operates upon a collection {@link Item} in a particular manner.
 * 
 * <p>
 * Stages must be thread safe and reusable.
 * </p>
 *
 * @param <T> type of metadata upon which the stage operates
 */
@ThreadSafe
public interface Stage<T> extends DestructableComponent, IdentifiedComponent,
        InitializableComponent {

    /**
     * Transforms the given input data.
     * 
     * @param items the data to be transformed
     * 
     * @throws StageProcessingException thrown if there is a problem running this stage on the given input
     */
    void execute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws StageProcessingException;
}
