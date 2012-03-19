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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.DestructableComponent;
import net.shibboleth.utilities.java.support.component.IdentifiableComponent;
import net.shibboleth.utilities.java.support.component.InitializableComponent;

/**
 * A pipeline represents an ordered list of {@link Stage} that operate on a collection of {@link Item}. In general the
 * first stage is responsible for populating the collection with an initial set of Items which subsequent stages further
 * modify.
 * 
 * Each pipeline must be initialized, via the {@link #initialize()} method, before use. After a pipeline has been
 * initialized it may never be re-initialized. A pipeline is not considered initialized until all of its {@link Stage},
 * have been initialized.
 * 
 * Pipelines are reusable and threadsafe.
 * 
 * @param <ItemType> type of Item which is produced by this source
 */
@ThreadSafe
public interface Pipeline<ItemType extends Item<?>> extends DestructableComponent, IdentifiableComponent,
        InitializableComponent {

    /**
     * Gets the list of Stages within the pipeline.
     * 
     * @return unmodifiable list of stages within the pipeline
     */
    @Nonnull @NonnullElements public List<? extends Stage<ItemType>> getStages();

    /**
     * Executes each registered {@link Stage} in turn.
     * 
     * @param itemCollection the collection that will hold the Item as it passes from stage to stage
     * 
     * @throws PipelineProcessingException thrown if there is a problem processing the pipeline
     */
    public void execute(@Nonnull @NonnullElements final Collection<ItemType> itemCollection)
            throws PipelineProcessingException;
}