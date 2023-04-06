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
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.component.DestroyedComponentException;
import net.shibboleth.shared.component.UnmodifiableComponentException;
import net.shibboleth.shared.xml.ElementSupport;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** Unit test for {@link ContactPersonFilterStage}. */
public class ContactPersonFilterStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public ContactPersonFilterStageTest() {
        super(ContactPersonFilterStage.class);
    }

    private final @Nonnull QName contactPersonQname = new QName(SAMLMetadataSupport.MD_NS, "ContactPerson");
    
    @Test public void testDesignatedTypes() throws ComponentInitializationException {
        ContactPersonFilterStage stage = new ContactPersonFilterStage();
        stage.setId("foo");
        Assert.assertEquals(stage.getDesignatedTypes().size(), 5);
        Assert.assertTrue(stage.getDesignatedTypes().contains(ContactPersonFilterStage.ADMINISTRATIVE));
        Assert.assertTrue(stage.getDesignatedTypes().contains(ContactPersonFilterStage.BILLING));
        Assert.assertTrue(stage.getDesignatedTypes().contains(ContactPersonFilterStage.OTHER));
        Assert.assertTrue(stage.getDesignatedTypes().contains(ContactPersonFilterStage.SUPPORT));
        Assert.assertTrue(stage.getDesignatedTypes().contains(ContactPersonFilterStage.TECHNICAL));

        stage.setDesignatedTypes(CollectionSupport.setOf(ContactPersonFilterStage.ADMINISTRATIVE,
                ContactPersonFilterStage.TECHNICAL, "", "foo", ContactPersonFilterStage.OTHER));
        Assert.assertEquals(stage.getDesignatedTypes().size(), 3);
        Assert.assertTrue(stage.getDesignatedTypes().contains(ContactPersonFilterStage.ADMINISTRATIVE));
        Assert.assertTrue(stage.getDesignatedTypes().contains(ContactPersonFilterStage.OTHER));
        Assert.assertTrue(stage.getDesignatedTypes().contains(ContactPersonFilterStage.TECHNICAL));

        stage.setDesignatedTypes(CollectionSupport.<String>emptyList());
        Assert.assertEquals(stage.getDesignatedTypes().size(), 0);

        stage.initialize();
        try {
            stage.setDesignatedTypes(CollectionSupport.setOf(ContactPersonFilterStage.ADMINISTRATIVE));
            Assert.fail();
        } catch (UnmodifiableComponentException e) {
            Assert.assertEquals(stage.getDesignatedTypes().size(), 0);
        }

        stage = new ContactPersonFilterStage();
        stage.destroy();
        try {
            stage.setDesignatedTypes(CollectionSupport.setOf(ContactPersonFilterStage.ADMINISTRATIVE));
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // expected this
        }

        stage = new ContactPersonFilterStage();
        try {
            stage.getDesignatedTypes().add("foo");
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
        final @Nonnull var entitiesDescriptor = readXMLData("entities.xml");
        ContactPersonFilterStage stage = new ContactPersonFilterStage();
        stage.setId("foo");
        stage.initialize();

        final ArrayList<Item<Element>> items = new ArrayList<>();
        items.add(new DOMElementItem(entitiesDescriptor));

        stage.execute(items);

        Element filteredEntitiesDescriptor = items.get(0).unwrap();
        List<Element> entityDescriptors = ElementSupport.getChildElements(filteredEntitiesDescriptor);
        
        Element idpDescriptor = entityDescriptors.get(0);
        assert idpDescriptor != null;
        List<Element> contactPersons = ElementSupport.getChildElements(idpDescriptor, contactPersonQname);
        Assert.assertEquals(contactPersons.size(), 1);
        
        Element issuesDescriptor = entityDescriptors.get(1);
        assert issuesDescriptor != null;
        contactPersons = ElementSupport.getChildElements(issuesDescriptor, contactPersonQname);
        Assert.assertEquals(contactPersons.size(), 5);
        
        Element wikiDescriptor = entityDescriptors.get(2);
        assert wikiDescriptor != null;
        contactPersons = ElementSupport.getChildElements(wikiDescriptor, contactPersonQname);
        Assert.assertEquals(contactPersons.size(), 0);
    }
    
    @Test public void testBlacklistContactPersons() throws Exception {
        final @Nonnull var entitiesDescriptor = readXMLData("entities.xml");
        ContactPersonFilterStage stage = new ContactPersonFilterStage();
        stage.setId("foo");
        stage.setDesignatedTypes(CollectionSupport.setOf(ContactPersonFilterStage.ADMINISTRATIVE, ContactPersonFilterStage.OTHER));
        stage.setWhitelistingTypes(false);
        stage.initialize();

        final ArrayList<Item<Element>> items = new ArrayList<>();
        items.add(new DOMElementItem(entitiesDescriptor));

        stage.execute(items);

        Element filteredEntitiesDescriptor = items.get(0).unwrap();
        List<Element> entityDescriptors = ElementSupport.getChildElements(filteredEntitiesDescriptor);
        
        Element idpDescriptor = entityDescriptors.get(0);
        assert idpDescriptor != null;
        List<Element> contactPersons = ElementSupport.getChildElements(idpDescriptor, contactPersonQname);
        Assert.assertEquals(contactPersons.size(), 1);
        
        Element issuesDescriptor = entityDescriptors.get(1);
        assert issuesDescriptor != null;
        contactPersons = ElementSupport.getChildElements(issuesDescriptor, contactPersonQname);
        Assert.assertEquals(contactPersons.size(), 3);
        
        Element wikiDescriptor = entityDescriptors.get(2);
        assert wikiDescriptor !=  null;
        contactPersons = ElementSupport.getChildElements(wikiDescriptor, contactPersonQname);
        Assert.assertEquals(contactPersons.size(), 0);
    }
    
    @Test
    public void mda243() throws Exception {
        final var stage = new ContactPersonFilterStage();
        stage.setId("test");
        stage.setDesignatedTypes(CollectionSupport.setOf(ContactPersonFilterStage.ADMINISTRATIVE, ContactPersonFilterStage.OTHER));
        stage.initialize();
        final var types = stage.getDesignatedTypes();
        Assert.assertEquals(types.size(), 2);
        Assert.assertTrue(types.contains(ContactPersonFilterStage.ADMINISTRATIVE));
        Assert.assertFalse(types.contains(ContactPersonFilterStage.BILLING));
    }

}
