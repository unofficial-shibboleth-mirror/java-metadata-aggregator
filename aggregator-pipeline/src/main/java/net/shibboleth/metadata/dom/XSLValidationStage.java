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

import java.util.Collection;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.pipeline.StageProcessingException;

/**
 * A pipeline stage which applies and XSLT to each element in the {@link DomElementItem} collection. The element that
 * served as the source of the XSLT is replaced by the elements the result from the transform.
 */
@ThreadSafe
public class XSLValidationStage extends AbstractXSLProcessingStage {

    /** {@inheritDoc} */
    protected void executeTransformer(Transformer transformer, Collection<DomElementItem> itemCollection)
            throws StageProcessingException, TransformerConfigurationException {

        try {
            for (DomElementItem domItem : itemCollection) {
                transformer.setErrorListener(new StatusInfoAppendingErrorListener(domItem));
                transformer.transform(new DOMSource(domItem.unwrap()), new DOMResult());
            }
        } catch (TransformerException e) {
            throw new StageProcessingException("Unable to validate DOM Element", e);
        }
    }
}