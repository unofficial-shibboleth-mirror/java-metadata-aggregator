/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * A simple implementation of the {@link DOMTraversalContext} interface.
 */
public class SimpleDOMTraversalContext implements DOMTraversalContext {

    /** The {@link Item} this traversal is being performed on. */
    @Nonnull
    private final Item<Element> item;

    /**
     * Constructor.
     * 
     * @param contextItem the {@link Item} this traversal is being performed on.
     */
    public SimpleDOMTraversalContext(@Nonnull final Item<Element> contextItem) {
        item = contextItem;
    }

    @Override
    @Nonnull
    public final Item<Element> getItem() {
        return item;
    }

    @Override
    public void end() {
    }

}
