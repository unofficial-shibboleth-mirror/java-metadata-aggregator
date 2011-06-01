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

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.Item;

/**
 * A stage in a {@link Pipeline} that operates upon a collection {@link Item} in a particular manner.
 * 
 * Stages must be thread safe and reusable.
 * 
 * @param <ItemType> type of Item upon which the stage operates
 */
@ThreadSafe
public interface Stage<ItemType extends Item<?>> extends Component {

    /**
     * Transforms the given input data.
     * 
     * @param itemCollection the data to be transformed
     * 
     * @throws StageProcessingException thrown if there is a problem running this stage on the given input
     */
    public void execute(Collection<ItemType> itemCollection) throws StageProcessingException;
}