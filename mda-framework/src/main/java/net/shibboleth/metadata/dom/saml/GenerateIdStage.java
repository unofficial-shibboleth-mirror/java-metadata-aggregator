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
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.shared.annotation.constraint.NonnullAfterInit;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.primitive.DeprecationSupport;
import net.shibboleth.shared.primitive.DeprecationSupport.ObjectType;
import net.shibboleth.shared.security.IdentifierGenerationStrategy;
import net.shibboleth.shared.security.impl.Type4UUIDIdentifierGenerationStrategy;
import net.shibboleth.shared.xml.AttributeSupport;


/**
 * A stage that populates the ID attribute of an EntitiesDescriptor or EntityDescriptor.
 *
 * <p>
 * By default, the stage will use a {@link Type4UUIDIdentifierGenerationStrategy}
 * to generate identifiers.
 * </p>
 */
@ThreadSafe
public class GenerateIdStage extends AbstractIteratingStage<Element> {

    /** QName of the ID attribute added to the descriptor. */
    public static final @Nonnull QName ID_ATTRIB = new QName("ID");

    /** Strategy used to generate identifiers. */
    @GuardedBy("this")
    private @NonnullAfterInit IdentifierGenerationStrategy generator;

    /** Constructor. */
    public GenerateIdStage() {
    }

    /**
     * Constructor.
     *
     * <p>
     * Note: BeansFileTest has an explicit exception to allow this deprecated
     * constructor. It should be updated when this constructor is removed.
     * </p>
     * 
     * @param newGenerator ID generation strategy to use
     * @deprecated Use the zero-argument constructor and the property instead.
     */
    @Deprecated(since="0.10.0", forRemoval=true)
    public GenerateIdStage(@Nonnull final IdentifierGenerationStrategy newGenerator) {
        DeprecationSupport.warnOnce(ObjectType.METHOD, "single-argument constructor", "GenerateIdStage",
                "zero-argument constructor and 'generator' property");
        generator = Constraint.isNotNull(newGenerator, "ID generation strategy can not be null");
    }

    /**
     * Gets the {@link IdentifierGenerationStrategy} being used.
     *
     * @return the {@link IdentifierGenerationStrategy} being used
     */
    public synchronized @NonnullAfterInit IdentifierGenerationStrategy getGenerator() {
        return generator;
    }

    /**
     * Sets the {@link IdentifierGenerationStrategy} to use.
     *
     * @param newGenerator the {@link IdentifierGenerationStrategy} to use
     */
    public synchronized void setGenerator(final @Nonnull IdentifierGenerationStrategy newGenerator) {
        checkSetterPreconditions();
        generator = Constraint.isNotNull(newGenerator, "ID generation strategy can not be null");
    }

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final var element = item.unwrap();
        if (!SAMLMetadataSupport.isEntityOrEntitiesDescriptor(element)) {
            return;
        }

        Attr idAttribute = AttributeSupport.getAttribute(element, ID_ATTRIB);
        if (idAttribute == null) {
            final var ownerDocument = element.getOwnerDocument();
            assert ownerDocument != null;
            idAttribute = AttributeSupport.constructAttribute(ownerDocument, ID_ATTRIB);
            element.setAttributeNode(idAttribute);
        }

        idAttribute.setValue(getGenerator().generateIdentifier());
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
        if (generator == null) {
            generator = new Type4UUIDIdentifierGenerationStrategy();
        }
    }
}
