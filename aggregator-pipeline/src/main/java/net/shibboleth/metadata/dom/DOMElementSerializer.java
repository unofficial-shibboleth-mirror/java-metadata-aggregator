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

import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Very simple serializer that serializes the document element of the given {@link Element}-based {@link Item}.
 */
@ThreadSafe
public class DOMElementSerializer implements ItemSerializer<Element> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DOMElementSerializer.class);

    /** {@inheritDoc} */
    @Override
    public void serialize(@Nonnull final Item<Element> item, @Nonnull OutputStream output) {
        if (item == null) {
            return;
        }

        final Element documentRoot = item.unwrap();

        try {
            TransformerFactory tfac = TransformerFactory.newInstance();
            Transformer serializer = tfac.newTransformer();
            serializer.setOutputProperty("encoding", "UTF-8");
            serializer.transform(new DOMSource(documentRoot.getOwnerDocument()), new StreamResult(output));
        } catch (TransformerException e) {
            log.error("Unable to write out XML", e);
        }
    }

}