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

/**
 * A strategy for determining if an item meets a given criteria.
 * 
 * @param <ItemType> the item type upon which this selector operates
 */
public interface ItemSelectionStrategy<ItemType extends Item> {

    /**
     * Determines if the item meets the expected criteria.
     * 
     * @param item the being inspected, never null
     * 
     * @return true of the item meets the expected criteria, false otherwise
     */
    public boolean isSelectedItem(ItemType item);
}
