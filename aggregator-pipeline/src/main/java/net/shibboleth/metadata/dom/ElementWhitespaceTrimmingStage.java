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

/**
 * Stage to trim leading and trailing whitespace from the text content of named elements
 * within a {@link net.shibboleth.metadata.dom.DOMElementItem}.
 */
public class ElementWhitespaceTrimmingStage extends AbstractElementVisitingStage {

    /** Visitor to apply to each visited element. */
    @Nonnull private final ElementVisitor visitor = new WhitespaceTrimmingVisitor();

    @Override
    protected void visit(@Nonnull final Element e, @Nonnull final TraversalContext context) {
        visitor.visitElement(e, context.getItem());
    }

}