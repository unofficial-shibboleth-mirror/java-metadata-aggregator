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
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Element;

import net.shibboleth.metadata.pipeline.StageProcessingException;

/**
 * Abstract base class allowing a selected subset of {@link Element}s in a DOM document
 * to be validated as a given type.
 *
 * @param <T> type to convert each {@link Element} to for validation
 *
 * @since 0.10.0
 */
@ThreadSafe
public abstract class AbstractElementValidationStage<T> extends AbstractElementVisitingValidationStage<T, Element> {

    @Override
    protected void visit(@Nonnull final Element element, @Nonnull final DOMTraversalContext context)
            throws StageProcessingException {
        applyValidators(convert(element), context);
    }
    
}
