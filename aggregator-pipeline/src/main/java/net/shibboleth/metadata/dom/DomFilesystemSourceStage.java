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
import java.util.Collection;
import java.util.List;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.opensaml.util.CloseableSupport;
import org.opensaml.util.xml.ParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * A stage which reads XML information from the filesystem and places it in the given {@link DomElementItem} collection.
 */
@ThreadSafe
public class DomFilesystemSourceStage extends BaseStage<DomElementItem> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DomFilesystemSourceStage.class);

    /** Pool of DOM parsers used to parse the XML file in to a DOM. */
    private ParserPool parserPool;

    /** The file path to the DOM material provided by this source. May be a file or a directory. */
    private File sourceFile;

    /**
     * Filter used to determine if a file should be included. This is only used if the {@link #sourceFile} is a
     * directory.
     */
    private FileFilter sourceFileFilter;

    /** Whether or not directories are recursed if the given input file is a directory. Default value: {@value} */
    private boolean recurseDirectories;

    /** Whether the lack of source files is treated as an error. Default value: {@value} */
    private boolean noSourceFilesAnError;

    /**
     * Whether an error parsing one source file causes this entire {@link net.shibboleth.metadata.pipeline.Stage} to
     * fail, or just excludes the material from the offending source file. Default value: {@value}
     */
    private boolean errorCausesSourceFailure = true;

    /**
     * Gets the pool of DOM parsers used to parse the XML file in to a DOM.
     * 
     * @return pool of DOM parsers used to parse the XML file in to a DOM
     */
    public ParserPool getParserPool() {
        return parserPool;
    }

    /**
     * Sets the pool of DOM parsers used to parse the XML file in to a DOM.
     * 
     * @param pool pool of DOM parsers used to parse the XML file in to a DOM
     */
    public synchronized void setParserPool(final ParserPool pool) {
        if (isInitialized()) {
            return;
        }
        parserPool = pool;
    }

    /**
     * Gets the path to the DOM material provided by this source. May be a file or a directory.
     * 
     * @return path to the DOM material provided by this source
     */
    public File getSource() {
        return sourceFile;
    }

    /**
     * Sets the path to the DOM material provided by this source. May be a file or a directory.
     * 
     * @param source path to the DOM material provided by this source
     */
    public synchronized void setSource(final File source) {
        if (isInitialized()) {
            return;
        }
        sourceFile = source;
    }

    /**
     * Gets the filter used to determine if a file, in a directory, should be treated as a source file. If no filter is
     * set then all files are used as source files. If the source file is not a directory this filter is meaningless.
     * 
     * @return filter used to determine if a file, in a directory, should be treated as a source file, may be null
     */
    public FileFilter getSourceFileFilter() {
        return sourceFileFilter;
    }

    /**
     * Sets the filter used to determine if a file, in a directory, should be treated as a source file.
     * 
     * @param filter filter used to determine if a file, in a directory, should be treated as a source file, may be null
     */
    public synchronized void setSourceFileFilter(FileFilter filter) {
        if (isInitialized()) {
            return;
        }
        sourceFileFilter = filter;
    }

    /**
     * Gets whether directories will be recursively searched for XML input files.
     * 
     * @return whether directories will be recursively searched for XML input files
     */
    public boolean getRecurseDirectories() {
        return recurseDirectories;
    }

    /**
     * Sets whether directories will be recursively searched for XML input files.
     * 
     * @param recurse whether directories will be recursively searched for XML input files
     */
    public synchronized void setRecurseDirectories(final boolean recurse) {
        if (isInitialized()) {
            return;
        }
        recurseDirectories = recurse;
    }

    /**
     * Get whether the lack of source files is considered an error.
     * 
     * @return whether the lack of source files is considered an error
     */
    public boolean isNoSourceFilesAnError() {
        return noSourceFilesAnError;
    }

    /**
     * Sets whether the lack of source files is considered an error.
     * 
     * @param isError whether the lack of source files is considered an error
     */
    public synchronized void setNoSourceFilesAnError(boolean isError) {
        if (isInitialized()) {
            return;
        }
        noSourceFilesAnError = isError;
    }

    /**
     * Gets whether an error parsing a single file causes the source to fail. If not, the parsed file is simply ignored.
     * 
     * @return whether an error parsing a single file causes the source to fail
     */
    public boolean getErrorCausesSourceFailure() {
        return errorCausesSourceFailure;
    }

    /**
     * Sets whether an error parsing a single file causes the source to fail. If not, the parsed file is simply ignored.
     * 
     * @param causesFailure whether an error parsing a single file causes the source to fail
     */
    public synchronized void setErrorCausesSourceFailure(final boolean causesFailure) {
        if (isInitialized()) {
            return;
        }
        errorCausesSourceFailure = causesFailure;
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<DomElementItem> itemCollection) throws StageProcessingException {
        final ArrayList<File> sourceFiles = new ArrayList<File>();
        if (sourceFile.isFile()) {
            sourceFiles.add(sourceFile);
        } else {
            getSourceFiles(sourceFile, sourceFiles);
        }

        if (sourceFiles.isEmpty()) {
            if (!noSourceFilesAnError) {
                log.warn("stage {}: no input XML files in source path {}", getId(), sourceFile.getPath());
                return;
            } else {
                throw new StageProcessingException("stage " + getId() + ": no source file was available for parsing");
            }
        }

        DomElementItem dme;
        for (File source : sourceFiles) {
            dme = processSourceFile(source);
            if (dme != null) {
                itemCollection.add(dme);
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
    protected void getSourceFiles(final File input, final List<File> collector) {
        if (input.isFile()) {
            if (sourceFileFilter == null || sourceFileFilter.accept(input)) {
                collector.add(input);
            }
            return;
        }

        // file must be a directory
        final File[] files = sourceFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() || (file.isDirectory() && recurseDirectories)) {
                    getSourceFiles(file, collector);
                }
            }
        }
    }

    /**
     * Reads in an XML source file, parses it, and creates the appropriate {@link DomElementItem} for the data.
     * 
     * @param source XML file to read in
     * 
     * @return the resultant Element Itme, may be null if there was an error parsing the data and
     *         {@link #errorCausesSourceFailure} is false
     * 
     * @throws StageProcessingException thrown if there is a problem reading in the Element and
     *             {@link #errorCausesSourceFailure} is true
     */
    protected DomElementItem processSourceFile(final File source) throws StageProcessingException {
        FileInputStream xmlIn = null;

        try {
            log.debug("{} pipeline source parsing XML file {}", getId(), source.getPath());
            xmlIn = new FileInputStream(source);
            final Document doc = parserPool.parse(xmlIn);
            return new DomElementItem(doc);
        } catch (Exception e) {
            if (errorCausesSourceFailure) {
                throw new StageProcessingException(getId() + " pipeline source unable to parse XML input file "
                        + source.getPath(), e);
            } else {
                log.warn("{} pipeline source: unable to parse XML source file {}, ignoring it bad file", new Object[] {
                        getId(), source.getPath(), e, });
                return null;
            }
        } finally {
            CloseableSupport.closeQuietly(xmlIn);
        }
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        if (parserPool == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", ParserPool may not be null");
        }

        if (sourceFile == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId() + ", Source may not be null");
        }

        if (!sourceFile.exists() || !sourceFile.canRead()) {
            throw new ComponentInitializationException("Unable to initialize " + getId() + ", source file/directory "
                    + sourceFile.getPath() + " can not be read");
        }
    }
}