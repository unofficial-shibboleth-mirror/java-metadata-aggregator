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

package net.shibboleth.metadata.pipeline.impl;

import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;

/**
 * Base class extending {@link AbstractInitializableComponent} with helper methods
 * assisting with lifecycle management.
 */
@ThreadSafe
public class BaseInitializableComponent extends AbstractInitializableComponent {

    /**
     * Checks if the component is destroyed and, if so, throws a {@link DestroyedComponentException}.
     */
    protected final void ifDestroyedThrowDestroyedComponentException() {
        if (isDestroyed()) {
            throw new DestroyedComponentException("Component has already been destroyed and can no longer be used");
        }
    }

    /**
     * Checks if a component has not been initialized and, if so, throws a {@link UninitializedComponentException}.
     */
    protected final void ifNotInitializedThrowUninitializedComponentException() {
        if (!isInitialized()) {
            throw new UninitializedComponentException("Component has not yet been initialized and cannot be used.");
        }
    }

    /**
     * Checks if a component has been initialized and, if so, throws a {@link UnmodifiableComponentException}.
     */
    protected final void ifInitializedThrowUnmodifiabledComponentException() {
        if (isInitialized()) {
            throw new UnmodifiableComponentException(
                    "Component has already been initialized and can no longer be modified");
        }
    }

    /**
     * Helper for a setter method to check the standard preconditions.
     */
    protected final void throwSetterPreconditionExceptions() {
        ifDestroyedThrowDestroyedComponentException();
        ifInitializedThrowUnmodifiabledComponentException();
    }

    /**
     * Helper for any method to throw appropriate exceptions if we are either
     * not initialized, or have been destroyed.
     */
    protected final void throwComponentStateExceptions() {
        ifDestroyedThrowDestroyedComponentException();
        ifNotInitializedThrowUninitializedComponentException();
    }

}
