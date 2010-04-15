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
import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Document;

import edu.internet2.middleware.shibboleth.metadata.core.BasicMetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.source.PipelineSourceException;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.source.Source;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadataElement;

/**
 * A source which reads XML information from the filesystem and optionally caches it in memory.
 * 
 * When caching is enabled the collection of metadata produced is always a clone of the DOM element that is cached.
 */
public class DomFilesystemSource extends AbstractComponent implements Source<DomMetadataElement> {

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

    /** {@inheritDoc} */
    public MetadataElementCollection<DomMetadataElement> execute(Map<String, Object> parameters)
            throws PipelineSourceException {
        try {
            FileInputStream xmlIn = new FileInputStream(xmlFile);
            Document doc = parser.parse(xmlIn);
            xmlIn.close();

            BasicMetadataElementCollection<DomMetadataElement> mec = new BasicMetadataElementCollection<DomMetadataElement>();
            mec.add(new DomMetadataElement(doc.getDocumentElement()));
            return mec;

        } catch (IOException e) {
            throw new PipelineSourceException("Unable to read XML input file", e);
        } catch (XMLParserException e) {
            throw new PipelineSourceException("Unable to parse XML input file", e);
        }
    }
}