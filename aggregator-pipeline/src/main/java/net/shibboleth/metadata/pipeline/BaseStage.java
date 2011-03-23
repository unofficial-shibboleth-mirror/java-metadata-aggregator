/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.pipeline;

import java.util.Collection;

import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.util.MetadataInfoHelper;

/**
 * A base class for {@link Stage} implementations.
 * 
 * @param <MetadataType> type of metadata elements this stage operates upon
 */
public abstract class BaseStage<MetadataType extends Metadata<?>> extends AbstractComponent implements
        Stage<MetadataType> {

    /**
     * Creates an {@link ComponentInfo}, delegates actual work on the collection to {@link #doExecute(Collection)}, adds
     * the {@link ComponentInfo} to all the resultant metadata elements and then sets its completion time.
     * 
     * {@inheritDoc}
     */
    public void execute(Collection<MetadataType> metadataCollection) throws StageProcessingException {
        final ComponentInfo compInfo = new ComponentInfo(this);

        doExecute(metadataCollection);

        MetadataInfoHelper.addToAll(metadataCollection, compInfo);
        compInfo.setCompleteInstant();
    }

    /**
     * Performs the stage process on the given metadata collection.
     * 
     * @param metadataCollection collection to be processed
     * 
     * @throws StageProcessingException thrown if there is an unrecoverable problem when processing the stage
     */
    protected abstract void doExecute(Collection<MetadataType> metadataCollection) throws StageProcessingException;
}