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

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.AbstractMetadata;
import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.MetadataInfo;
import net.shibboleth.metadata.util.MetadataInfoHelper;

import org.w3c.dom.Element;


/** A metadata element whose data is a DOM, version 3, {@link Element}. */
@ThreadSafe
public class DomMetadata extends AbstractMetadata<Element> {

    /** Serial version UID. */
    private static final long serialVersionUID = 8677951541031584666L;

    /**
     * Constructor.
     * 
     * @param metadata DOM metadata element
     */
    public DomMetadata(final Element metadata) {
        super();
        setMetadata(metadata);
    }

    /** {@inheritDoc} */
    public Metadata<Element> copy() {
        final Element domClone = (Element) getMetadata().cloneNode(true);
        final DomMetadata clone = new DomMetadata(domClone);
        MetadataInfoHelper.addToAll(clone, getMetadataInfo().values().toArray(new MetadataInfo[] {}));
        return clone;
    }
}