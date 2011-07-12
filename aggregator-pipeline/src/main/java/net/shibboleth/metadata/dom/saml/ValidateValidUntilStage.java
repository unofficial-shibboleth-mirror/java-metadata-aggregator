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

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.opensaml.util.Assert;
import org.opensaml.util.xml.AttributeSupport;
import org.w3c.dom.Element;

/**
 * Checks that a SAML EntitiesDescriptor or EntityDescriptor's validUntil is (optionally) present and is within a given
 * range. Items which are not a SAML EntitiesDescriptor or EntityDescriptor are ignored.
 */
public class ValidateValidUntilStage extends BaseIteratingStage<DomElementItem> {

    /** Whether the item is required to have a validUntil attribute. Default value: true */
    private boolean requireValidUntil = true;

    /** Interval, in milliseconds, from now within which the validUntil date must fall. Default value: 1 week */
    private long maxValidityInterval = 1000 * 60 * 60 * 24 * 7;

    /**
     * Gets whether the item is required to have a validUntil attribute.
     * 
     * @return whether the item is required to have a validUntil attribute
     */
    public boolean isRequireValidUntil() {
        return requireValidUntil;
    }

    /**
     * Sets whether the item is required to have a validUntil attribute.
     * 
     * @param isRequired whether the item is required to have a validUntil attribute
     */
    public synchronized void setRequireValidUntil(boolean isRequired) {
        if (isInitialized()) {
            return;
        }

        requireValidUntil = isRequired;
    }

    /**
     * Gets the interval, in milliseconds, from now within which the validUntil date must fall.
     * 
     * @return Interval, in milliseconds, from now within which the validUntil date must fall
     */
    public long getMaxValidityInterval() {
        return maxValidityInterval;
    }

    /**
     * Sets the interval, in milliseconds, from now within which the validUntil date must fall. A value of 0 indicates
     * that there is no check on the upper bound of the validity period.
     * 
     * @param interval interval, in milliseconds, from now within which the validUntil date must fall; must be greater
     *            than or equal to 0
     */
    public synchronized void setMaxValidityInterval(long interval) {
        if (isInitialized()) {
            return;
        }

        Assert.isGreaterThanOrEqual(0, interval);
        maxValidityInterval = interval;
    }

    /** {@inheritDoc} */
    protected boolean doExecute(final DomElementItem item) throws StageProcessingException {
        final Element element = item.unwrap();

        if (!SamlMetadataSupport.isEntitiesDescriptor(element) && !SamlMetadataSupport.isEntityDescriptor(element)) {
            return true;
        }

        final Long validUntil =
                AttributeSupport.getDateTimeAttributeAsLong(AttributeSupport.getAttribute(element,
                        SamlMetadataSupport.VALID_UNTIL_ATTIB_NAME));
        if (validUntil == null) {
            if (requireValidUntil) {
                item.getItemMetadata().put(new ErrorStatus(getId(), "Item does not include a validUntil attribute"));
            }
        } else {
            final long lowerBound = System.currentTimeMillis();
            if (validUntil < lowerBound) {
                item.getItemMetadata().put(new ErrorStatus(getId(), "Item has a validUntil prior to the current time"));
            }

            if (maxValidityInterval > 0) {
                final long upperBound = lowerBound + maxValidityInterval;
                if (validUntil > upperBound) {
                    item.getItemMetadata().put(
                            new ErrorStatus(getId(), "Item has validUntil larger than the maximum validity interval"));
                }
            }
        }

        return true;
    }
}