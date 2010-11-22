/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
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

package net.shibboleth.metadata;

import net.shibboleth.metadata.AbstractMetadata;
import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.MetadataInfo;
import net.shibboleth.metadata.util.ClassToInstanceMultiMap;
import net.shibboleth.metadata.util.MetadataInfoHelper;

public class MockMetadata extends AbstractMetadata<String> {

    private static final long serialVersionUID = 7960618036577597153L;

    public MockMetadata(String str){
        setMetadata(str);
    }
    
    public void setMetadata(String entityMetadata) {
        super.setMetadata(entityMetadata);
    }
    
    public void setMetadataInfo(ClassToInstanceMultiMap<MetadataInfo> info) {
        getMetadataInfo().clear();
        getMetadataInfo().putAll(info);
    }

    public Metadata<String> copy() {
        MockMetadata clone = new MockMetadata(new String(getMetadata()));
        MetadataInfoHelper.addToAll(clone, getMetadataInfo().values().toArray(new MetadataInfo[] {}));
        return clone;
    }
}