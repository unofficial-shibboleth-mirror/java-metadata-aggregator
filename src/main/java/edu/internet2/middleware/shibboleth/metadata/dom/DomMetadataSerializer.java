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

package edu.internet2.middleware.shibboleth.metadata.dom;

import java.io.OutputStream;

import net.jcip.annotations.ThreadSafe;

import org.opensaml.util.xml.SerializeSupport;
import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.MetadataSerializer;

/** Very simple serializer that serializes the first element of the given metadata collection. */
@ThreadSafe
public class DomMetadataSerializer implements MetadataSerializer<DomMetadata> {

    /** {@inheritDoc} */
    public void serialize(final MetadataCollection<DomMetadata> metadataCollection, OutputStream output) {
        if (metadataCollection == null || metadataCollection.isEmpty()) {
            return;
        }

        final Element documentRoot = metadataCollection.iterator().next().getMetadata();
        SerializeSupport.writeNode(documentRoot, output);
    }
}