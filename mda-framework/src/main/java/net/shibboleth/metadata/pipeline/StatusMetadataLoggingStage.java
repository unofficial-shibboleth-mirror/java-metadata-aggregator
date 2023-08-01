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

package net.shibboleth.metadata.pipeline;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.StatusMetadata;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.collection.ClassToInstanceMultiMap;
import net.shibboleth.shared.primitive.LoggerFactory;

/**
 * A {@link Stage} that logs {@link StatusMetadata} associated with an {@link Item}.
 * 
 * @param <T> type of item which this stage processes
 */
@ThreadSafe
public class StatusMetadataLoggingStage<T> extends AbstractItemMetadataSelectionStage<T, StatusMetadata> {

    /** Class logger. */
    private static final @Nonnull Logger LOG = LoggerFactory.getLogger(StatusMetadataLoggingStage.class);

    @Override
    protected void doExecute(
            @Nonnull @NonnullElements final List<Item<T>> items,
            @Nonnull final Item<T> matchingItem,
            @Nonnull @NonnullElements final ClassToInstanceMultiMap<StatusMetadata> matchingMetadata)
                    throws StageProcessingException {

        final String itemId = getItemIdentificationStrategy().getItemIdentifier(matchingItem);

        logInfos(itemId, matchingMetadata.get(InfoStatus.class));
        logWarnings(itemId, matchingMetadata.get(WarningStatus.class));
        logErrors(itemId, matchingMetadata.get(ErrorStatus.class));
    }

    /**
     * Logs info messages.
     * 
     * @param itemId ID of the item
     * @param statuses status messages to log
     */
    private void logInfos(@Nonnull final String itemId,
            @Nonnull @NonnullElements final List<InfoStatus> statuses) {
        if (!statuses.isEmpty() && LOG.isInfoEnabled()) {
            LOG.info("Item {} was marked with the following Info status messages", itemId);
            for (final var status : statuses) {
                LOG.info("    {}: {}", status.getComponentId(), status.getStatusMessage());
            }
        }
    }

    /**
     * Logs warning messages.
     * 
     * @param itemId ID of the item
     * @param statuses status messages to log
     */
    private void logWarnings(@Nonnull final String itemId,
            @Nonnull @NonnullElements final List<WarningStatus> statuses) {
        if (!statuses.isEmpty() && LOG.isWarnEnabled()) {
            LOG.warn("Item {} was marked with the following Warning status messages", itemId);
            for (final var status : statuses) {
                LOG.warn("    {}: {}", status.getComponentId(), status.getStatusMessage());
            }
        }
    }

    /**
     * Logs error messages.
     * 
     * @param itemId ID of the item
     * @param statuses status messages to log
     */
    private void logErrors(@Nonnull final String itemId,
            @Nonnull @NonnullElements final List<ErrorStatus> statuses) {
        if (!statuses.isEmpty() && LOG.isErrorEnabled()) {
            LOG.error("Item {} was marked with the following Error status messages", itemId);
            for (final var status : statuses) {
                LOG.error("    {}: {}", status.getComponentId(), status.getStatusMessage());
            }
        }
    }
}
