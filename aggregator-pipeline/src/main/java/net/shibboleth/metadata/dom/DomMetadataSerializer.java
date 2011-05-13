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

package net.shibboleth.metadata.dom;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.MetadataSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/** Very simple serializer that serializes the first element of the given metadata collection. */
@ThreadSafe
public class DomMetadataSerializer implements MetadataSerializer<DomMetadata> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DomMetadataSerializer.class);

    /** {@inheritDoc} */
    public void serialize(final Collection<DomMetadata> metadataCollection, OutputStream output) {
        if (metadataCollection == null || metadataCollection.isEmpty()) {
            return;
        }

        final Element documentRoot = metadataCollection.iterator().next().getMetadata();

        try {
            TransformerFactory tfac = TransformerFactory.newInstance();
            Transformer serializer = tfac.newTransformer();
            serializer.setOutputProperty("encoding", "UTF-8");
            serializer.transform(new DOMSource(documentRoot.getOwnerDocument()), new StreamResult(output));
        } catch (TransformerException e) {
            log.error("Unable to write out XML", e);
        }

        try {
            output.flush();
            output.close();
        } catch (IOException e) {
            log.error("Unable to close output stream", e);
        }
    }
}