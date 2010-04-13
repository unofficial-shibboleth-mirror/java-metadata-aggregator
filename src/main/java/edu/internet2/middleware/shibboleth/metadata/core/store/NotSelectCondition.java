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

/** A {@link SelectCondition} whose result is the boolean NOT its operand. */
@ThreadSafe
public class NotSelectCondition implements SelectCondition<MetadataElement<?>> {

    /** {@link SelectCondition} operand which is NOT'ed. */
    private SelectCondition<MetadataElement<?>> operandCondition;

    /**
     * Constructor.
     * 
     * @param condition condition which will be NOT'ed, may not be null
     */
    public NotSelectCondition(SelectCondition<MetadataElement<?>> condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Operand condition may not be null");
        }
        operandCondition = condition;
    }

    /** {@inheritDoc} */
    public boolean isSelected(MetadataElement<?> element) {
        return !operandCondition.isSelected(element);
    }
}