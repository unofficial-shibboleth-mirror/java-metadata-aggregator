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

package net.shibboleth.metadata.dom.saml;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;

import net.shibboleth.metadata.dom.AbstractDOMTraversalStage;
import net.shibboleth.metadata.dom.DOMTraversalContext;
import net.shibboleth.metadata.dom.SimpleDOMTraversalContext;

/**
 * An abstract SAML DOM metadata traversal class using the template method pattern.
 *
 * A context object, extending {@link DOMTraversalContext}, is created by the
 * implementing subclass and passed to the {@link #visit} method when
 * each applicable {@link Element} is visited. In very simple cases, the
 * {@link SimpleDOMTraversalContext} may suffice, but more complicated
 * behaviour can be built up by extending or re-implementing that class.
 *
 * At the end of the traversal, the context's {@link DOMTraversalContext#end()}
 * method is called to perform any post-processing required.
 *
 * @param <C> the context to carry through the traversal
 *
 * @since 0.10.0
 */
@ThreadSafe
public abstract class AbstractSAMLTraversalStage <C extends DOMTraversalContext>
    extends AbstractDOMTraversalStage<C> {

    @Override
    protected @Nonnull String errorPrefix(@Nonnull final Element element) {
        return SAMLSupport.errorPrefix(element);
    }

}
