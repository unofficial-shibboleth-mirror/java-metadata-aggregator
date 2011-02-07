/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.dom.source;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.SimpleMetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.AbstractComponent;
import net.shibboleth.metadata.pipeline.ComponentInfo;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.Source;
import net.shibboleth.metadata.pipeline.SourceProcessingException;

import org.opensaml.util.CloseableSupport;
import org.opensaml.util.xml.ParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * A source which reads XML information from the filesystem and optionally caches it in memory.
 * 
 * When caching is enabled the collection of metadata produced is always a clone of the DOM element that is cached.
 */
@ThreadSafe
public class DomFilesystemSource extends AbstractComponent implements Source<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DomFilesystemSource.class);

    /** Pool of DOM parsers used to parse the XML file in to a DOM. */
    private ParserPool parserPool;

    /** The file path to the DOM material provided by this source. May be a file or a directory. */
    private File sourceFile;

    /** Whether or not directories are recursed if the given input file is a directory. */
    private boolean recurseDirectories;

    /**
     * Whether an error parsing one source file causes this entire {@link Source} to fail, or just excludes the material
     * from the offending source file.
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
    public MetadataCollection<DomMetadata> execute() throws SourceProcessingException {
        final ComponentInfo compInfo = new ComponentInfo(this);

        final SimpleMetadataCollection<DomMetadata> mec = new SimpleMetadataCollection<DomMetadata>();

        final ArrayList<File> sourceFiles = new ArrayList<File>();
        getSourceFiles(sourceFile, sourceFiles);

        if (sourceFiles.isEmpty()) {
            log.debug("{} pipeline source: no input XML files in source path {}", getId(), sourceFile.getPath());
            return mec;
        }

        DomMetadata dme;
        for (File source : sourceFiles) {
            dme = processSourceFile(source);
            if (dme != null) {
                dme.getMetadataInfo().put(compInfo);
                mec.add(dme);
            }
        }

        compInfo.setCompleteInstant();
        return mec;
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
            if (isXmlFile(input)) {
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
     * Checks to see if the file is an XML file. Default implementation simply checks to see if the file ends in '.xml'.
     * 
     * @param file file to check
     * 
     * @return true of the file is an XML file, false if not
     */
    protected boolean isXmlFile(final File file) {
        return file.getName().endsWith(".xml");
    }

    /**
     * Reads in an XML source file, parses it, and creates the appropriate {@link DomMetadata} for the data.
     * 
     * @param source XML file to read in
     * 
     * @return the resultant metadata element, may be null if there was an error parsing the data and
     *         {@link #errorCausesSourceFailure} is false
     * 
     * @throws SourceProcessingException thrown if there is a problem reading in the metadata and
     *             {@link #errorCausesSourceFailure} is true
     */
    protected DomMetadata processSourceFile(final File source) throws SourceProcessingException {
        FileInputStream xmlIn = null;

        try {
            log.debug("{} pipeline source parsing XML file {}", getId(), source.getPath());
            xmlIn = new FileInputStream(source);
            final Document doc = parserPool.parse(xmlIn);
            return new DomMetadata(doc.getDocumentElement());
        } catch (Exception e) {
            if (errorCausesSourceFailure) {
                throw new SourceProcessingException(getId() + " pipeline source unable to parse XML input file "
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