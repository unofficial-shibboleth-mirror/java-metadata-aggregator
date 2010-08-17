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

package edu.internet2.middleware.shibboleth.metadata.core;

import org.opensaml.util.Assert;
import org.opensaml.util.Strings;

/** Carries a unique identifier of the entity described by a piece of metadata. */
public class EntityIdInfo implements MetadataInfo {

    /** Serial version UID. */
    private static final long serialVersionUID = -3907907112463674533L;
    
    /** Unique ID for the entity. */
    private String entityId;

    /**
     * Constructor.
     * 
     * @param id a unique identifier for the entity, never null
     */
    public EntityIdInfo(String id) {
        entityId = Strings.trimOrNull(id);
        Assert.isNotNull(entityId, "Entity ID may not be null or empty");
    }

    /**
     * Gets a unique identifier for the entity.
     * 
     * @return unique identifier for the entity, never null
     */
    public String getEntityId() {
        return entityId;
    }
}