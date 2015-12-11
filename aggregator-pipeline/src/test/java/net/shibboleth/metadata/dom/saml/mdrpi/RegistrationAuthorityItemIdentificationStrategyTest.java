
package net.shibboleth.metadata.dom.saml.mdrpi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.MockItem;

public class RegistrationAuthorityItemIdentificationStrategyTest {

    private RegistrationAuthorityItemIdentificationStrategy makeStrat() {
        final RegistrationAuthorityItemIdentificationStrategy strat = new RegistrationAuthorityItemIdentificationStrategy();
        strat.setNoItemIdIdentifier("mu");
        return strat;
    }
    
    @Test
    public void base_unidentified() {
        final RegistrationAuthorityItemIdentificationStrategy strat = new RegistrationAuthorityItemIdentificationStrategy();
        Assert.assertEquals(strat.getNoItemIdIdentifier(), "unidentified");
        final Item<String> item = new MockItem("item");
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "unidentified");
    }
    
    @Test
    public void base_setNoItemIdIdentifier() {
        final RegistrationAuthorityItemIdentificationStrategy strat = makeStrat();
        strat.setNoItemIdIdentifier("mu2");
        Assert.assertEquals(strat.getNoItemIdIdentifier(), "mu2");
        final Item<String> item = new MockItem("item");
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "mu2");
    }
    
    @Test
    public void base_oneIdentifier() {
        final RegistrationAuthorityItemIdentificationStrategy strat = makeStrat();
        final Item<String> item = new MockItem("item");
        item.getItemMetadata().put(new ItemId("id"));
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "id");
    }

    @Test
    public void base_twoIdentifiers() {
        final RegistrationAuthorityItemIdentificationStrategy strat = makeStrat();
        final Item<String> item = new MockItem("item");
        item.getItemMetadata().put(new ItemId("first"));
        item.getItemMetadata().put(new ItemId("second"));
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "first");
    }

    @Test
    public void getItemIdentifier() {
        final RegistrationAuthorityItemIdentificationStrategy strat = makeStrat();
        
        final Item<String> item2 = new MockItem("item 2");
        Assert.assertEquals(strat.getItemIdentifier(item2), "mu");
        item2.getItemMetadata().put(new ItemId("item-id"));
        Assert.assertEquals(strat.getItemIdentifier(item2), "item-id");
    }
    
    @Test
    public void withRegistrationAuthority() throws Exception {
        final RegistrationAuthorityItemIdentificationStrategy strat = makeStrat();
        final Item<String> item = new MockItem("present");
        item.getItemMetadata().put(new ItemId("uk002232"));
        item.getItemMetadata().put(new RegistrationAuthority("http://ukfederation.org.uk"));
        Assert.assertEquals(strat.getItemIdentifier(item), "uk002232 (http://ukfederation.org.uk)");
    }

    @Test
    public void ignoredAuthority() throws Exception {
        final RegistrationAuthorityItemIdentificationStrategy strat = makeStrat();
        final Item<String> item = new MockItem("present");
        item.getItemMetadata().put(new ItemId("uk002232"));
        item.getItemMetadata().put(new RegistrationAuthority("http://ukfederation.org.uk"));

        final Set<String> auths = new HashSet<>();
        auths.add("http://ukfederation.org.uk");
        strat.setIgnoredRegistrationAuthorities(auths);
        
        Assert.assertEquals(strat.getItemIdentifier(item), "uk002232");
    }
    
    @Test
    public void mappedAuthority() throws Exception {
        final RegistrationAuthorityItemIdentificationStrategy strat = makeStrat();
        final Map<String, String> nameMap = new HashMap<>();
        nameMap.put("http://ukfederation.org.uk", "UKf");
        strat.setRegistrationAuthorityDisplayNames(nameMap);
        
        final Item<String> item = new MockItem("present");
        item.getItemMetadata().put(new ItemId("uk002232"));
        item.getItemMetadata().put(new RegistrationAuthority("http://ukfederation.org.uk"));
        
        Assert.assertEquals(strat.getItemIdentifier(item), "uk002232 (UKf)");
    }
    
}
