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
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import com.google.common.collect.Sets;

/** Unit test for {@link ContactPersonFilterStage}. */
public class ContactPersonFilterStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public ContactPersonFilterStageTest() {
        super(ContactPersonFilterStage.class);
    }

    private final QName contactPersonQname = new QName("urn:oasis:names:tc:SAML:2.0:metadata", "ContactPerson");
    
    private Element entitiesDescriptor;

    @BeforeClass public void setup() throws Exception {
        entitiesDescriptor = readXMLData("entities.xml");
    }

    @Test public void testDesignatedTypes() throws ComponentInitializationException {
        ContactPersonFilterStage stage = new ContactPersonFilterStage();
        stage.setId("foo");
        Assert.assertEquals(stage.getDesignateTypes().size(), 5);
        Assert.assertTrue(stage.getDesignateTypes().contains(ContactPersonFilterStage.ADMINISTRATIVE));
        Assert.assertTrue(stage.getDesignateTypes().contains(ContactPersonFilterStage.BILLING));
        Assert.assertTrue(stage.getDesignateTypes().contains(ContactPersonFilterStage.OTHER));
        Assert.assertTrue(stage.getDesignateTypes().contains(ContactPersonFilterStage.SUPPORT));
        Assert.assertTrue(stage.getDesignateTypes().contains(ContactPersonFilterStage.TECHNICAL));

        stage.setDesignatedTypes(Sets.newHashSet(ContactPersonFilterStage.ADMINISTRATIVE, null,
                ContactPersonFilterStage.TECHNICAL, "", "foo", ContactPersonFilterStage.OTHER));
        Assert.assertEquals(stage.getDesignateTypes().size(), 3);
        Assert.assertTrue(stage.getDesignateTypes().contains(ContactPersonFilterStage.ADMINISTRATIVE));
        Assert.assertTrue(stage.getDesignateTypes().contains(ContactPersonFilterStage.OTHER));
        Assert.assertTrue(stage.getDesignateTypes().contains(ContactPersonFilterStage.TECHNICAL));

        stage.setDesignatedTypes(Collections.<String>emptyList());
        Assert.assertEquals(stage.getDesignateTypes().size(), 0);

        stage.setDesignatedTypes(null);
        Assert.assertEquals(stage.getDesignateTypes().size(), 0);

        stage.initialize();
        try {
            stage.setDesignatedTypes(Sets.newHashSet(ContactPersonFilterStage.ADMINISTRATIVE));
            Assert.fail();
        } catch (UnmodifiableComponentException e) {
            Assert.assertEquals(stage.getDesignateTypes().size(), 0);
        }

        stage = new ContactPersonFilterStage();
        stage.destroy();
        try {
            stage.setDesignatedTypes(Sets.newHashSet(ContactPersonFilterStage.ADMINISTRATIVE));
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // expected this
        }

        stage = new ContactPersonFilterStage();
        try {
            stage.getDesignateTypes().add("foo");
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // expected this
        }
    }

    @Test public void testWhitelistingTypes() throws ComponentInitializationException {
        ContactPersonFilterStage stage = new ContactPersonFilterStage();
        stage.setId("foo");
        Assert.assertTrue(stage.isWhitelistingTypes());

        stage.setWhitelistingTypes(false);
        Assert.assertFalse(stage.isWhitelistingTypes());

        stage.initialize();
        try {
            stage.setWhitelistingTypes(true);
            Assert.fail();
        } catch (UnmodifiableComponentException e) {
            Assert.assertFalse(stage.isWhitelistingTypes());
        }

        stage = new ContactPersonFilterStage();
        stage.destroy();
        try {
            stage.setWhitelistingTypes(true);
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // expected this
        }
    }
    
    @Test public void testWhitelistContactPersons() throws Exception {
        ContactPersonFilterStage stage = new ContactPersonFilterStage();
        stage.setId("foo");
        stage.initialize();

        final ArrayList<Item<Element>> itemCollection = new ArrayList<>();
        itemCollection.add(new DOMElementItem(entitiesDescriptor));

        stage.execute(itemCollection);

        Element filteredEntitiesDescriptor = itemCollection.get(0).unwrap();
        List<Element> entityDescriptors = ElementSupport.getChildElements(filteredEntitiesDescriptor);
        
        Element idpDescriptor = entityDescriptors.get(0);
        List<Element> contactPersons = ElementSupport.getChildElements(idpDescriptor, contactPersonQname);
        Assert.assertEquals(contactPersons.size(), 1);
        
        Element issuesDescriptor = entityDescriptors.get(1);
        contactPersons = ElementSupport.getChildElements(issuesDescriptor, contactPersonQname);
        Assert.assertEquals(contactPersons.size(), 5);
        
        Element wikiDescriptor = entityDescriptors.get(2);
        contactPersons = ElementSupport.getChildElements(wikiDescriptor, contactPersonQname);
        Assert.assertEquals(contactPersons.size(), 0);
    }
    
    @Test public void testBlacklistContactPersons() throws Exception {
        ContactPersonFilterStage stage = new ContactPersonFilterStage();
        stage.setId("foo");
        stage.setDesignatedTypes(Sets.newHashSet(ContactPersonFilterStage.ADMINISTRATIVE, ContactPersonFilterStage.OTHER));
        stage.setWhitelistingTypes(false);
        stage.initialize();

        final ArrayList<Item<Element>> itemCollection = new ArrayList<>();
        itemCollection.add(new DOMElementItem(entitiesDescriptor));

        stage.execute(itemCollection);

        Element filteredEntitiesDescriptor = itemCollection.get(0).unwrap();
        List<Element> entityDescriptors = ElementSupport.getChildElements(filteredEntitiesDescriptor);
        
        Element idpDescriptor = entityDescriptors.get(0);
        List<Element> contactPersons = ElementSupport.getChildElements(idpDescriptor, contactPersonQname);
        Assert.assertEquals(contactPersons.size(), 1);
        
        Element issuesDescriptor = entityDescriptors.get(1);
        contactPersons = ElementSupport.getChildElements(issuesDescriptor, contactPersonQname);
        Assert.assertEquals(contactPersons.size(), 3);
        
        Element wikiDescriptor = entityDescriptors.get(2);
        contactPersons = ElementSupport.getChildElements(wikiDescriptor, contactPersonQname);
        Assert.assertEquals(contactPersons.size(), 0);
    }
}