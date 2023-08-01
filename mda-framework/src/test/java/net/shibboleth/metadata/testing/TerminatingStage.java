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

import java.util.List;

import javax.annotation.Nonnull;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.pipeline.TerminationException;

/**
 * Stage that throws a {@link TerminationException} when it is called.
 * 
 * @param <T> type of {@link Item} being processed
 */
public class TerminatingStage<T> extends AbstractStage<T> {

    /** Constructor. */
    public TerminatingStage() {
        setId("TerminatingStage");
    }

    @Override
    protected void doExecute(@Nonnull List<Item<T>> items) throws StageProcessingException {
        throw new TerminationException("from TerminatingStage");
    }
}
