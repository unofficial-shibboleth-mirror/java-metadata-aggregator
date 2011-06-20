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

package net.shibboleth.metadata.pipeline;

import java.util.Collection;
import java.util.concurrent.Callable;

import net.shibboleth.metadata.Item;

import org.opensaml.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A {@link Callable} that executes a {@link Pipeline} and returns the given item collection. */
public class PipelineCallable implements Callable<Collection<? extends Item>> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(PipelineCallable.class);

    /** The pipeline to be executed, never null. */
    private Pipeline pipeline;

    /** The collection of items upon which the pipeline will operate. */
    private Collection<? extends Item> itemCollection;

    /**
     * Constructor.
     * 
     * @param invokedPipeline the pipeline that will be invoked; must be initialized; can not be null
     * @param items the collection of items upon which the pipeline will operate, can not be null
     */
    public PipelineCallable(Pipeline invokedPipeline, Collection<? extends Item> items) {
        Assert.isNotNull(invokedPipeline, "To-be-invoked pipeline can not be null");
        Assert.isTrue(invokedPipeline.isInitialized(), "To-be-invoked pipeline must be initialized");
        pipeline = invokedPipeline;

        Assert.isNotNull(items, "Item collection can not be null");
        itemCollection = items;
    }

    /** {@inheritDoc} */
    public Collection<? extends Item> call() {
        try {
            log.debug("Executing pipeline {} on an item collection containing {} items", pipeline.getId(),
                    itemCollection.size());
            pipeline.execute(itemCollection);
            return itemCollection;
        } catch (PipelineProcessingException e) {
            log.error("Execution of pipeline {} failed.", pipeline.getId(), e);
            return null;
        }
    }
}