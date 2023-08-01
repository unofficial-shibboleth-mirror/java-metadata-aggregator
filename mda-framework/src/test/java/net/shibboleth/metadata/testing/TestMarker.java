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

package net.shibboleth.metadata.testing;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.shared.annotation.constraint.NotEmpty;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.primitive.StringSupport;

/** A {@link ItemMetadata} that can be used as a marker in tests. */
@Immutable
public class TestMarker implements ItemMetadata {

    @Nonnull @NotEmpty private final String marker;

    /**
     * Constructor.
     * 
     * @param mark a marker for an item, must not be either null or empty
     */
    public TestMarker(@Nonnull @NotEmpty final String mark) {
        marker = Constraint.isNotNull(StringSupport.trimOrNull(mark), "marker may not be null or empty");
    }

    /**
     * Gets the tag for the item.
     * 
     * @return tag for the item, never null or empty
     */
    @Nonnull @NotEmpty public String getMarker() {
        return marker;
    }
}
