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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * A type of {@link StatusMetadata} that carries informational messages. These messages should never be used to carry
 * status messages that would indicate an error or failing of the Item in some way.
 */
@Immutable
public class InfoStatus extends StatusMetadata {

    /**
     * Constructor.
     * 
     * @param componentId ID of the component creating the status message, never null or empty
     * @param statusMessage the status message, never null or empty
     */
    public InfoStatus(@Nonnull @NotEmpty final String componentId, @Nonnull @NotEmpty final String statusMessage) {
        super(componentId, statusMessage);
    }
}
