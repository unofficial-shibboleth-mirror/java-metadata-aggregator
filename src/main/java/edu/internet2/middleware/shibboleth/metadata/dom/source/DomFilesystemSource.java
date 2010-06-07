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

package edu.internet2.middleware.shibboleth.metadata.dom.source;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.ThreadSafe;

import org.opensaml.util.Closeables;
import org.opensaml.util.xml.ParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.w3c.dom.Document;

import edu.internet2.middleware.shibboleth.metadata.core.BasicMetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.PipelineInitializationException;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.source.PipelineSourceException;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.source.Source;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadataElement;

/**
 * A source which reads XML information from the filesystem and optionally caches it in memory.
 * 
 * When caching is enabled the collection of metadata produced is always a clone of the DOM element that is cached.
 */
@ThreadSafe
public class DomFilesystemSource extends AbstractComponent implements Source<DomMetadataElement> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DomFilesystemSource.class);

    /** Pool of DOM parsers used to parse the XML file in to a DOM. */
    private ParserPool parser;

    /** The file path to the DOM material provided by this source. May be a file or a directory. */
    private File sourceFile;

    /** Whether or not directories are recursed if the given input file is a directory. */
    private boolean recurseDirectories;

    /**
     * Whether an error parsing one source file causes this entire {@link Source} to fail, or just excludes the material
     * from the offending source file.
     */
    private boolean errorCausesSourceFailure;

    /**
     * Constructor.
     * 
     * @param sourceId unique ID of this source
     * @param parserPool pool of parsers used to parse the xml file
     * @param sourcePath filesystem path to DOM material provided by this source, may be a file or directory, may not be
     *            null
     */
    public DomFilesystemSource(String sourceId, ParserPool parserPool, String sourcePath) {
        this(sourceId, parserPool, new File(sourcePath));
    }

    /**
     * Constructor.
     * 
     * @param sourceId unique ID of this source
     * @param parserPool pool of parsers used to parse the xml file
     * @param source DOM material provided by this source, may be a file or directory, may not be null
     */
    public DomFilesystemSource(String sourceId, ParserPool parserPool, File source) {
        super(sourceId);
        parser = parserPool;
        sourceFile = source;
        recurseDirectories = false;
        errorCausesSourceFailure = true;
    }

    /**
     * Gets the filesystem path to the DOM material provided by this source.
     * 
     * @return filesystem path to the DOM material provided by this source
     */
    public String getSourcePath() {
        return sourceFile.getPath();
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
    public void setRecurseDirectories(boolean recurse) {
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
    public void setErrorCausesSourceFailure(boolean causesFailure) {
        errorCausesSourceFailure = causesFailure;
    }

    /** {@inheritDoc} */
    public MetadataElementCollection<DomMetadataElement> execute(Map<String, Object> parameters)
            throws PipelineSourceException {

        BasicMetadataElementCollection<DomMetadataElement> mec = new BasicMetadataElementCollection<DomMetadataElement>();

        ArrayList<File> sourceFiles = new ArrayList<File>();
        getSourceFiles(sourceFile, sourceFiles);

        if (sourceFiles.isEmpty()) {
            log.debug("{} pipeline source: no input XML files in source path {}", getId(), sourceFile.getPath());
            return mec;
        }

        DomMetadataElement dme;
        for (File source : sourceFiles) {
            dme = processSourceFile(source);
            if (dme != null) {
                mec.add(dme);
            }
        }

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
    protected void getSourceFiles(File input, List<File> collector) {
        if (input.isFile()) {
            if (isXmlFile(input)) {
                collector.add(input);
            }
            return;
        }

        // file must be a directory
        File[] files = sourceFile.listFiles();
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
    protected boolean isXmlFile(File file) {
        return file.getName().endsWith(".xml");
    }

    /**
     * Reads in an XML source file, parses it, and creates the appropriate {@link DomMetadataElement} for the data.
     * 
     * @param source XML file to read in
     * 
     * @return the resultant metadata element, may be null if there was an error parsing the data and
     *         {@link #errorCausesSourceFailure} is false
     * 
     * @throws PipelineSourceException thrown if there is a problem reading in the metadata and
     *             {@link #errorCausesSourceFailure} is true
     */
    protected DomMetadataElement processSourceFile(File source) throws PipelineSourceException {
        FileInputStream xmlIn = null;

        try {
            log.debug("{} pipeline source parsing XML file {}", getId(), source.getPath());
            xmlIn = new FileInputStream(source);
            Document doc = parser.parse(xmlIn);
            return new DomMetadataElement(doc.getDocumentElement());
        } catch (Exception e) {
            if (errorCausesSourceFailure) {
                String errMsg = MessageFormatter.format("{} pipeline source unable to parse XML input file {}",
                        getId(), source.getPath());
                log.error(errMsg, e);
                throw new PipelineSourceException(errMsg, e);
            } else {
                log.warn(MessageFormatter.format(
                        "{} pipeline source: unable to parse XML source file {}, ignoring it bad file", getId(), source
                                .getPath()), e);
                return null;
            }
        } finally {
            Closeables.closeQuiety(xmlIn);
        }
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws PipelineInitializationException {
        if (parser == null) {
            log.error("Unable to initialize " + getId() + ", parser pool may not be null");
            throw new PipelineInitializationException("ParserPool may not be null");
        }

        if (!sourceFile.exists() || !sourceFile.canRead()) {
            log.error("Unable to initialize " + getId() + ", source file/directory " + sourceFile.getPath()
                    + " can not be read");
            throw new PipelineInitializationException("Unable to read source file/directory " + sourceFile.getPath());
        }
    }
}