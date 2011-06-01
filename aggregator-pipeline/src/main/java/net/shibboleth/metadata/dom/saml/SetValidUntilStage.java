/*
 * Licensed to the University Corporation for Advanced Internet Development, Inc.
 * under one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache 
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
import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.opensaml.util.Assert;
import org.opensaml.util.xml.AttributeSupport;
import org.w3c.dom.Element;

/** Sets a validUntil attribute for every EntityDescriptor and EntitiesDescriptor element in the collection. */
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
     * @param duration amount of time the descriptors will be valid, expressed in milliseconds, must be greater than 0
     */
    public synchronized void setValidityDuration(long duration) {
        if (isInitialized()) {
            return;
        }
        Assert.isGreaterThan(0, duration, "Validity duration must be greater than 0");
        validityDuration = duration;
    }

    /** {@inheritDoc} */
    protected boolean doExecute(DomElementItem item) throws StageProcessingException {
        Element descriptor = item.unwrap();
        if (MetadataHelper.isEntitiesDescriptor(descriptor) || MetadataHelper.isEntityDescriptor(descriptor)) {
            AttributeSupport.removeAttribute(descriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME);
            AttributeSupport.appendDateTimeAttribute(descriptor, MetadataHelper.VALID_UNTIL_ATTIB_NAME,
                    System.currentTimeMillis() + validityDuration);
        }

        return true;
    }
}