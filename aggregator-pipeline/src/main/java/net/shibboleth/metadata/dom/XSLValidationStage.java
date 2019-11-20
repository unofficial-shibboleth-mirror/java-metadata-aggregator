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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;

/**
 * A pipeline which "validates" each element in the {@link DOMElementItem} collection via an XSL stylesheet. The results
 * of the transform are discarded but the source element receives {@link net.shibboleth.metadata.InfoStatus},
 * {@link net.shibboleth.metadata.WarningStatus}, and {@link net.shibboleth.metadata.ErrorStatus} metadata via the
 * {@link AbstractXSLProcessingStage.StatusInfoAppendingErrorListener}.
 */
@ThreadSafe
public class XSLValidationStage extends AbstractXSLProcessingStage {

    /** {@inheritDoc} */
    @Override protected void executeTransformer(@Nonnull final Transformer transformer,
            @Nonnull @NonnullElements final Collection<Item<Element>> itemCollection) throws StageProcessingException,
            TransformerConfigurationException {

        try {
            for (final Item<Element> domItem : itemCollection) {
                transformer.setErrorListener(new StatusInfoAppendingErrorListener(domItem));
                transformer.transform(new DOMSource(domItem.unwrap().getOwnerDocument()), new DOMResult());
            }
        } catch (final TransformerException e) {
            throw new StageProcessingException("Unable to validate DOM Element", e);
        }
    }
}