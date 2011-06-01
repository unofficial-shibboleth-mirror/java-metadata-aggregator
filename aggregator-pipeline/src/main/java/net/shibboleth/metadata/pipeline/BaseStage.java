/*
 * Licensed to the University Corporation for Advanced Internet Development, Inc.
 * under one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache 
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

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.util.ItemMetadataSupport;

/**
 * A base class for {@link Stage} implementations.
 * 
 * @param <ItemType> type of Item this stage operates upon
 */
public abstract class BaseStage<ItemType extends Item<?>> extends AbstractComponent implements Stage<ItemType> {

    /**
     * Creates an {@link ComponentInfo}, delegates actual work on the collection to {@link #doExecute(Collection)}, adds
     * the {@link ComponentInfo} to all the resultant Item elements and then sets its completion time.
     * 
     * {@inheritDoc}
     */
    public void execute(Collection<ItemType> itemCollection) throws StageProcessingException {
        final ComponentInfo compInfo = new ComponentInfo(this);

        doExecute(itemCollection);

        ItemMetadataSupport.addToAll(itemCollection, compInfo);
        compInfo.setCompleteInstant();
    }

    /**
     * Performs the stage processing on the given Item collection.
     * 
     * @param itemCollection collection to be processed
     * 
     * @throws StageProcessingException thrown if there is an unrecoverable problem when processing the stage
     */
    protected abstract void doExecute(Collection<ItemType> itemCollection) throws StageProcessingException;
}