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
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.pipeline.MultiOutputSerializationStage.Destination;
import net.shibboleth.metadata.pipeline.impl.BaseInitializableComponent;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

/**
 * An output strategy for use with the {@link MultiOutputSerializationStage} which generates
 * individual files within a directory.
 * 
 * The files are named by a combination of:
 * 
 * <ul>
 * <li>an optional prefix string,
 * <li>each item's {@link ItemId} transformed by an optional {@link Function},
 * <li>an optional suffix string.
 * </ul>
 * 
 * @param <T> the type of {@link Item} to operate on
 *
 * @since 0.9.2
 */
@ThreadSafe
public class FilesInDirectoryMultiOutputStrategy<T> extends BaseInitializableComponent
    implements MultiOutputSerializationStage.OutputStrategy<T> {
    
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(FilesInDirectoryMultiOutputStrategy.class);

    /**
     * Class implementing the returned {@link MultiOutputSerializationStage.Destination} objects.
     */
    @Immutable
    private class FileDestination implements MultiOutputSerializationStage.Destination {

        /** The destination {@link File}. */
        @Nonnull private final File file;

        /**
         * Constructor.
         *
         * @param f the destination {@link File}
         */
        protected FileDestination(@Nonnull final File f) {
            file = f;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new FileOutputStream(file);
        }
        
    }
    /** String to use as a prefix for file names generated by this strategy. Default value: empty string. */
    @Nonnull @GuardedBy("this") private String namePrefix = "";
    
    /** {@link Function} to use to transform the {@link Item}'s {@link ItemId}. Default: identity transform. */
    @Nonnull @GuardedBy("this") private Function<String, String> nameTransformer = x -> x;

    /** String to use as a suffix for file names generated by this strategy. Default value: empty string. */
    @Nonnull @GuardedBy("this") private String nameSuffix = "";
    
    /** Directory into which to write files. */
    @NonnullAfterInit @GuardedBy("this") private File directory;

    /**
     * Gets the name prefix in use.
     * 
     * @return the name prefix in use
     */
    @Nonnull public final synchronized String getNamePrefix() {
        return namePrefix;
    }

    /**
     * Sets the name prefix to use.
     * 
     * @param prefix the name prefix to use
     */
    public synchronized void setNamePrefix(@Nonnull final String prefix) {
        throwSetterPreconditionExceptions();
        namePrefix = Constraint.isNotNull(prefix, "name prefix may not be null");
    }

    /**
     * Gets the name transformer in use.
     * 
     * @return the name transformer in use.
     */
    @Nonnull public final synchronized Function<String, String> getNameTransformer() {
        return nameTransformer;
    }

    /**
     * Sets the name transformer to use.
     * 
     * @param transformer the name transformer to use
     */
    public synchronized void setNameTransformer(@Nonnull final Function<String, String> transformer) {
        throwSetterPreconditionExceptions();
        nameTransformer = Constraint.isNotNull(transformer,
                "name transformer may not be null");
    }

    /**
     * Gets the name suffix in use.
     * 
     * @return the name suffix in use
     */
    @Nonnull public final synchronized String getNameSuffix() {
        return nameSuffix;
    }

    /**
     * Sets the name suffix to use.
     * 
     * @param suffix the name suffix to use
     */
    public synchronized void setNameSuffix(@Nonnull final String suffix) {
        throwSetterPreconditionExceptions();
        nameSuffix = Constraint.isNotNull(suffix, "name suffix may not be null");
    }

    /**
     * Gets the directory in use.
     * 
     * @return the directory in use
     */
    @Nonnull public final synchronized File getDirectory() {
        return directory;
    }

    /**
     * Sets the directory to use.
     * 
     * @param dir the directory to use
     */
    public synchronized void setDirectory(@Nonnull final File dir) {
        throwSetterPreconditionExceptions();
        directory = Constraint.isNotNull(dir, "directory may not be null");
    }

    @Override
    public Destination getDestination(final Item<T> item) throws StageProcessingException {
        // Locate the item's identifier.
        final List<ItemId> ids = item.getItemMetadata().get(ItemId.class);
        if (ids.isEmpty()) {
            throw new StageProcessingException("item has no ItemId to base a file name on");
        }
        final ItemId id = ids.get(0);

        // Construct file name
        final String name = getNamePrefix() + getNameTransformer().apply(id.getId()) + getNameSuffix();
        log.debug("id mapped {} -> {}", id.getId(), name);
        
        // Locate the output file within the directory
        final File outputFile = new File(getDirectory(), name);

        return new FileDestination(outputFile);
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (directory == null) {
            throw new ComponentInitializationException("directory can not be null");
        }

        // Check that the directory can be written to.
        if (!directory.canWrite()) {
            throw new ComponentInitializationException("Can not write to parent directory of output files");
        }

    }

}
