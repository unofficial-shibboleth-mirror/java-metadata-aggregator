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

import java.util.ArrayList;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** {@link EntitiesDescriptorDisassemblerStage} unit test. */
public class EntitiesDescriptorDisassemblerStageTest extends BaseDOMTest {

    @Test
    public void test() throws Exception {
        Element entitiesDescriptor = readXmlData("samlMetadata/entitiesDescriptor1.xml");

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(new DOMElementItem(entitiesDescriptor));

        EntitiesDescriptorDisassemblerStage stage = new EntitiesDescriptorDisassemblerStage();
        stage.setId("foo");
        stage.initialize();

        stage.execute(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 3);
    }
}