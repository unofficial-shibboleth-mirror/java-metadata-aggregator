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

package net.shibboleth.metadata.query;

import net.shibboleth.metadata.AbstractItem;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.util.ItemMetadataSupport;
import net.shibboleth.utilities.java.support.collection.ClassToInstanceMultiMap;

public class MockItem extends AbstractItem<String> {

    private static final long serialVersionUID = 7960618036577597153L;

    public MockItem(String str){
        setMetadata(str);
    }
    
    public void setMetadata(String entityMetadata) {
        super.setData(entityMetadata);
    }
    
    public void setMetadataInfo(ClassToInstanceMultiMap<ItemMetadata> info) {
        getItemMetadata().clear();
        getItemMetadata().putAll(info);
    }

    @Override
    public Item<String> copy() {
        final MockItem clone = new MockItem(new String(unwrap()));
        ItemMetadataSupport.addAll(clone, getItemMetadata().values());
        return clone;
    }
}