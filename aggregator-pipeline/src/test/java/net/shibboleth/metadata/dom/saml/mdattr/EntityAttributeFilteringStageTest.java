
package net.shibboleth.metadata.dom.saml.mdattr;

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.ContextImpl;
import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.EntityAttributeContext;
import net.shibboleth.metadata.dom.saml.mdrpi.RegistrationAuthorityPopulationStage;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class EntityAttributeFilteringStageTest extends BaseDOMTest {

    protected EntityAttributeFilteringStageTest() {
        super(EntityAttributeFilteringStage.class);
    }
    
    private Element makeInputDocument() throws Exception {
        return readXMLData("input.xml");
    }
    
    private List<Item<Element>> makeItems(final Element inputElement) throws Exception {
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

    private List<Item<Element>> makeInputItems() throws Exception {
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
        rules.add(Predicates.<EntityAttributeContext>alwaysTrue());
        
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
}
