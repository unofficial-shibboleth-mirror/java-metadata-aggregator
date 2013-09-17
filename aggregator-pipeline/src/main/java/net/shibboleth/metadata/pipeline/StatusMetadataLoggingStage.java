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
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.StatusMetadata;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Stage} that logs {@link StatusMetadata} associated with an {@link Item}.
 */
@ThreadSafe
public class StatusMetadataLoggingStage extends AbstractItemMetadataSelectionStage {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(StatusMetadataLoggingStage.class);

    /** {@inheritDoc} */
    protected void doExecute(
                    @Nonnull @NonnullElements final Collection<Item<?>> itemCollection,
                    @Nonnull final Item<?> matchingItem,
                    @Nonnull @NonnullElements final Map<Class<? extends ItemMetadata>,
                        List<? extends ItemMetadata>> matchingMetadata)
                    throws StageProcessingException {

        final String itemId = getItemIdentifierStrategy().getItemIdentifier(matchingItem);

        logInfos(itemId, (List<StatusMetadata>) matchingMetadata.get(InfoStatus.class));
        logWarnings(itemId, (List<StatusMetadata>) matchingMetadata.get(WarningStatus.class));
        logErrors(itemId, (List<StatusMetadata>) matchingMetadata.get(ErrorStatus.class));
    }

    /**
     * Logs info messages.
     * 
     * @param itemId ID of the item
     * @param statuses status messages to log
     */
    private void logInfos(@Nonnull final String itemId, @Nonnull @NonnullElements List<StatusMetadata> statuses) {
        if (statuses != null && !statuses.isEmpty() && log.isInfoEnabled()) {
            log.info("Item {} was marked with the following Info status messages", itemId);
            for (StatusMetadata status : statuses) {
                log.info("    {} reported: {}", status.getComponentId(), status.getStatusMessage());
            }
        }
    }

    /**
     * Logs warning messages.
     * 
     * @param itemId ID of the item
     * @param statuses status messages to log
     */
    private void logWarnings(@Nonnull final String itemId, @Nonnull @NonnullElements List<StatusMetadata> statuses) {
        if (statuses != null && !statuses.isEmpty() && log.isWarnEnabled()) {
            log.warn("Item {} was marked with the following Warning status messages", itemId);
            for (StatusMetadata status : statuses) {
                log.warn("    {} reported: {}", status.getComponentId(), status.getStatusMessage());
            }
        }
    }

    /**
     * Logs error messages.
     * 
     * @param itemId ID of the item
     * @param statuses status messages to log
     */
    private void logErrors(@Nonnull final String itemId, @Nonnull @NonnullElements List<StatusMetadata> statuses) {
        if (statuses != null && !statuses.isEmpty() && log.isErrorEnabled()) {
            log.error("Item {} was marked with the following Error status messages", itemId);
            for (StatusMetadata status : statuses) {
                log.error("    {} reported: {}", status.getComponentId(), status.getStatusMessage());
            }
        }
    }
}