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

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.xml.AttributeSupport;

/**
 * Checks that a SAML EntitiesDescriptor or EntityDescriptor's validUntil is (optionally) present and is within a given
 * range. Items which are not a SAML EntitiesDescriptor or EntityDescriptor are ignored.
 */
@ThreadSafe
public class ValidateValidUntilStage extends AbstractIteratingStage<Element> {

    /** Whether the item is required to have a validUntil attribute. Default value: <code>true</code> */
    @GuardedBy("this") private boolean requireValidUntil = true;

    /**
     * Interval from now within which the validUntil date must fall. A value of 0 indicates that no
     * maximum interval is checked. Default value: 1 week
     */
    @GuardedBy("this")
    private @Nonnull Duration maxValidityInterval;

    /**
     * Constructor.
     */
    public ValidateValidUntilStage() {
        final var dur = Duration.ofDays(7);
        assert dur != null;
        maxValidityInterval = dur;
    }

    /**
     * Gets whether the item is required to have a validUntil attribute.
     * 
     * @return whether the item is required to have a validUntil attribute
     */
    public final synchronized boolean isRequireValidUntil() {
        return requireValidUntil;
    }

    /**
     * Sets whether the item is required to have a validUntil attribute.
     * 
     * @param isRequired whether the item is required to have a validUntil attribute
     */
    public synchronized void setRequireValidUntil(final boolean isRequired) {
        checkSetterPreconditions();
        requireValidUntil = isRequired;
    }

    /**
     * Gets the interval from now within which the validUntil date must fall.
     * 
     * @return Interval from now within which the validUntil date must fall
     */
    @Nonnull
    public final synchronized Duration getMaxValidityInterval() {
        return maxValidityInterval;
    }

    /**
     * Sets the interval from now within which the validUntil date must fall. A value of 0 indicates
     * that there is no check on the upper bound of the validity period.
     * 
     * @param interval interval from now within which the validUntil date must fall; must be greater
     *            than or equal to 0
     */
    public synchronized void setMaxValidityInterval(@Nonnull final Duration interval) {
        checkSetterPreconditions();

        Constraint.isNotNull(interval, "max validity interval can not be null");
        Constraint.isFalse(interval.isNegative(), "max validity interval can not be negative");

        maxValidityInterval = interval;
    }

    @Override
    protected void doExecute(@Nonnull final Item<Element> item) throws StageProcessingException {
        final Element element = item.unwrap();

        if (!SAMLMetadataSupport.isEntitiesDescriptor(element)) {
            return;
        }

        /*
         * Nothing to do if there is no validUntil attribute.
         */
        final @Nullable Attr attr = AttributeSupport.getAttribute(element,
                SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME);
        if (attr == null) {
            if (isRequireValidUntil()) {
                item.getItemMetadata().put(new ErrorStatus(getId(), "Item does not include a validUntil attribute"));
            }
            return;
        }

        final Instant validUntil = AttributeSupport.getDateTimeAttribute(attr);
        if (validUntil == null) {
            if (isRequireValidUntil()) {
                item.getItemMetadata().put(new ErrorStatus(getId(), "Item does not include a validUntil attribute"));
            }
        } else {
            final var lowerBound = Instant.now();
            if (validUntil.isBefore(lowerBound)) {
                item.getItemMetadata().put(new ErrorStatus(getId(), "Item has a validUntil prior to the current time"));
            }

            if (!getMaxValidityInterval().isZero()) {
                final var upperBound = lowerBound.plus(getMaxValidityInterval());
                if (validUntil.isAfter(upperBound)) {
                    item.getItemMetadata().put(
                            new ErrorStatus(getId(), "Item has validUntil larger than the maximum validity interval"));
                }
            }
        }
    }
}
