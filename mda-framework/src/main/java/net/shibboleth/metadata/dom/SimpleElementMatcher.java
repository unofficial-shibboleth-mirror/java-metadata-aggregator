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
import javax.annotation.concurrent.Immutable;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.xml.ElementSupport;

/**
 * Basic matcher class for {@link Element}s for use with the {@link Container} system.
 *
 * @since 0.10.0
 */
@Immutable
public class SimpleElementMatcher implements ElementMatcher {

    /** Element {@link QName} to match. */
    private final @Nonnull QName qname;

    /**
     * Constructor.
     * 
     * @param qnameToMatch qualified name ({@link QName}) to match
     */
    public SimpleElementMatcher(@Nonnull final QName qnameToMatch) {
        qname = Constraint.isNotNull(qnameToMatch, "qnameToMatch must not be null");
    }

    @Override
    public boolean match(final @Nonnull Element input) {
        return ElementSupport.isElementNamed(input, qname);
    }

}
