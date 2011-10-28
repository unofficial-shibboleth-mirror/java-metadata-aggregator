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

package net.shibboleth.metadata.dom.saml;

import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;
import net.shibboleth.metadata.pipeline.ComponentInitializationException;
import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.opensaml.util.xml.AttributeSupport;
import org.w3c.dom.Element;

/** 
 * Sets a validUntil attribute for every EntityDescriptor and EntitiesDescriptor element in the collection. 
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>validityDuration</code></li>
 * </ul> 
 */
public class SetValidUntilStage extends BaseIteratingStage<DomElementItem> {

    /** Amount of time the descriptors will be valid, expressed in milliseconds. */
    private long validityDuration;

    /**
     * Gets the amount of time the descriptors will be valid, expressed in milliseconds.
     * 
     * @return amount of time the descriptors will be valid, expressed in milliseconds
     */
    public long getValidityDuration() {
        return validityDuration;
    }

    /**
     * Sets the amount of time the descriptors will be valid, expressed in milliseconds.
     * 
     * @param duration amount of time the descriptors will be valid, expressed in milliseconds
     */
    public synchronized void setValidityDuration(long duration) {
        if (isInitialized()) {
            return;
        }
        validityDuration = duration;
    }

    /** {@inheritDoc} */
    protected boolean doExecute(DomElementItem item) throws StageProcessingException {
        Element descriptor = item.unwrap();
        if (SamlMetadataSupport.isEntitiesDescriptor(descriptor) || SamlMetadataSupport.isEntityDescriptor(descriptor)) {
            AttributeSupport.removeAttribute(descriptor, SamlMetadataSupport.VALID_UNTIL_ATTIB_NAME);
            AttributeSupport.appendDateTimeAttribute(descriptor, SamlMetadataSupport.VALID_UNTIL_ATTIB_NAME,
                    System.currentTimeMillis() + validityDuration);
        }

        return true;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (validityDuration <= 0) {
            throw new ComponentInitializationException("Validity duration must be greater than 0");
        }
    }
}