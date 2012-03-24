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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class ElementStrippingStageTest extends BaseDomTest {

    @Test
    public void doExecute() throws Exception {
        final Element doc = readXmlData("elementStripIn.xml");
        final DomElementItem item = new DomElementItem(doc);
        final List<DomElementItem> items = new ArrayList<DomElementItem>();
        items.add(item);
        
        final ElementStrippingStage stage = new ElementStrippingStage();
        stage.setId("stripTest");
        stage.setElementNamespace("urn:namespace:beta");
        stage.setElementName("StripMe");
        stage.initialize();
        
        stage.execute(items);
        
        final Element out = readXmlData("elementStripOut.xml");
        assertXmlIdentical(out, item.unwrap());
    }
}
