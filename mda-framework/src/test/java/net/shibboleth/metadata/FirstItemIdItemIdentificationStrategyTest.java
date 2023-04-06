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


package net.shibboleth.metadata;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.testing.MockItem;

public class FirstItemIdItemIdentificationStrategyTest {

    @Test
    public void unidentified() {
        final var strat = new FirstItemIdItemIdentificationStrategy<String>();
        Assert.assertEquals(strat.getNoItemIdIdentifier(), "unidentified");
        final Item<String> item = new MockItem("item");
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "unidentified");
    }
    
    @Test
    public void setNoItemIdIdentifier() {
        final var strat = new FirstItemIdItemIdentificationStrategy<String>();
        strat.setNoItemIdIdentifier("mu");
        Assert.assertEquals(strat.getNoItemIdIdentifier(), "mu");
        final Item<String> item = new MockItem("item");
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "mu");
    }
    
    @Test
    public void oneIdentifier() {
        final var strat = new FirstItemIdItemIdentificationStrategy<String>();
        final Item<String> item = new MockItem("item");
        item.getItemMetadata().put(new ItemId("id"));
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "id");
    }

    @Test
    public void twoIdentifiers() {
        final var strat = new FirstItemIdItemIdentificationStrategy<String>();
        final Item<String> item = new MockItem("item");
        item.getItemMetadata().put(new ItemId("first"));
        item.getItemMetadata().put(new ItemId("second"));
        final String res = strat.getItemIdentifier(item);
        Assert.assertEquals(res, "first");
    }

}
