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
import java.util.Iterator;

import net.shibboleth.metadata.Metadata;

/**
 * Base class for {@link Stage} implementations that iterate over each metadata element in a collection and do
 * something.
 * 
 * @param <MetadataType> type of metadata elements this stage operates upon
 */
public abstract class BaseIteratingStage<MetadataType extends Metadata<?>> extends BaseStage<MetadataType> {

    /**
     * Iterates over each element of the metadata collection and delegates the processing of that element to
     * {@link #doExecute(Metadata)}.
     * 
     * {@inheritDoc}
     */
    protected void doExecute(Collection<MetadataType> metadataCollection) throws StageProcessingException {
        Iterator<MetadataType> metadataIterator = metadataCollection.iterator();

        MetadataType metadata;
        while (metadataIterator.hasNext()) {
            metadata = metadataIterator.next();
            if (doExecute(metadata)) {
                metadataIterator.remove();
            }
        }
    }

    /**
     * Processes a given metadata element.
     * 
     * @param metadata metadata element on which to operate
     * 
     * @return true of the metadata element should be retained in the collection, false if not
     * 
     * @throws StageProcessingException thrown if there is a problem with the stage processing
     */
    protected abstract boolean doExecute(MetadataType metadata) throws StageProcessingException;
}