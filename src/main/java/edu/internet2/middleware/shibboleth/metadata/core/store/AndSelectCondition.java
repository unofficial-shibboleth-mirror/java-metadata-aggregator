/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.shibboleth.metadata.core.store;

import net.jcip.annotations.ThreadSafe;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataElement;

/**
 * A {@link SelectCondition} whose result is the boolean AND its operands. If no conditions are given the result of this
 * condition is false.
 */
@ThreadSafe
public class AndSelectCondition implements SelectCondition<MetadataElement<?>> {

    /** {@link SelectCondition} operands which are AND'ed. */
    private SelectCondition<MetadataElement<?>>[] operandConditions;

    /**
     * Constructor.
     * 
     * @param conditions conditions which will be AND'ed together
     */
    public AndSelectCondition(SelectCondition<MetadataElement<?>>... conditions) {
        operandConditions = conditions;
    }

    /** {@inheritDoc} */
    public boolean isSelected(MetadataElement<?> element) {
        if (operandConditions.length == 0) {
            return false;
        }

        for (SelectCondition<MetadataElement<?>> condition : operandConditions) {
            if (!condition.isSelected(element)) {
                return false;
            }
        }

        return true;
    }
}