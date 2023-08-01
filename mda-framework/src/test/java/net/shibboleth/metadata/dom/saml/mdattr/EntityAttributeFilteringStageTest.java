/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package net.shibboleth.metadata.dom.saml.mdattr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.ContextImpl;
import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.EntityAttributeContext;
import net.shibboleth.metadata.dom.saml.mdrpi.RegistrationAuthorityPopulationStage;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class EntityAttributeFilteringStageTest extends BaseDOMTest {

    protected EntityAttributeFilteringStageTest() {
        super(EntityAttributeFilteringStage.class);
    }
    
    private @Nonnull Element makeInputDocument() throws Exception {
        return readXMLData("input.xml");
    }
    
    private @Nonnull List<Item<Element>> makeItems(final @Nonnull Element inputElement) throws Exception {
        final Item<Element> item = new DOMElementItem(inputElement);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        // Extract the registration authority
        final RegistrationAuthorityPopulationStage ras = new RegistrationAuthorityPopulationStage();
        ras.setId("id");
        ras.initialize();
        ras.execute(items);
        ras.destroy();

        return items;
    }

    private @Nonnull List<Item<Element>> makeInputItems() throws Exception {
        return makeItems(makeInputDocument());
    }
    
    @Test
    public void testNoPredicates() throws Exception {
        final List<Item<Element>> items = makeInputItems();
        
        final EntityAttributeFilteringStage stage = new EntityAttributeFilteringStage();
        stage.setId("id");
        stage.initialize();
        stage.execute(items);
        stage.destroy();
        
        final Element result = items.get(0).unwrap();
        final Element expected = readXMLData("keepnone.xml");
        assertXMLIdentical(expected, result);
    }
    
    @Test
    public void testKeepCoC() throws Exception {
        final List<Item<Element>> items = makeInputItems();
        final List<Predicate<EntityAttributeContext>> rules = new ArrayList<>();
        rules.add(new EntityCategoryMatcher("http://www.geant.net/uri/dataprotection-code-of-conduct/v1"));
        
        final EntityAttributeFilteringStage stage = new EntityAttributeFilteringStage();
        stage.setId("id");
        stage.setRules(rules);
        stage.initialize();
        stage.execute(items);
        stage.destroy();
        
        final Element result = items.get(0).unwrap();
        final Element expected = readXMLData("keepcoc.xml");
        assertXMLIdentical(expected, result);
    }

    @Test
    public void testKeepCoCRightRegistrar() throws Exception {
        final List<Item<Element>> items = makeInputItems();
        final List<Predicate<EntityAttributeContext>> rules = new ArrayList<>();
        rules.add(new EntityCategoryMatcher("http://www.geant.net/uri/dataprotection-code-of-conduct/v1",
                "http://ukfederation.org.uk"));
        
        final EntityAttributeFilteringStage stage = new EntityAttributeFilteringStage();
        stage.setId("id");
        stage.setRules(rules);
        stage.initialize();
        stage.execute(items);
        stage.destroy();
        
        final Element result = items.get(0).unwrap();
        final Element expected = readXMLData("keepcoc.xml");
        assertXMLIdentical(expected, result);
    }

    @Test
    public void testKeepCoCWrongRegistrar() throws Exception {
        final List<Item<Element>> items = makeInputItems();
        final List<Predicate<EntityAttributeContext>> rules = new ArrayList<>();
        rules.add(new EntityCategoryMatcher("http://www.geant.net/uri/dataprotection-code-of-conduct/v1",
                "http://not.ukfederation.org.uk"));
        
        final EntityAttributeFilteringStage stage = new EntityAttributeFilteringStage();
        stage.setId("id");
        stage.setRules(rules);
        stage.initialize();
        stage.execute(items);
        stage.destroy();
        
        final Element result = items.get(0).unwrap();
        final Element expected = readXMLData("keepnone.xml");
        assertXMLIdentical(expected, result);
    }

    @Test
    public void testKeepCoC2() throws Exception {
        final List<Item<Element>> items = makeInputItems();
        final List<Predicate<EntityAttributeContext>> rules = new ArrayList<>();
        rules.add(new EntityCategoryMatcher("http://www.geant.net/uri/dataprotection-code-of-conduct/v1"));
        rules.add(new EntityCategorySupportMatcher("http://www.geant.net/uri/dataprotection-code-of-conduct/v1"));
        
        final EntityAttributeFilteringStage stage = new EntityAttributeFilteringStage();
        stage.setId("id");
        stage.setRules(rules);
        stage.initialize();
        stage.execute(items);
        stage.destroy();
        
        final Element result = items.get(0).unwrap();
        final Element expected = readXMLData("keepcoc2.xml");
        assertXMLIdentical(expected, result);
    }

    @Test
    public void testKeepEverything() throws Exception {
        final List<Item<Element>> items = makeInputItems();
        final List<Predicate<EntityAttributeContext>> rules = new ArrayList<>();
        rules.add(x -> true);
        
        final EntityAttributeFilteringStage stage = new EntityAttributeFilteringStage();
        stage.setId("id");
        stage.setRules(rules);
        stage.initialize();
        stage.execute(items);
        stage.destroy();
        
        final Element result = items.get(0).unwrap();
        final Element expected = readXMLData("input.xml");
        assertXMLIdentical(expected, result);
    }

    @Test
    public void testBlacklist() throws Exception {
        final List<Item<Element>> items = makeInputItems();
        final List<Predicate<EntityAttributeContext>> rules = new ArrayList<>();
        rules.add(new EntityCategoryMatcher("http://www.geant.net/uri/dataprotection-code-of-conduct/v1",
                "http://ukfederation.org.uk"));
        
        final EntityAttributeFilteringStage stage = new EntityAttributeFilteringStage();
        stage.setId("id");
        stage.setWhitelisting(false);
        stage.setRules(rules);
        stage.initialize();
        stage.execute(items);
        stage.destroy();
        
        final Element result = items.get(0).unwrap();
        final Element expected = readXMLData("blacklist.xml");
        assertXMLIdentical(expected, result);
    }

    // Tests for ContextImpl inner class

    @Test
    public void contextImplFour() {
        final EntityAttributeContext ctx = new ContextImpl("a", "b", "c", "d");
        Assert.assertEquals(ctx.getValue(), "a");
        Assert.assertEquals(ctx.getName(), "b");
        Assert.assertEquals(ctx.getNameFormat(), "c");
        Assert.assertEquals(ctx.getRegistrationAuthority(), "d");
    }
    
    @Test
    public void contextImplThree() {
        final EntityAttributeContext ctx = new ContextImpl("a", "b", "c");
        Assert.assertEquals(ctx.getValue(), "a");
        Assert.assertEquals(ctx.getName(), "b");
        Assert.assertEquals(ctx.getNameFormat(), "c");
        Assert.assertNull(ctx.getRegistrationAuthority());
    }

    @Test
    public void contextImplstringFour() {
        final EntityAttributeContext ctx = new ContextImpl("a", "b", "c", "d");
        Assert.assertEquals(ctx.toString(), "{v=a, n=b, f=c, r=d}");
    }

    @Test
    public void contextImplstringThree() {
        final EntityAttributeContext ctx = new ContextImpl("a", "b", "c");
        Assert.assertEquals(ctx.toString(), "{v=a, n=b, f=c, r=(none)}");
    }

    // Tests for MDA-168 multiple containers issue
    
    @Test
    public void testMDA168_1() throws Exception {
        final List<Item<Element>> items = makeItems(readXMLData("multicon.xml"));
        final List<Predicate<EntityAttributeContext>> rules = new ArrayList<>();
        rules.add(new EntityCategoryMatcher("http://www.geant.net/uri/dataprotection-code-of-conduct/v1"));
        
        final EntityAttributeFilteringStage stage = new EntityAttributeFilteringStage();
        stage.setId("id");
        stage.setRules(rules);
        stage.initialize();
        stage.execute(items);
        stage.destroy();
        
        final Element result = items.get(0).unwrap();
        final Element expected = readXMLData("multiout.xml");
        assertXMLIdentical(expected, result);
    }

    @Test
    public void testMDA168_2() throws Exception {
        final List<Item<Element>> items = makeItems(readXMLData("multi2in.xml"));
        final List<Predicate<EntityAttributeContext>> rules = new ArrayList<>();
        rules.add(new EntityCategoryMatcher("http://refeds.org/category/research-and-scholarship"));
        
        final EntityAttributeFilteringStage stage = new EntityAttributeFilteringStage();
        stage.setId("id");
        stage.setRules(rules);
        stage.initialize();
        stage.execute(items);
        stage.destroy();
        
        final Element result = items.get(0).unwrap();
        final Element expected = readXMLData("multi2out.xml");
        assertXMLIdentical(expected, result);
    }

    // Tests for MDA-160 log removed entity attributes

    @Test
    public void testMDA160_1() throws Exception {
        final List<Item<Element>> items = makeInputItems();

        final List<Predicate<EntityAttributeContext>> rules = new ArrayList<>();
        rules.add(new EntityCategoryMatcher("http://www.geant.net/uri/dataprotection-code-of-conduct/v1"));

        final EntityAttributeFilteringStage stage = new EntityAttributeFilteringStage();
        stage.setId("id");
        stage.setRules(rules);
        stage.setWhitelisting(false);
        stage.initialize();
        stage.execute(items);
        stage.destroy();

        // should be no warnings if we don't ask for them
        final List<WarningStatus> result = items.get(0).getItemMetadata().get(WarningStatus.class);
        Assert.assertEquals(result.size(), 0);
    }

    @Test
    public void testMDA160_2() throws Exception {
        final List<Item<Element>> items = makeInputItems();

        final List<Predicate<EntityAttributeContext>> rules = new ArrayList<>();
        rules.add(new EntityCategoryMatcher("http://www.geant.net/uri/dataprotection-code-of-conduct/v1"));

        final EntityAttributeFilteringStage stage = new EntityAttributeFilteringStage();
        stage.setId("id");
        stage.setRules(rules);
        stage.setRecordingRemovals(true);
        stage.setWhitelisting(false);
        stage.initialize();
        stage.execute(items);
        stage.destroy();

        // should be one warning
        final List<WarningStatus> result = items.get(0).getItemMetadata().get(WarningStatus.class);
        Assert.assertEquals(result.size(), 1);
        final WarningStatus warning = result.get(0);
        Assert.assertEquals(warning.getComponentId(), "id");
        Assert.assertEquals(warning.getStatusMessage(),
                "removing 'http://macedir.org/entity-category' = 'http://www.geant.net/uri/dataprotection-code-of-conduct/v1'");
    }
}
