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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemCollectionSerializer;
import net.shibboleth.shared.annotation.constraint.NonnullAfterInit;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.logic.Constraint;

/**
 * A stage which writes the given item collection out to a file using a provided
 * {@link ItemCollectionSerializer}.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>outputFile</code></li>
 * <li><code>serializer</code></li>
 * </ul>
 * 
 * @param <T> type of items upon which this stage operates
 */
@ThreadSafe
public class SerializationStage<T> extends AbstractStage<T> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(SerializationStage.class);

    /** File to which the item will be written. */
    @NonnullAfterInit @GuardedBy("this")
    private File outputFile;

    /** Whether an existing output file should be overwritten. Default value: <code>true</code> */
    @GuardedBy("this") private boolean overwritingExistingOutputFile = true;

    /** Serializer used to write the collection to the output stream. */
    @NonnullAfterInit @GuardedBy("this")
    private ItemCollectionSerializer<T> serializer;

    /**
     * Gets the file to which the item will be written.
     * 
     * @return file to which the item will be written
     */
    @NonnullAfterInit public final synchronized File getOutputFile() {
        return outputFile;
    }

    /**
     * Sets the file to which the item will be written.
     * 
     * @param file file to which the item will be written
     */
    public synchronized void setOutputFile(@Nonnull final File file) {
        checkSetterPreconditions();
        outputFile = Constraint.isNotNull(file, "Output file can not be null");
    }

    /**
     * Gets whether an existing output file should be overwritten.
     * 
     * @return whether an existing output file should be overwritten
     */
    public final synchronized boolean isOverwritingExistingOutputFile() {
        return overwritingExistingOutputFile;
    }

    /**
     * Sets whether an existing output file should be overwritten.
     * 
     * @param isOverwriting whether an existing output file should be overwritten
     */
    public synchronized void setOverwritingExistingOutputFile(final boolean isOverwriting) {
        checkSetterPreconditions();
        overwritingExistingOutputFile = isOverwriting;
    }

    /**
     * Gets the serializer used to write item to the output file.
     * 
     * @return serializer used to write item to the output file
     */
    @Nullable public final synchronized ItemCollectionSerializer<T> getSerializer() {
        return serializer;
    }

    /**
     * Sets the serializer used to write item to the output file.
     * 
     * @param itemSerializer serializer used to write item to the output file
     */
    public synchronized void setSerializer(@Nonnull final ItemCollectionSerializer<T> itemSerializer) {
        checkSetterPreconditions();
        serializer = Constraint.isNotNull(itemSerializer, "Item collection serializer can not be null");
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws StageProcessingException {
        try (OutputStream stream = new FileOutputStream(getOutputFile())) {
            getSerializer().serializeCollection(items, stream);
        } catch (final IOException e) {
            throw new StageProcessingException("Error writing to output file " +
                    getOutputFile().getAbsolutePath(), e);
        }
    }

    @Override
    protected synchronized void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (outputFile == null) {
            throw new ComponentInitializationException("Output file can not be null");
        }

        // canWrite() returns false if the file doesn't exist already
        // we don't want to create the file if it doesn't exist so we check
        // to see if the parent directory can be written to
        if (outputFile.exists()) {
            if (!overwritingExistingOutputFile) {
                throw new ComponentInitializationException("Output file '" + outputFile.getAbsolutePath()
                        + "' exists and stage is configured not to overwrite the file");
            } else if (!outputFile.canWrite()) {
                throw new ComponentInitializationException("Can not write to output file '"
                        + outputFile.getAbsolutePath() + "'");

            }
        } else {
            final File parentDirectory = outputFile.getParentFile();
            if (parentDirectory != null) {
                if (!parentDirectory.canWrite()) {
                    throw new ComponentInitializationException("Can not write to parent directory of output file '"
                            + outputFile.getAbsolutePath() + "'");
                }
            } else {
                log.warn(
                        "Unable to determine parent directory for output file {}, " +
                            "this may result in a problem during stage execution",
                        outputFile.getAbsolutePath());
            }
        }

        if (serializer == null) {
            throw new ComponentInitializationException("Item collection serializer can not be null");
        }

    }

}
