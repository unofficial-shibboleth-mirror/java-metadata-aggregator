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
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * A stage which removes all evidence of a given XML namespace from each metadata item.
 *
 * @since 0.9.0
 */
@ThreadSafe
public class NamespaceStrippingStage extends AbstractNamespacesStrippingStage {

    /**
     * XML namespace to remove.
     */
    @Nonnull @NotEmpty @GuardedBy("this")
    private String namespace;

    /**
     * Gets the namespace being checked for.
     * 
     * @return namespace URI
     */
    @Nullable public final synchronized String getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace to check for.
     * 
     * @param ns namespace URI as a string
     */
    public synchronized void setNamespace(@Nonnull @NotEmpty final String ns) {
        throwSetterPreconditionExceptions();
        namespace = Constraint.isNotNull(StringSupport.trimOrNull(ns),
                "target namespace can not be null or empty");
    }

    @Override
    protected boolean removingNamespace(final String ns) {
        return getNamespace().equals(ns);
    }

    @Override
    protected void doDestroy() {
        namespace = null;

        super.doDestroy();
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (namespace == null) {
            throw new ComponentInitializationException("target namespace can not be null or empty");
        }
    }

}
