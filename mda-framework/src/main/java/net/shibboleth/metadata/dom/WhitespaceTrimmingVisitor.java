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
import javax.annotation.concurrent.Immutable;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.shibboleth.metadata.Item;

/**
 * {@link Node} visitor which trims leading and trailing whitespace from the
 * visited node's text content.
 *
 * @since 0.9.0
 */
@Immutable
class WhitespaceTrimmingVisitor implements NodeVisitor, ElementVisitor, AttrVisitor {

    @Override
    public void visitNode(@Nonnull final Node visited, @Nonnull final Item<Element> item) {
        final String originalText = visited.getTextContent();
        final String newText = originalText.strip();
        visited.setTextContent(newText);
    }

    @Override
    public void visitElement(@Nonnull final Element visited, @Nonnull final Item<Element> item) {
        visitNode(visited, item);
    }
    
    @Override
    public void visitAttr(@Nonnull final Attr visited, @Nonnull final Item<Element> item) {
        visitNode(visited, item);
    }
    
}
