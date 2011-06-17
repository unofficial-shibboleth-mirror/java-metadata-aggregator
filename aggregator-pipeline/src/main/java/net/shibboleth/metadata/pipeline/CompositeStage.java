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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.shibboleth.metadata.Item;

/**
 * A stage that is composed of other stages. This allows a collection of stages to be grouped together and for that
 * composition to the be referenced and reused.
 * 
 * @param <ItemType> type of Items this stage, and its composed stages, operate upon
 */
public class CompositeStage<ItemType extends Item<?>> extends AbstractComponent implements Stage<ItemType> {

    /** Stages which compose this stage. */
    private List<Stage<ItemType>> composedStages;

    /**
     * Gets an unmodifiable list the stages that compose this stage.
     * 
     * @return list the stages that compose this stage, never null nor containing null elements
     */
    public List<Stage<ItemType>> getComposedStages() {
        return composedStages;
    }

    /**
     * Sets the list the stages that compose this stage.
     * 
     * @param stages list the stages that compose this stage, may be null or contain null elements
     */
    public synchronized void setComposedStages(List<Stage<ItemType>> stages) {
        if (isInitialized()) {
            return;
        }

        ArrayList<Stage<ItemType>> newStages = new ArrayList<Stage<ItemType>>();
        for (Stage<ItemType> stage : stages) {
            if (stage != null) {
                newStages.add(stage);
            }
        }

        composedStages = Collections.unmodifiableList(newStages);
    }

    /** {@inheritDoc} */
    public void execute(Collection<ItemType> itemCollection) throws StageProcessingException {
        for (Stage<ItemType> stage : composedStages) {
            stage.execute(itemCollection);
        }
    }
}