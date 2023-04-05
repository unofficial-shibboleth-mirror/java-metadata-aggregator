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

package net.shibboleth.metadata.dom.saml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.shibboleth.metadata.dom.AbstractElementVisitingStage;
import net.shibboleth.metadata.dom.DOMTraversalContext;
import net.shibboleth.shared.xml.QNameSupport;

/**
 * A Stage which checks the text content of the named elements to verify that
 * they meet the constraints of SAML string values.
 *
 * @since 0.10.0
 */
@ThreadSafe
public class SAMLStringElementCheckingStage extends AbstractElementVisitingStage {

    /** Regular expression matching a string which contains no non-whitespace characters. */
    private static final Pattern ALL_WHITE_SPACE_PATTERN = Pattern.compile("^[ \\t\\r\\n\\x85\\u2028]*$");

    /**
     * Check that the string value is appropriate.
     * 
     * This is done using a regular expression because Java's {@link String#trim} method does not
     * use XML's definition of white space.
     * 
     * @param visited DOM {@link Node} being checked
     * @return <code>true</code> if the {@link Node}'s value matches
     */
    private static boolean match(@Nonnull final Node visited) {
        final String textContent = visited.getTextContent();
        final Matcher matcher = ALL_WHITE_SPACE_PATTERN.matcher(textContent);
        return matcher.matches();
    }

    @Override
    protected @Nonnull String errorPrefix(@Nonnull final Element element) {
        return SAMLSupport.errorPrefix(element);
    }

    @Override
    protected void visit(@Nonnull final Element e, @Nonnull final DOMTraversalContext context) {
        if (match(e)) {
            final StringBuilder b = new StringBuilder("element ");
            b.append(QNameSupport.getNodeQName(e));
            b.append(" must contain at least one non-whitespace character");
            final var message = b.toString();
            assert message != null;
            addError(context.getItem(), e, message);
        }
    }

}
