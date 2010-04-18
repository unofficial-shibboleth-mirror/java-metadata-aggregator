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

    /** The XML file parsed in to the DOM. */
    private File xmlFile;

    /**
     * Constructor.
     * 
     * @param sourceId unique ID of this source
     * @param parserPool pool of parsers used to parse the xml file
     * @param filePath filesystem path to an XML metadata file
     */
    public DomFilesystemSource(String sourceId, ParserPool parserPool, String filePath) {
        super(sourceId);
        parser = parserPool;
        xmlFile = new File(filePath);
    }

    /**
     * Constructor.
     * 
     * @param sourceId unique ID of this source
     * @param parserPool pool of parsers used to parse the xml file
     * @param file an XML metadata file
     */
    public DomFilesystemSource(String sourceId, ParserPool parserPool, File file) {
        super(sourceId);
        parser = parserPool;
        xmlFile = file;
    }

    /**
     * Gets the filesystem path to the XML file.
     * 
     * @return filesystem path to the XML file
     */
    public String getXmlFile() {
        return xmlFile.getPath();
    }

    /** {@inheritDoc} */
    public MetadataElementCollection<DomMetadataElement> execute(Map<String, Object> parameters)
            throws PipelineSourceException {
        FileInputStream xmlIn = null;

        try {
            log.debug("{} pipeline source parsing XML file {}", getId(), xmlFile.getPath());
            xmlIn = new FileInputStream(xmlFile);
            Document doc = parser.parse(xmlIn);

            BasicMetadataElementCollection<DomMetadataElement> mec = new BasicMetadataElementCollection<DomMetadataElement>();
            mec.add(new DomMetadataElement(doc.getDocumentElement()));
            return mec;
        } catch (Exception e) {
            String errMsg = MessageFormatter.format("{} pipeline source unable to parse XML input file {}", getId(),
                    xmlFile.getPath());
            log.error(errMsg, e);
            throw new PipelineSourceException(errMsg, e);
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

        if (!xmlFile.exists() || !xmlFile.canRead()) {
            log.error("Unable to initialize " + getId() + ", XML file " + xmlFile.getPath() + " can not be read");
            throw new PipelineInitializationException("Unable to read XML file " + xmlFile.getPath());
        }
    }
}