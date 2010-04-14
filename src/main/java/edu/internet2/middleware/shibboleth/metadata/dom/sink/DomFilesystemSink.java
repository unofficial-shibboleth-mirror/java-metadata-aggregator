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
import java.io.OutputStream;
import java.util.Map;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSSerializerFilter;

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
            writeNode(metadataElement.getEntityMetadata(), out);
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new PipelineSinkException("Unable to write XML to file", e);
        }
    }

    /**
     * Writes a Node out to a Writer using the DOM, level 3, Load/Save serializer. The written content is encoded using
     * the encoding specified in the writer configuration.
     * 
     * @param node the node to write out
     * @param output the output stream to write the XML to
     */
    protected void writeNode(Node node, OutputStream output) {
        DOMImplementation domImpl;
        if (node instanceof Document) {
            domImpl = ((Document) node).getImplementation();
        } else {
            domImpl = node.getOwnerDocument().getImplementation();
        }

        DOMImplementationLS domImplLS = (DOMImplementationLS) domImpl.getFeature("LS", "3.0");
        LSSerializer serializer = domImplLS.createLSSerializer();
        serializer.setFilter(new LSSerializerFilter() {

            public short acceptNode(Node arg0) {
                return FILTER_ACCEPT;
            }

            public int getWhatToShow() {
                return SHOW_ALL;
            }
        });

        LSOutput serializerOut = domImplLS.createLSOutput();
        serializerOut.setByteStream(output);

        serializer.write(node, serializerOut);
    }
}