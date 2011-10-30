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
import java.util.Collection;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemSerializer;

/**
 * A stage which writes the given item collection out to a file.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>outputFile</code></li>
 * <li><code>serializer</code></li>
 * </ul>
 * 
 * @param <ItemType> type of items upon which this stage operates
 */
public class SerializationStage<ItemType extends Item<?>> extends BaseStage<ItemType> {

    /** File to which the item will be written. */
    private File outputFile;

    /** Whether an existing output file should be overwritten. Default value: <code>true</code> */
    private boolean overwritingExistingOutputFile = true;

    /** Serializer used to write the item to the output stream. */
    private ItemSerializer<ItemType> serializer;

    /**
     * Gets the file to which the item will be written.
     * 
     * @return file to which the item will be written
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * Sets the file to which the item will be written.
     * 
     * @param file file to which the item will be written
     */
    public synchronized void setOutputFile(File file) {
        if (isInitialized()) {
            return;
        }

        outputFile = file;
    }

    /**
     * Gets whether an existing output file should be overwritten.
     * 
     * @return whether an existing output file should be overwritten
     */
    public boolean isOverwritingExistingOutputFile() {
        return overwritingExistingOutputFile;
    }

    /**
     * Sets whether an existing output file should be overwritten.
     * 
     * @param isOverwriting whether an existing output file should be overwritten
     */
    public synchronized void setOverwritingExistingOutputFile(boolean isOverwriting) {
        if (isInitialized()) {
            return;
        }

        overwritingExistingOutputFile = isOverwriting;
    }

    /**
     * Gets the serializer used to write item to the output file.
     * 
     * @return serializer used to write item to the output file
     */
    public ItemSerializer<ItemType> getSerializer() {
        return serializer;
    }

    /**
     * Sets the serializer used to write item to the output file.
     * 
     * @param itemSerializer serializer used to write item to the output file
     */
    public synchronized void setSerializer(ItemSerializer<ItemType> itemSerializer) {
        if (isInitialized()) {
            return;
        }

        serializer = itemSerializer;
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<ItemType> itemCollection) throws StageProcessingException {
        try {
            serializer.serialize(itemCollection, new FileOutputStream(outputFile));
        } catch (IOException e) {
            throw new StageProcessingException("Error write to output file " + outputFile.getAbsolutePath(), e);
        }
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (outputFile == null) {
            throw new ComponentInitializationException("Output file can not be null");
        }

        if (serializer == null) {
            throw new ComponentInitializationException("Item serializer can not be null");
        }

        if (outputFile.exists() && !overwritingExistingOutputFile) {
            throw new ComponentInitializationException("Output file " + outputFile.getAbsolutePath()
                    + " exist and stage is configured not to overwrite the file");
        }
    }
}