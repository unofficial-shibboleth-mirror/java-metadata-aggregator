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

package net.shibboleth.metadata.dom;

import javax.annotation.Nonnull;

import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;

/**
 * The context for a particular DOM traversal.
 *
 * Implementations may add additional fields and methods to the definition
 * of a {@link DOMTraversalContext}, and may define {@link #end()} to perform
 * operations at the end of the traversal.
 */
public interface DOMTraversalContext {

    /**
     * Get the {@link Item} this traversal is being performed on.
     * 
     * @return the context {@link Item}
     */
    @Nonnull Item<Element> getItem();

    /**
     * Perform any clean-up or final operations for the traversal.
     *
     * Called at the end of the traversal, may be overridden by subclasses.
     */
    void end();
}
