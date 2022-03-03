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

package net.shibboleth.metadata.dom;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.Live;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.xml.ParserPool;

/**
 * A stage which reads XML information from the filesystem and places it in the given {@link DOMElementItem} collection.
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>parserPool</code></li>
 * <li><code>source</code></li>
 * </ul>
 */
@ThreadSafe
public class DOMFilesystemSourceStage extends AbstractStage<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DOMFilesystemSourceStage.class);

    /** Pool of DOM parsers used to parse the XML file in to a DOM. */
    @NonnullAfterInit @GuardedBy("this")
    private ParserPool parserPool;

    /** The file path to the DOM material provided by this source. May be a file or a directory. */
    @NonnullAfterInit @GuardedBy("this")
    private File sourceFile;

    /**
     * Filter used to determine if a file should be included. This is only used if the {@link #sourceFile} is a
     * directory.
     */
    @Nullable @GuardedBy("this")
    private FileFilter sourceFileFilter;

    /**
     * Whether or not directories are recursed if the given input file is a directory. Default value: <code>false</code>
     */
    @GuardedBy("this")
    private boolean recurseDirectories;

    /** Whether the lack of source files is treated as an error. Default value: <code>false</code> */
    @GuardedBy("this")
    private boolean noSourceFilesAnError;

    /**
     * Whether an error parsing one source file causes this entire {@link net.shibboleth.metadata.pipeline.Stage} to
     * fail, or just excludes the material from the offending source file. Default value: <code>true</code>
     */
    @GuardedBy("this")
    private boolean errorCausesSourceFailure = true;

    /**
     * Gets the pool of DOM parsers used to parse the XML file in to a DOM.
     * 
     * @return pool of DOM parsers used to parse the XML file in to a DOM
     */
    @Nullable public final synchronized ParserPool getParserPool() {
        return parserPool;
    }

    /**
     * Sets the pool of DOM parsers used to parse the XML file in to a DOM.
     * 
     * @param pool pool of DOM parsers used to parse the XML file in to a DOM
     */
    public synchronized void setParserPool(@Nonnull final ParserPool pool) {
        throwSetterPreconditionExceptions();
        parserPool = Constraint.isNotNull(pool, "Parser pool can not be null");
    }

    /**
     * Gets the path to the DOM material provided by this source. May be a file or a directory.
     * 
     * @return path to the DOM material provided by this source
     */
    @Nullable public final synchronized File getSource() {
        return sourceFile;
    }

    /**
     * Sets the path to the DOM material provided by this source. May be a file or a directory.
     * 
     * @param source path to the DOM material provided by this source
     */
    public synchronized void setSource(@Nonnull final File source) {
        throwSetterPreconditionExceptions();
        sourceFile = Constraint.isNotNull(source, "XML source file can not be null");
    }

    /**
     * Gets the filter used to determine if a file, in a directory, should be treated as a source file. If no filter is
     * set then all files are used as source files. If the source file is not a directory this filter is meaningless.
     * 
     * @return filter used to determine if a file, in a directory, should be treated as a source file, may be null
     */
    @Nullable public final synchronized FileFilter getSourceFileFilter() {
        return sourceFileFilter;
    }

    /**
     * Sets the filter used to determine if a file, in a directory, should be treated as a source file.
     * 
     * @param filter filter used to determine if a file, in a directory, should be treated as a source file, may be null
     */
    public synchronized void setSourceFileFilter(@Nonnull final FileFilter filter) {
        throwSetterPreconditionExceptions();
        sourceFileFilter = Constraint.isNotNull(filter, "File filter can not be null");
    }

    /**
     * Gets whether directories will be recursively searched for XML input files.
     * 
     * @return whether directories will be recursively searched for XML input files
     */
    public final synchronized boolean getRecurseDirectories() {
        return recurseDirectories;
    }

    /**
     * Sets whether directories will be recursively searched for XML input files.
     * 
     * @param recurse whether directories will be recursively searched for XML input files
     */
    public synchronized void setRecurseDirectories(final boolean recurse) {
        throwSetterPreconditionExceptions();
        recurseDirectories = recurse;
    }

    /**
     * Get whether the lack of source files is considered an error.
     * 
     * @return whether the lack of source files is considered an error
     */
    public final synchronized boolean isNoSourceFilesAnError() {
        return noSourceFilesAnError;
    }

    /**
     * Sets whether the lack of source files is considered an error.
     * 
     * @param isError whether the lack of source files is considered an error
     */
    public synchronized void setNoSourceFilesAnError(final boolean isError) {
        throwSetterPreconditionExceptions();
        noSourceFilesAnError = isError;
    }

    /**
     * Gets whether an error parsing a single file causes the source to fail. If not, the parsed file is simply ignored.
     * 
     * @return whether an error parsing a single file causes the source to fail
     */
    public final synchronized boolean getErrorCausesSourceFailure() {
        return errorCausesSourceFailure;
    }

    /**
     * Sets whether an error parsing a single file causes the source to fail. If not, the parsed file is simply ignored.
     * 
     * @param causesFailure whether an error parsing a single file causes the source to fail
     */
    public synchronized void setErrorCausesSourceFailure(final boolean causesFailure) {
        throwSetterPreconditionExceptions();
        errorCausesSourceFailure = causesFailure;
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<Element>> items)
            throws StageProcessingException {
        final ArrayList<File> sourceFiles = new ArrayList<>();

        final var sFile = getSource();
        if (sFile.isFile()) {
            sourceFiles.add(sFile);
        } else {
            getSourceFiles(sFile, sourceFiles);
        }

        if (sourceFiles.isEmpty()) {
            if (!isNoSourceFilesAnError()) {
                log.warn("stage {}: no input XML files in source path {}", getId(), sFile.getPath());
                return;
            }
            throw new StageProcessingException("stage " + getId() + ": no source file was available for parsing");
        }

        for (final File source : sourceFiles) {
            final var dme = processSourceFile(source);
            if (dme != null) {
                items.add(dme);
            }
        }
    }

    /**
     * Gets the source files from a given input. If the input is an XML file, it's added to the collector. If the input
     * file is a directory, all of its XML files are added to the collector. If the directory contains other directories
     * and {@link #recurseDirectories} is true, then this process is repeated for each child direcrory.
     * 
     * @param input the source input file, never null
     * @param collector the collector of XML input files
     */
    protected void getSourceFiles(@Nonnull final File input,
            @Nonnull @NonnullElements @Live final List<File> collector) {

        if (input.isFile()) {
            final var filter = getSourceFileFilter();
            if (filter == null || filter.accept(input)) {
                collector.add(input);
            }
            return;
        }

        // file must be a directory
        final File[] files = input.listFiles();
        if (files != null) {
            final var recursing = getRecurseDirectories();
            for (final File file : files) {
                if (file.isFile() || (file.isDirectory() && recursing)) {
                    getSourceFiles(file, collector);
                }
            }
        }
    }

    /**
     * Reads in an XML source file, parses it, and creates the appropriate {@link DOMElementItem} for the data.
     * 
     * @param source XML file to read in
     * 
     * @return the resultant {@link DOMElementItem}, may be <code>null</code> if there was an error parsing the data and
     *         {@link #errorCausesSourceFailure} is false
     * 
     * @throws StageProcessingException thrown if there is a problem reading in the Element and
     *             {@link #errorCausesSourceFailure} is true
     */
    @Nullable
    protected DOMElementItem processSourceFile(@Nonnull final File source) throws StageProcessingException {
        log.debug("{} pipeline source parsing XML file {}", getId(), source.getPath());
        try (FileInputStream xmlIn = new FileInputStream(source)) {
            final Document doc = getParserPool().parse(xmlIn);
            return new DOMElementItem(doc);
        } catch (final Exception e) {
            if (getErrorCausesSourceFailure()) {
                throw new StageProcessingException(getId() + " pipeline source unable to parse XML input file "
                        + source.getPath(), e);
            }
            log.warn("{} pipeline source: unable to parse XML source file {}, ignoring it bad file", new Object[] {
                    getId(), source.getPath(), e,});
            return null;
        }
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (parserPool == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", ParserPool may not be null");
        }

        if (sourceFile == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId() + ", Source may not be null");
        }

        if (noSourceFilesAnError && (!sourceFile.exists() || !sourceFile.canRead())) {
            throw new ComponentInitializationException("Unable to initialize " + getId() + ", source file/directory "
                    + sourceFile.getPath() + " can not be read");
        }
    }
}
