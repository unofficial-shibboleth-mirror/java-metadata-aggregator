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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.util.ItemMetadataSupport;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.AbstractDestructableIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

/**
 * A base class for {@link Stage} implementations.
 * 
 * @param <ItemType> type of Item this stage operates upon
 */
@ThreadSafe
public abstract class BaseStage<ItemType extends Item<?>> extends
        AbstractDestructableIdentifiableInitializableComponent implements Stage<ItemType> {

    /** {@inheritDoc} */
    public synchronized void setId(@Nonnull @NotEmpty final String componentId) {
        super.setId(componentId);
    }

    /**
     * Creates an {@link ComponentInfo}, delegates actual work on the collection to {@link #doExecute(Collection)}, adds
     * the {@link ComponentInfo} to all the resultant Item elements and then sets its completion time.
     * 
     * {@inheritDoc}
     */
    public void execute(@Nonnull @NonnullElements final Collection<ItemType> itemCollection)
            throws StageProcessingException {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);

        final ComponentInfo compInfo = new ComponentInfo(this);

        doExecute(itemCollection);

        ItemMetadataSupport.addToAll(itemCollection, compInfo);
        compInfo.setCompleteInstant();
    }

    /**
     * Performs the stage processing on the given Item collection.
     * 
     * <p>
     * The stage is guaranteed to be have been initialized and not destroyed when this is invoked.
     * </p>
     * 
     * @param itemCollection collection to be processed
     * 
     * @throws StageProcessingException thrown if there is an unrecoverable problem when processing the stage
     */
    protected abstract void doExecute(@Nonnull @NonnullElements final Collection<ItemType> itemCollection)
            throws StageProcessingException;
}