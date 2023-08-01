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

import org.w3c.dom.Element;

/**
 * Functional interface allowing for the construction of a new {@link Element}
 * within the given {@link Container}.
 *
 * @since 0.10.0
 */
@FunctionalInterface
public interface ElementMaker {

    /**
     * Construct an {@link Element} within the given {@link Container}.
     *
     * @param container parent {@link Container} for the new {@link Element}
     * @return newly constructed {@link Element}
     */
    @Nonnull Element make(@Nonnull Container container);

}
