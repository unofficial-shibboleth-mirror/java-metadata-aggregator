/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata;

import javax.annotation.concurrent.Immutable;

/** A type of {@link StatusMetadata} that indicates something is definitely wrong with the Item. */
@Immutable
public class ErrorStatus extends StatusMetadata {

    /**
     * Constructor.
     *
     * <p>
     * Note that the parameters must not be either <code>null</code>
     * or the empty string. However, as they are often set from
     * theoretically nullable sources such as a a bean's identifier
     * (which is not statically known to be non-null until after
     * initialization) or a <code>toString</code> method result,
     * this is not not included in the parameter annotations.
     * </p>
     * 
     * <p>
     * Instead, nullness is checked as a run-time
     * constraint resulting in a <code>ConstraintViolation</code>.
     * </p>
     * 
     * @param componentId ID of the component creating the status message, never null or empty
     * @param statusMessage the status message, never null or empty
     */
    public ErrorStatus(final String componentId, final String statusMessage) {
        super(componentId, statusMessage);
    }
}
