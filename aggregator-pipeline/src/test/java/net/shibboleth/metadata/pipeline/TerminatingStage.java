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

package net.shibboleth.metadata.pipeline;

import java.util.Collection;

import net.shibboleth.metadata.Item;

/**
 * Stage that throws a {@link TerminationException} when it is called.
 * 
 * @param <T> type of {@link Item} being processed
 */
class TerminatingStage<T> extends AbstractStage<T> {

    /** Constructor. */
    public TerminatingStage() {
        setId("TerminatingStage");
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(Collection<Item<T>> itemCollection) throws StageProcessingException {
        throw new TerminationException("from TerminatingStage");
    }
}
