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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.pipeline.MultiOutputSerializationStage.Destination;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
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
 */
public class FilesInDirectoryMultiOutputStrategy<T> extends AbstractInitializableComponent
    implements MultiOutputSerializationStage.OutputStrategy<T> {
    
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(FilesInDirectoryMultiOutputStrategy.class);

    private class FileDestination implements MultiOutputSerializationStage.Destination {

        private final File file;

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
    @Nonnull private String namePrefix = "";
    
    /** {@link Function} to use to transform the {@link Item}'s {@link ItemId}. Default: identity transform. */
    @Nonnull private Function<String, String> nameTransformer = Functions.identity();

    /** String to use as a suffix for file names generated by this strategy. Default value: empty string. */
    @Nonnull private String nameSuffix = "";
    
    /** Directory into which to write files. */
    @NonnullAfterInit private File directory;

    /**
     * @return Returns the namePrefix.
     */
    @Nonnull public String getNamePrefix() {
        return namePrefix;
    }

    /**
     * @param namePrefix The namePrefix to set.
     */
    public void setNamePrefix(@Nonnull final String namePrefix) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        this.namePrefix = Constraint.isNotNull(namePrefix, "name prefix may not be null");
    }

    /**
     * @return Returns the nameTransformer.
     */
    @Nonnull public Function<String, String> getNameTransformer() {
        return nameTransformer;
    }

    /**
     * @param nameTransformer The nameTransformer to set.
     */
    public void setNameTransformer(@Nonnull final Function<String, String> nameTransformer) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        this.nameTransformer = Constraint.isNotNull(nameTransformer,
                "name transformer may not be null");
    }

    /**
     * @return Returns the nameSuffix.
     */
    @Nonnull public String getNameSuffix() {
        return nameSuffix;
    }

    /**
     * @param nameSuffix The nameSuffix to set.
     */
    public void setNameSuffix(@Nonnull final String nameSuffix) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        this.nameSuffix = Constraint.isNotNull(nameSuffix, "name suffix may not be null");
    }

    /**
     * @return Returns the directory.
     */
    @Nonnull public File getDirectory() {
        return directory;
    }

    /**
     * @param directory The directory to set.
     */
    public void setDirectory(@Nonnull final File directory) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        this.directory = Constraint.isNotNull(directory, "directory may not be null");
    }

    @Override
    public Destination getDestination(Item<T> item) throws StageProcessingException {
        // Locate the item's identifier.
        final List<ItemId> ids = item.getItemMetadata().get(ItemId.class);
        if (ids.isEmpty()) {
            throw new StageProcessingException("item has no ItemId to base a file name on");
        }
        final ItemId id = ids.get(0);

        // Construct file name
        final String name = namePrefix + nameTransformer.apply(id.getId()) + nameSuffix;
        log.debug("id mapped {} -> {}", id.getId(), name);
        
        // Locate the output file within the directory
        final File outputFile = new File(directory, name);

        return new FileDestination(outputFile);
    }

    @Override
    protected void doDestroy() {
        namePrefix = null;
        nameTransformer = null;
        nameSuffix = null;
        directory = null;

        super.doDestroy();
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
