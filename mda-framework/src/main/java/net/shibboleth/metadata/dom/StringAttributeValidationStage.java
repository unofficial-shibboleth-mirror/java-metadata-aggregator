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

import org.w3c.dom.Attr;

import net.shibboleth.metadata.pipeline.Stage;

/**
 * A {@link Stage} allowing validation of DOM {@link Attr}s treated as {@link String}s.
 *
 * @since 0.10.0
 */
@ThreadSafe
public class StringAttributeValidationStage extends AbstractAttributeValidationStage<String> {

    @SuppressWarnings("null")
    @Override
    protected @Nonnull String convert(@Nonnull final Attr attr) {
        return attr.getTextContent();
    }

}
