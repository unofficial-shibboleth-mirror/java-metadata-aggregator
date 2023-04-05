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

package net.shibboleth.metadata.dom.saml.mdui;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.AbstractDOMTraversalStage;
import net.shibboleth.metadata.dom.DOMTraversalContext;
import net.shibboleth.metadata.dom.SimpleDOMTraversalContext;
import net.shibboleth.shared.net.IPRange;
import net.shibboleth.shared.xml.ElementSupport;

/**
 * A stage which validates <code>mdui:IPHint</code> elements.
 *
 * @since 0.10.0
 */
@ThreadSafe
public class IPHintValidationStage extends AbstractDOMTraversalStage<DOMTraversalContext> {

    /** Whether to check that the CIDR notation describes a network. Defaults to true. */
    @GuardedBy("this") private boolean checkingNetworks = true;
    
    /**
     * Gets whether the stage is checking for network addresses only.
     * 
     * @return whether the stage is checking for network addresses only
     */
    public final synchronized boolean isCheckingNetworks() {
        return checkingNetworks;
    }

    /**
     * Sets whether the stage is checking for network addresses only.
     * 
     * @param check whether to check for network addresses only
     */
    public synchronized void setCheckingNetworks(final boolean check) {
        checkSetterPreconditions();
        this.checkingNetworks = check;
    }

    @Override
    protected boolean applicable(@Nonnull final Element element, @Nonnull final DOMTraversalContext context) {
        return ElementSupport.isElementNamed(element, MDUISupport.IPHINT_NAME);
    }

    @Override
    protected void visit(@Nonnull final Element ipHint, @Nonnull final DOMTraversalContext context) {
        final String hint = ipHint.getTextContent();
        assert hint != null;
        try {
            final IPRange range = IPRange.parseCIDRBlock(hint);
            if (isCheckingNetworks()) {
                if (range.getHostAddress() != null) {
                    addError(context.getItem(), ipHint, "invalid IPHint '" + hint +
                            "': CIDR notation represents a host, not a network");
                }
            }
        } catch (final IllegalArgumentException e) {
            addError(context.getItem(), ipHint, "invalid IPHint '" + hint + "': " + e.getMessage());
        }
    }

    @Override
    protected @Nonnull DOMTraversalContext buildContext(final @Nonnull Item<Element> item) {
        return new SimpleDOMTraversalContext(item);
    }

}
