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
import net.shibboleth.metadata.dom.BaseDomTest;
import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.utilities.java.support.xml.AttributeSupport;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** Unit test for {@link ValidateValidUntilStage}. */
public class ValidateValidUntilStageTest extends BaseDomTest {

    /** Tests that optional check flag on the stage operates properly. */
    @Test
    public void testOptionalCheck() throws Exception {
        ValidateValidUntilStage stage = new ValidateValidUntilStage();
        stage.setId("test");
        stage.setRequireValidUntil(false);
        stage.initialize();

        DomElementItem item = buildDomElementItem(0);
        stage.doExecute(item);
        Assert.assertFalse(item.getItemMetadata().containsKey(ErrorStatus.class));

        stage = new ValidateValidUntilStage();
        stage.setId("test");
        stage.setRequireValidUntil(true);
        stage.initialize();
        
        item = buildDomElementItem(0);
        stage.doExecute(item);
        Assert.assertTrue(item.getItemMetadata().containsKey(ErrorStatus.class));
    }

    /** Tests that Items within range are not marked as in error and Items outside range are marked as in error. */
    @Test
    public void testValidUntilRangeCheck() throws Exception {
        ValidateValidUntilStage stage = new ValidateValidUntilStage();
        stage.setId("test");
        stage.setRequireValidUntil(false);
        stage.initialize();

        DomElementItem item = buildDomElementItem(10000);
        stage.doExecute(item);
        Assert.assertFalse(item.getItemMetadata().containsKey(ErrorStatus.class));

        item = buildDomElementItem(-10000);
        stage.doExecute(item);
        Assert.assertTrue(item.getItemMetadata().containsKey(ErrorStatus.class));

        item = buildDomElementItem(1000 * 60 * 60 * 24 * 8);
        stage.doExecute(item);
        Assert.assertTrue(item.getItemMetadata().containsKey(ErrorStatus.class));
    }

    /**
     * Creates a {@link DomElementItem} that contains a validUntil attribute whose value is now plus a given interval.
     * 
     * @param validUntilInterval interval for the valid until, interval of 0 indicates not to include the validUntil
     *            attribute
     * 
     * @return the created Item
     */
    private DomElementItem buildDomElementItem(long validUntilInterval) throws Exception {
        Element descriptor = readXmlData("samlMetadata/entitiesDescriptor1.xml");
        if (validUntilInterval != 0) {
            AttributeSupport.appendDateTimeAttribute(descriptor, SamlMetadataSupport.VALID_UNTIL_ATTIB_NAME,
                    System.currentTimeMillis() + validUntilInterval);
        }else{
            AttributeSupport.removeAttribute(descriptor, SamlMetadataSupport.VALID_UNTIL_ATTIB_NAME);
        }
        return new DomElementItem(descriptor.getOwnerDocument());
    }
}