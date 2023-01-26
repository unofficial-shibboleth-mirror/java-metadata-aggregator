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


package net.shibboleth.metadata.dom.saml.mdrpi;

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.shared.collection.ClassToInstanceMultiMap;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.xml.XMLParserException;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class RegistrationAuthorityPopulationStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public RegistrationAuthorityPopulationStageTest() {
        super(RegistrationAuthorityPopulationStage.class);
    }

    private RegistrationAuthorityPopulationStage makeStage() throws ComponentInitializationException {
        final RegistrationAuthorityPopulationStage stage = new RegistrationAuthorityPopulationStage();
        stage.setId("test");
        stage.initialize();
        return stage; 
    }
    
    private DOMElementItem makeItem(final String which) throws XMLParserException {
        final Element doc = readXMLData(classRelativeResource(which + ".xml"));
        return new DOMElementItem(doc);
    }
    
    @Test
    public void populatePresent() throws Exception {
        final DOMElementItem item = makeItem("present");
        
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final RegistrationAuthorityPopulationStage stage = makeStage();
        
        stage.execute(items);
        
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        final List<ErrorStatus> errors = metadata.get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 0);
        
        final List<RegistrationAuthority> regAuths = metadata.get(RegistrationAuthority.class);
        Assert.assertEquals(regAuths.size(), 1);
        final RegistrationAuthority regAuth = regAuths.get(0);
        Assert.assertEquals(regAuth.getRegistrationAuthority(), "http://ukfederation.org.uk");
    }
    
    @Test
    public void populateAbsent() throws Exception  {
        final DOMElementItem item = makeItem("absent");
        
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final RegistrationAuthorityPopulationStage stage = makeStage();
        
        stage.execute(items);
        
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        final List<ErrorStatus> errors = metadata.get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 0);
        
        final List<RegistrationAuthority> regAuths = metadata.get(RegistrationAuthority.class);
        Assert.assertEquals(regAuths.size(), 0);
    }
    
    @Test
    public void populateNoExtensions() throws Exception  {
        final DOMElementItem item = makeItem("noext");
        
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final RegistrationAuthorityPopulationStage stage = makeStage();
        
        stage.execute(items);
        
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        final List<ErrorStatus> errors = metadata.get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 0);
        
        final List<RegistrationAuthority> regAuths = metadata.get(RegistrationAuthority.class);
        Assert.assertEquals(regAuths.size(), 0);
    }
    
}
