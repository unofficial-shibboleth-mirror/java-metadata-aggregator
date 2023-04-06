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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.testing.MockItem;

public class RegistrationAuthorityItemIdentificationStrategyTest {

    private RegistrationAuthorityItemIdentificationStrategy<String> makeStrat() {
        final var strat = new RegistrationAuthorityItemIdentificationStrategy<String>();
        strat.setNoItemIdIdentifier("mu");
        return strat;
    }
    
    @Test
    public void base_unidentified() {
        final var strat = new RegistrationAuthorityItemIdentificationStrategy<String>();
        Assert.assertEquals(strat.getNoItemIdIdentifier(), "unidentified");
        final Item<String> item = new MockItem("item");
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "unidentified");
    }
    
    @Test
    public void base_setNoItemIdIdentifier() {
        final var strat = makeStrat();
        strat.setNoItemIdIdentifier("mu2");
        Assert.assertEquals(strat.getNoItemIdIdentifier(), "mu2");
        final Item<String> item = new MockItem("item");
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "mu2");
    }
    
    @Test
    public void base_oneIdentifier() {
        final var strat = makeStrat();
        final Item<String> item = new MockItem("item");
        item.getItemMetadata().put(new ItemId("id"));
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "id");
    }

    @Test
    public void base_twoIdentifiers() {
        final var strat = makeStrat();
        final Item<String> item = new MockItem("item");
        item.getItemMetadata().put(new ItemId("first"));
        item.getItemMetadata().put(new ItemId("second"));
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "first");
    }

    @Test
    public void getItemIdentifier() {
        final var strat = makeStrat();
        
        final Item<String> item2 = new MockItem("item 2");
        Assert.assertEquals(strat.getItemIdentifier(item2), "mu");
        item2.getItemMetadata().put(new ItemId("item-id"));
        Assert.assertEquals(strat.getItemIdentifier(item2), "item-id");
    }
    
    @Test
    public void withRegistrationAuthority() throws Exception {
        final var strat = makeStrat();
        final Item<String> item = new MockItem("present");
        item.getItemMetadata().put(new ItemId("uk002232"));
        item.getItemMetadata().put(new RegistrationAuthority("http://ukfederation.org.uk"));
        Assert.assertEquals(strat.getItemIdentifier(item), "uk002232 (http://ukfederation.org.uk)");
    }

    @Test
    public void ignoredAuthority() throws Exception {
        final var strat = makeStrat();
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
        final var strat = makeStrat();
        final Map<String, String> nameMap = new HashMap<>();
        nameMap.put("http://ukfederation.org.uk", "UKf");
        strat.setRegistrationAuthorityDisplayNames(nameMap);
        
        final Item<String> item = new MockItem("present");
        item.getItemMetadata().put(new ItemId("uk002232"));
        item.getItemMetadata().put(new RegistrationAuthority("http://ukfederation.org.uk"));
        
        Assert.assertEquals(strat.getItemIdentifier(item), "uk002232 (UKf)");
    }
    
}
