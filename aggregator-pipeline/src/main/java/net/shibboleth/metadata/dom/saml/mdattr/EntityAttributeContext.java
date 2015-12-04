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

package net.shibboleth.metadata.dom.saml.mdattr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An entity attribute context against which matches can take place. It consists
 * of the attribute's value, <code>Name</code> and <code>NameFormat</code> attributes,
 * and the entity's <code>registrationAuthority</code>, if any.
 * 
 * A matcher is a {@link com.google.common.base.Predicate} over such a context.
 */
public interface EntityAttributeContext {

    /**
     * Returns the registration authority component, or <code>null</code>.
     * 
     * @return the registration authority, or <code>null</code>
     */
    @Nullable
    String getRegistrationAuthority();
    
    /**
     * Returns the attribute's <code>NameFormat</code>.
     * 
     * @return the attribute's <code>NameFormat</code>.
     */
    @Nonnull
    String getNameFormat();
    
    /**
     * Returns the attribute's <code>Name</code>.
     * 
     * @return the attribute's <code>Name</code>
     */
    @Nonnull
    String getName();
    
    /**
     * Returns the attribute's value.
     * 
     * @return the attribute's value
     */
    @Nonnull
    String getValue();
    
}
