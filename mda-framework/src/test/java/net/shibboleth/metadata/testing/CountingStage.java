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

/**
 * Stage that counts the number of times it was invoked and the number of items it has observed.
 * 
 * @param <T> type of {@link Item} being processed
 */
public class CountingStage<T> extends AbstractStage<T> {

    /** Number of times the stage was invoked. */
    private int invocationCount;

    /** Number of items observed. */
    private int itemCount;

    /** Constructor. */
    public CountingStage() {
        setId("CountingStage");
    }

    /**
     * Gets the number of times the stage was invoked.
     * 
     * @return number of times the stage was invoked
     */
    public int getInvocationCount() {
        return invocationCount;
    }

    /**
     * Gets the number of items observed.
     * 
     * @return number of items observed
     */
    public int getItemCount() {
        return itemCount;
    }

    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final List<Item<T>> metadataCollection) throws StageProcessingException {
        invocationCount += 1;
        itemCount += metadataCollection.size();
    }

}
