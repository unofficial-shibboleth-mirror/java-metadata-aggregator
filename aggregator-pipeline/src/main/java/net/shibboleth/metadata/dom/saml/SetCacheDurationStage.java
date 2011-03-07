/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
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

package net.shibboleth.metadata.dom.saml;

import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.dom.DomMetadata;
import net.shibboleth.metadata.pipeline.AbstractComponent;
import net.shibboleth.metadata.pipeline.ComponentInfo;
import net.shibboleth.metadata.pipeline.Stage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.util.MetadataInfoHelper;

import org.opensaml.util.Assert;
import org.opensaml.util.xml.AttributeSupport;
import org.w3c.dom.Element;

/** Sets a cacheDuration attribute for every EntityDescriptor and EntitiesDescriptor element in the collection. */
public class SetCacheDurationStage extends AbstractComponent implements Stage<DomMetadata> {

    /** Cache duration, in milliseconds, that will be set on each metadata element. */
    private long cacheDuration;

    /**
     * Gets the cache duration, in milliseconds, that will be set on each metadata element.
     * 
     * @return cache duration, in milliseconds
     */
    public long getCacheDuration() {
        return cacheDuration;
    }

    /**
     * Sets the cache duration, in milliseconds, that will be set on each metadata element.
     * 
     * @param duration cache duration, in milliseconds; must be greater than 0
     */
    public void setCacheDuration(long duration) {
        Assert.isGreaterThan(0, duration, "Cache duration must be greater than 0");
        cacheDuration = duration;
    }

    /** {@inheritDoc} */
    public MetadataCollection<DomMetadata> execute(MetadataCollection<DomMetadata> metadataCollection)
            throws StageProcessingException {
        final ComponentInfo compInfo = new ComponentInfo(this);

        Element descriptor;
        for (DomMetadata metadata : metadataCollection) {
            descriptor = metadata.getMetadata();
            if (MetadataHelper.isEntitiesDescriptor(descriptor) || MetadataHelper.isEntityDescriptor(descriptor)) {
                if (AttributeSupport.hasAttribute(descriptor, MetadataHelper.CACHE_DURATION_ATTRIB_NAME)) {
                    descriptor.removeAttributeNode(AttributeSupport.getAttribute(descriptor,
                            MetadataHelper.CACHE_DURATION_ATTRIB_NAME));
                }

                AttributeSupport.appendDurationAttribute(descriptor, MetadataHelper.CACHE_DURATION_ATTRIB_NAME,
                        cacheDuration);
            }
        }

        compInfo.setCompleteInstant();
        MetadataInfoHelper.addToAll(metadataCollection, compInfo);
        return metadataCollection;
    }
}