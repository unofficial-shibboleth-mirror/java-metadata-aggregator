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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.metadata.Item;

/**
 * Helper class for dealing with {@link Future} values.
 */
@ThreadSafe
public final class FutureSupport {

    /**
     * An implementation of {@link Future} that returns a value that is
     * already known.
     *
     * @param <T> type of "future" object to return
     */
    private static class FutureNow<T> implements Future<T> {
    
        /**
         * Value to be returned.
         */
        private final T value;
        
        /**
         * Constructor.
         *
         * @param t value to be returned
         */
        FutureNow(final T t) {
            value = t;
        }
        
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }
    
        @Override
        public boolean isCancelled() {
            return false;
        }
    
        @Override
        public boolean isDone() {
            return true;
        }
    
        @Override
        public T get() throws InterruptedException, ExecutionException {
            return value;
        }
    
        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            return value;
        }
        
    }

    /** Class logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FutureSupport.class);

    /** Constructor. */
    private FutureSupport() {

    }

    /**
     * Returns a {@link Future} containing an already computed value.
     * 
     * @param t value to be returned
     * @param <T> type of value to be returned
     * 
     * @return {@link Future} returning the passed value
     */
    @Nonnull
    public static <T> Future<T> futureNow(final T t) {
        return new FutureNow<>(t);
    }

    /**
     * Resolves the future value of a {@link Future} collection value.
     * @param future {@link Future} value to be resolved
     * @param <T> type of the items in the collection
     * @return resolved value of the {@link Future}
     * 
     * @throws StageProcessingException unless resolution is successful
     */
    @Nonnull public static <T> Collection<Item<T>> futureItems(
            @Nonnull Future<Collection<Item<T>>> future) throws StageProcessingException {
        assert future != null;
        try {
            final Collection<Item<T>> value = future.get();
            if (value == null) {
                throw new StageProcessingException("null returned from future value");
            }
            return value;
        } catch (ExecutionException e) {
            LOG.debug("Pipeline threw an unexpected exception", e);
            if (e.getCause() instanceof StageProcessingException) {
                // UN-wrap our own exceptions so as to propagate them
                throw (StageProcessingException) e.getCause();
            } else {
                // Wrap other exceptions
                throw new StageProcessingException("ExecutionException during processing", e);
            }
        } catch (InterruptedException e) {
            LOG.debug("Execution service was interrupted", e);
            throw new StageProcessingException("Execution service was interrupted", e);
        }
    }
    
}
