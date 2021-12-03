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

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemSerializer;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * A stage which writes the given item collection out to multiple destinations.
 * 
 * The destination for each item is determined by a strategy function, and the
 * serialisation is performed using a provided {@link ItemSerializer}.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>outputStrategy</code></li>
 * <li><code>serializer</code></li>
 * </ul>
 * 
 * @param <T> type of items upon which this stage operates
 *
 * @since 0.9.2
 */
@ThreadSafe
public class MultiOutputSerializationStage<T> extends AbstractIteratingStage<T> {

    /**
     * Interface implemented by destination objects created by an {@link OutputStrategy}.
     * 
     * The {@link Destination} must be closed after the associated {@link OutputStream}. This
     * extra level of processing allows the {@link Destination} to perform other tasks
     * after the stream is closed without having to subclass or proxy the stream
     * class.
     */
    public interface Destination extends Closeable {
        
        /**
         * Gets an {@link OutputStream} to which an item should be serialized.
         * 
         * @return output stream to which to serialize the item
         * @throws IOException if there is an error creating the output stream
         */
        @Nonnull OutputStream getOutputStream() throws IOException;
    }

    /**
     * Interface to be implemented by strategy object determining where
     * an item should be written.
     *
     * @param <T> type of items on which this strategy operates
     */
    public interface OutputStrategy<T> {
        
        /**
         * Gets a {@link Destination} to which an item should be serialized.
         *
         * @param item {@link Item} for which to generate a {@link Destination}
         * @return {@link Destination} for the item
         * 
         * @throws StageProcessingException if an output stream cannot be generated for
         *  the {@link Item}
         */
        @Nonnull Destination getDestination(@Nonnull final Item<T> item)
            throws StageProcessingException;

    }

    /** Strategy used to determine where to serialize the item. */
    @NonnullAfterInit @GuardedBy("this")
    private OutputStrategy<T> outputStrategy;

    /** Serializer used to write the collection to the output stream. */
    @NonnullAfterInit @GuardedBy("this")
    private ItemSerializer<T> serializer;

    /**
     * Gets the output strategy function determining where an item will be written.
     * 
     * @return the output strategy function
     */
    @Nullable public final synchronized OutputStrategy<T> getOutputStrategy() {
        return outputStrategy;
    }

    /**
     * Sets the output strategy function determining where an item will be written.
     * 
     * @param strategy output strategy function determining where an item will be written
     */
    public synchronized void setOutputStrategy(@Nonnull final OutputStrategy<T> strategy) {
        throwSetterPreconditionExceptions();
        outputStrategy = Constraint.isNotNull(strategy, "Output strategy can not be null");
    }

    /**
     * Gets the serializer used to write item to the output file.
     * 
     * @return serializer used to write item to the output file
     */
    @Nullable public final synchronized ItemSerializer<T> getSerializer() {
        return serializer;
    }

    /**
     * Sets the serializer used to write item to the output file.
     * 
     * @param itemSerializer serializer used to write item to the output file
     */
    public synchronized void setSerializer(@Nonnull final ItemSerializer<T> itemSerializer) {
        throwSetterPreconditionExceptions();
        serializer = Constraint.isNotNull(itemSerializer, "Item serializer can not be null");
    }

    @Override
    protected void doExecute(@Nonnull final Item<T> item)
            throws StageProcessingException {
        try (final Destination destination = getOutputStrategy().getDestination(item);
                final OutputStream stream = destination.getOutputStream()) {
            getSerializer().serialize(item, stream);
        } catch (final IOException e) {
            throw new StageProcessingException("Error writing to output location", e);
        }
    }

    @Override
    protected void doDestroy() {
        outputStrategy = null;
        serializer = null;

        super.doDestroy();
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (outputStrategy == null) {
            throw new ComponentInitializationException("Output strategy can not be null");
        }

        if (serializer == null) {
            throw new ComponentInitializationException("Item collection serializer can not be null");
        }

    }
}
