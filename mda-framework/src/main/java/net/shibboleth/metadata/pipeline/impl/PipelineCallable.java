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

package net.shibboleth.metadata.pipeline.impl;

import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.Pipeline;
import net.shibboleth.metadata.pipeline.PipelineProcessingException;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.primitive.LoggerFactory;

/**
 * A {@link Callable} that executes a {@link Pipeline} and returns the given item collection.
 * 
 * @param <T> type of the items processed by the pipeline
 */
@Immutable
public class PipelineCallable<T> implements Callable<List<Item<T>>> {

    /** Class logger. */
    private static final @Nonnull Logger LOG = LoggerFactory.getLogger(PipelineCallable.class);

    /** The pipeline to be executed, never null. */
    private final @Nonnull Pipeline<T> thePipeline;

    /** The collection of items upon which the pipeline will operate. */
    private final @Nonnull @NonnullElements List<Item<T>> theItems;

    /**
     * Constructor.
     * 
     * @param pipeline the pipeline that will be invoked; must be initialized; can not be null
     * @param items the collection of items upon which the pipeline will operate, can not be null
     */
    public PipelineCallable(@Nonnull final Pipeline<T> pipeline,
            @Nonnull @NonnullElements final List<Item<T>> items) {
        thePipeline = Constraint.isNotNull(pipeline, "To-be-invoked pipeline can not be null");
        Constraint.isTrue(pipeline.isInitialized(), "To-be-invoked pipeline must be initialized");

        theItems = Constraint.isNotNull(items, "Item collection can not be null");
    }

    @Override @Nonnull @NonnullElements public List<Item<T>> call() throws PipelineProcessingException {
        LOG.debug("Executing pipeline {} on an item collection containing {} items", thePipeline.getId(),
                theItems.size());
        thePipeline.execute(theItems);
        return theItems;
    }
}
