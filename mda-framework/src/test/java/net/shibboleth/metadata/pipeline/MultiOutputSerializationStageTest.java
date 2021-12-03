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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.ItemSerializer;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.pipeline.MultiOutputSerializationStage.Destination;

public class MultiOutputSerializationStageTest {
    
    private static class StringMapOutputStrategy implements MultiOutputSerializationStage.OutputStrategy<String> {

        public final Map<String, String> map = new HashMap<>();

        private class StringDestination implements MultiOutputSerializationStage.Destination {

            private final String id;
            private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            public StringDestination(String i) {
                id = i;
            }

            @Override
            public void close() throws IOException {
                map.put(id, baos.toString("UTF-8"));
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return baos;
            }
            
        }

        @Override
        public Destination getDestination(Item<String> item) throws StageProcessingException {
            // Locate the item's identifier.
            final List<ItemId> ids = item.getItemMetadata().get(ItemId.class);
            if (ids.isEmpty()) {
                throw new StageProcessingException("item has no ItemId to base a file name on");
            }
            final ItemId id = ids.get(0);
            return new StringDestination(id.getId());
        }
        
    }

    @Test public void basicTest() throws Exception {
        final Item<String> aaaItem = new MockItem("aaaContent");
        aaaItem.getItemMetadata().put(new ItemId("aaa"));
        final Item<String> bbbItem = new MockItem("bbbContent");
        bbbItem.getItemMetadata().put(new ItemId("bbb"));
        final Item<String> cccItem = new MockItem("cccContent");
        cccItem.getItemMetadata().put(new ItemId("ccc"));
        final List<Item<String>> items = new ArrayList<>();
        items.add(aaaItem);
        items.add(cccItem);
        items.add(bbbItem);
        
        final StringMapOutputStrategy strategy = new StringMapOutputStrategy();

        final MultiOutputSerializationStage<String> stage = new MultiOutputSerializationStage<>();
        stage.setId("test");
        stage.setOutputStrategy(strategy);
        stage.setSerializer(new ItemSerializer<String> () {

            @Override
            public void serialize(Item<String> item, OutputStream output) {
                try {
                    output.write(item.unwrap().getBytes("UTF-8"));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        });
        stage.initialize();
        
        stage.execute(items);
        stage.destroy();
        
        final Map<String, String> map = strategy.map;
        Assert.assertEquals(map.size(), 3);
        Assert.assertEquals(map.get("aaa"), "aaaContent");
        Assert.assertEquals(map.get("bbb"), "bbbContent");
        Assert.assertEquals(map.get("ccc"), "cccContent");
    }

}
