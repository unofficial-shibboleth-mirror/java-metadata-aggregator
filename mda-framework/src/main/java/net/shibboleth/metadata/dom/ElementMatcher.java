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
 * Functional interface representing matching an {@link Element}.
 *
 * @since 0.10.0
 */
@FunctionalInterface
public interface ElementMatcher {

    /**
     * Match an {@link Element} against specified criteria.
     *
     * @param input the {@link Element} to match
     * @return <code>true</code> if the {@link Element} matches
     */
    boolean match(@Nonnull Element input);

}
