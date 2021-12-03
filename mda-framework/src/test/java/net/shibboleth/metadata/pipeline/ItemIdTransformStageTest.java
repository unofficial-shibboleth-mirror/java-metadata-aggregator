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

package net.shibboleth.metadata.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.MockItem;

public class ItemIdTransformStageTest {

    @Test
    public void test() throws Exception {
        final MockItem item = new MockItem("test");
        item.getItemMetadata().put(new ItemId("http://example.org"));

        final ArrayList<Item<String>> mdColl = new ArrayList<>();
        mdColl.add(item);

        final ArrayList<Function<String, String>> transforms = new ArrayList<>();
        transforms.add(new MDQueryMD5ItemIdTransformer());
        transforms.add(new MDQuerySHA1ItemIdTransformer());

        final ItemIdTransformStage<String> stage = new ItemIdTransformStage<>();
        stage.setId("test");
        stage.setIdTransformers(transforms);
        Assert.assertFalse(stage.isInitialized());

        stage.initialize();
        Assert.assertTrue(stage.isInitialized());

        stage.execute(mdColl);
        Assert.assertEquals(mdColl.size(), 1);
        Assert.assertEquals(item.getItemMetadata().values().size(), 4);

        final ComponentInfo compInfo = item.getItemMetadata().get(ComponentInfo.class).get(0);
        Assert.assertNotNull(compInfo.getCompleteInstant());
        Assert.assertEquals(compInfo.getComponentId(), "test");
        Assert.assertEquals(compInfo.getComponentType(), ItemIdTransformStage.class);
        Assert.assertNotNull(compInfo.getStartInstant());

        final List<ItemId> idInfos = item.getItemMetadata().get(ItemId.class);
        Assert.assertEquals(idInfos.size(), 3);

        boolean idMatch = false, sha1Match = false, md5Match = false;
        for (ItemId info : idInfos) {
            final String id = info.getId();
            if (id.startsWith("{sha1}")) {
                Assert.assertFalse(sha1Match);
                sha1Match = "{sha1}ff7c1f10ab54968058fdcfaadf1b2457cd5d1a3f".equals(id);
            } else if (id.startsWith("{md5}")) {
                Assert.assertFalse(md5Match);
                md5Match = "{md5}dab521de65f9250b4cca7383feef67dc".equals(id);
            } else {
                Assert.assertFalse(idMatch);
                idMatch = "http://example.org".equals(id);
            }
        }

        Assert.assertTrue(idMatch);
        Assert.assertTrue(sha1Match);
        Assert.assertTrue(md5Match);
    }
    
    @Test
    public void testMDA226() throws Exception {
        final var stage = new ItemIdTransformStage<>();
        
        // Initial list should be empty
        Assert.assertEquals(stage.getIdTransformers().size(), 0);
        
        // Put in a list of one thing.
        stage.setIdTransformers(List.of(new MDQueryMD5ItemIdTransformer()));
        Assert.assertEquals(stage.getIdTransformers().size(), 1);
        
        // Put in a list of one other thing
        stage.setIdTransformers(List.of(new MDQuerySHA1ItemIdTransformer()));
        // Should still be one thing, not accumulated to two
        Assert.assertEquals(stage.getIdTransformers().size(), 1);
        // Should also be the new thing, not the old thing.
        Assert.assertTrue(List.copyOf(stage.getIdTransformers()).get(0) instanceof MDQuerySHA1ItemIdTransformer);
    }
}
