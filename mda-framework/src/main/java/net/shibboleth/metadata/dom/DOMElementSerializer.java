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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemCollectionSerializer;
import net.shibboleth.metadata.ItemSerializer;
import net.shibboleth.shared.annotation.constraint.NonnullElements;
import net.shibboleth.shared.primitive.LoggerFactory;

import org.slf4j.Logger;
import org.w3c.dom.Element;

/**
 * Very simple {@link ItemSerializer} that serializes the document element of the given
 * {@link Element}-based {@link Item}.
 * 
 * When used as an {@link ItemCollectionSerializer}, just serializes the first {@link Item} in the collection.
 * This will result in well-formed XML, but any other items in the collection will simply be ignored.
 */
@ThreadSafe
public class DOMElementSerializer implements ItemSerializer<Element>, ItemCollectionSerializer<Element> {

    /** Class logger. */
    private static final @Nonnull Logger LOG = LoggerFactory.getLogger(DOMElementSerializer.class);

    @Override
    public void serialize(@Nonnull final Item<Element> item, @Nonnull final OutputStream output)
        throws IOException {

        final Element documentRoot = item.unwrap();

        try {
            final TransformerFactory tfac = TransformerFactory.newInstance();
            final Transformer serializer = tfac.newTransformer();
            serializer.setOutputProperty("encoding", "UTF-8");
            serializer.transform(new DOMSource(documentRoot.getOwnerDocument()), new StreamResult(output));
        } catch (final TransformerException e) {
            LOG.error("Unable to write out XML", e);
            throw new IOException(e);
        }
    }

    @Override
    public void serializeCollection(@Nonnull @NonnullElements final Collection<Item<Element>> items,
            @Nonnull final OutputStream output) throws IOException {
        final Iterator<Item<Element>> iter = items.iterator();
        if (iter.hasNext()) {
            serialize(iter.next(), output);
            if (iter.hasNext()) {
                LOG.warn("collection contained more than one Item; rest ignored");
            }
        } else {
            LOG.warn("collection was empty");
        }
    }

}
