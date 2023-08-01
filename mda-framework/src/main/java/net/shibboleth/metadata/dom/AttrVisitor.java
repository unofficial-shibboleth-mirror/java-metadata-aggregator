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

import net.shibboleth.metadata.Item;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Provides a variation of the Visitor pattern for performing operations on
 * DOM attributes which are part of {@link Element} items.
 *
 * <p>
 * All implementations of this interface <strong>must</strong> be thread-safe.
 * </p>
 *
 * @since 0.9.0
 */
@ThreadSafe
public interface AttrVisitor {

    /**
     * Called on each {@link Attr} visited as part of the processing
     * of an {@link Element} item.
     * 
     * @param visited the {@link Attr} being visited.
     * @param item the {@link Item} which is the context for the visit.
     */
    void visitAttr(@Nonnull Attr visited, @Nonnull Item<Element> item);
    
}
