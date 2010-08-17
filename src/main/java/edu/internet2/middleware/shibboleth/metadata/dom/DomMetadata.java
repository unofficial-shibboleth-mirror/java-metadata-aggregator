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

import org.w3c.dom.Element;

import edu.internet2.middleware.shibboleth.metadata.core.AbstractMetadata;
import edu.internet2.middleware.shibboleth.metadata.core.Metadata;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataInfo;
import edu.internet2.middleware.shibboleth.metadata.util.MetadataInfoHelper;

/** A metadata element whose data is a DOM, version 3, {@link Element}. */
public class DomMetadata extends AbstractMetadata<Element> {

    /** Serial version UID. */
    private static final long serialVersionUID = -6308292147361514467L;

    /**
     * Constructor.
     * 
     * @param metadata DOM metadata element
     */
    public DomMetadata(Element metadata) {
        super();
        setMetadata(metadata);
    }

    /** {@inheritDoc} */
    public Metadata<Element> copy() {
        Element domClone = (Element) getMetadata().cloneNode(true);
        DomMetadata clone = new DomMetadata(domClone);
        MetadataInfoHelper.addToAll(clone, getMetadataInfo().values().toArray(new MetadataInfo[] {}));
        return clone;
    }
}