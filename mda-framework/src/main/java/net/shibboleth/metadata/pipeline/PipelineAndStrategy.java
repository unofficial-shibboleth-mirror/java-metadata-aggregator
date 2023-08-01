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

import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.shibboleth.metadata.Item;
import net.shibboleth.shared.logic.Constraint;

/**
 * Representation of a {@link Pipeline} and the {@link Predicate} to be used as a strategy to select
 * items sent to that pipeline.
 *
 * @param pipeline a {@link Pipeline} to which items will be sent
 * @param strategy a {@link Predicate} used to determine which items will be sent to the pipeline
 * @param <T> type of items processed by the pipeline
 *
 * @since 0.10.0
 */
public record PipelineAndStrategy<T>(@Nonnull Pipeline<T> pipeline, @Nonnull Predicate<Item<T>> strategy) {

    /**
     * Constructor.
     */
    public PipelineAndStrategy {
        Constraint.isNotNull(pipeline, "Pipeline can not be null");
        Constraint.isNotNull(strategy, "strategy can not be null");
    }
}
