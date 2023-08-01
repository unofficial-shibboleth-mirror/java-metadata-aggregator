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

package net.shibboleth.metadata.dom.saml.shibboleth;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.AbstractDOMValidationStage;
import net.shibboleth.metadata.dom.DOMTraversalContext;
import net.shibboleth.metadata.dom.SimpleDOMTraversalContext;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.metadata.validate.ValidatorSequence;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.xml.AttributeSupport;
import net.shibboleth.shared.xml.ElementSupport;

/**
 * Stage to apply a collection of validators to Shibboleth <code>shibmd:Scope</code>
 * values.
 *
 * <p>
 * A separate collection of validators is used for the case of the <code>regexp</code>
 * attribute being <code>true</code> and <code>false</code>.
 * </p>
 *
 * @since 0.10.0
 */
@ThreadSafe
public class ScopeValidationStage extends AbstractDOMValidationStage<String, DOMTraversalContext> {

    /** The sequence of validators to apply to <code>regexp</code> scopes. */
    @GuardedBy("this")
    private @Nonnull ValidatorSequence<String> regexpValidators = new ValidatorSequence<>();

    /**
     * Set the sequence of validators to apply to each <code>regexp</code> scope.
     * 
     * @param newValidators the list of validators to set
     */
    public synchronized void setRegexpValidators(@Nonnull final List<Validator<String>> newValidators) {
        regexpValidators.setValidators(newValidators);
    }

    /**
     * Gets the sequence of validators being applied to each <code>regexp</code> scope.
     * 
     * @return list of validators
     */
    @Nonnull
    public synchronized List<Validator<String>> getRegexpValidators() {
        return regexpValidators.getValidators();
    }

    @Override
    protected @Nonnull DOMTraversalContext buildContext(final @Nonnull Item<Element> item) {
        return new SimpleDOMTraversalContext(item);
    }

    @Override
    protected boolean applicable(@Nonnull final Element element, @Nonnull final DOMTraversalContext context) {
        return ElementSupport.isElementNamed(element, ShibbolethMetadataSupport.SCOPE_NAME);
    }

    /**
     * Evaluate the <code>regexp</code> attribute on an {@link Element}.
     *
     * <p>Treats a missing attribute or non-boolean string values as <code>false</code></p>8
     *
     * @param element {@link Element} hosting the <code>regexp</code> attribute.
     * @return <code>true</code> if the attribute has a true value (<code>1</code> or
     *   <code>true</code> or variations), otherwise <code>false</code>
     */
    private boolean evaluateRegexpAttribute(final @Nonnull Element element) {
        final var attr = AttributeSupport.getAttribute(element, ShibbolethMetadataSupport.REGEXP_ATTRIB_NAME);
        if (attr == null) {
            return false;
        }

        final Boolean isRegexp = AttributeSupport.getAttributeValueAsBoolean(attr);
        if (isRegexp == null) {
            return false;
        }
        
        return isRegexp.booleanValue();
    }

    @Override
    protected void visit(final @Nonnull Element element, final @Nonnull DOMTraversalContext context)
            throws StageProcessingException {

        final String text = element.getTextContent();
        assert text != null;

        if (!evaluateRegexpAttribute(element)) {
            // non-regexp Scope, apply normal validators
            applyValidators(text, context);
        } else {
            // regexp Scope, apply secondary validators
            regexpValidators.validate(text, context.getItem(), ensureId());
        }
    }

    @Override
    protected void doDestroy() {
        regexpValidators.destroy();
        super.doDestroy();
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        regexpValidators.setId(ensureId());
        regexpValidators.initialize();
    }

}
