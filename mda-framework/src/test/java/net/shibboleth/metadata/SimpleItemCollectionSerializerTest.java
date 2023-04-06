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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.testing.MockItem;

public class SimpleItemCollectionSerializerTest {

    @Test
    public void serializeCollection() throws Exception {
        final Item<String> i1 = new MockItem("one");
        final Item<String> i2 = new MockItem("two");
        final Item<String> i3 = new MockItem("three");
        final List<Item<String>> items = new ArrayList<>();
        items.add(i1);
        items.add(i2);
        items.add(i3);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final var base = new ItemSerializer<String>() {
            @Override
            public void serialize(@Nonnull Item<String> item, @Nonnull OutputStream out) {
                try {
                    out.write(item.unwrap().getBytes());
                } catch (IOException e) {
                    Assert.fail("exception in string serializer", e);
                }
            }
        };
        final ItemCollectionSerializer<String> ics = new SimpleItemCollectionSerializer<>(base);
        ics.serializeCollection(items, output);
        Assert.assertEquals(output.toByteArray(),
                new byte[]{'o', 'n', 'e', 't', 'w', 'o', 't', 'h', 'r', 'e', 'e'});
    }

}
