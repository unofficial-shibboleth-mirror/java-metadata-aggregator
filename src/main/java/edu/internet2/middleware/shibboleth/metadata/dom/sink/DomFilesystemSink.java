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

package edu.internet2.middleware.shibboleth.metadata.dom.sink;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.opensaml.util.xml.Serialize;

import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.sink.PipelineSinkException;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.sink.Sink;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadataElement;

/** A pipeline {@link Sink} which writes an {@link org.w3c.dom.Element} to a file. */
public class DomFilesystemSink implements Sink<DomMetadataElement> {

    /** File to which resultant XML is written. */
    private File xmlFile;

    /**
     * Constructor.
     * 
     * @param filePath path to file to which resultant XML is written
     */
    public DomFilesystemSink(String filePath) {
        xmlFile = new File(filePath);
    }

    /**
     * Constructor.
     * 
     * @param file file to which resultant XML is written
     */
    public DomFilesystemSink(File file) {
        xmlFile = file;
    }

    /** {@inheritDoc} */
    public void execute(Map<String, Object> parameters, MetadataElementCollection<DomMetadataElement> metadata)
            throws PipelineSinkException {
        DomMetadataElement metadataElement = metadata.iterator().next();
        try {
            FileOutputStream out = new FileOutputStream(xmlFile);
            Serialize.writeNode(metadataElement.getEntityMetadata(), out);
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new PipelineSinkException("Unable to write XML to file", e);
        }
    }
}