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

package net.shibboleth.metadata;

import java.util.Collection;

/**
 * Factory used to create the {@link Collection} that will be passed in to each child pipeline.
 * 
 * @param <ItemType> the type of items the produced collection will contain
 */
public interface ItemCollectionFactory<ItemType extends Item> {

    /**
     * Creates the {@link Collection}.
     * 
     * @return the {@link Collection}
     */
    public Collection<ItemType> newCollection();
}