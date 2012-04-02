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

package net.shibboleth.metadata.util;

import java.io.Serializable;
import java.util.List;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.base.AbstractDateTime;
import org.joda.time.base.AbstractInstant;
import org.joda.time.base.BaseDateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link ClassToInstanceMultiMap}. */
public class ClassToInstanceMultiMapTest {

    @Test public void testClearIsEmpty() {
        ClassToInstanceMultiMap<Object> map = new ClassToInstanceMultiMap<Object>();

        map.clear();
        Assert.assertTrue(map.isEmpty());

        map.put(new Object());
        Assert.assertFalse(map.isEmpty());

        map.clear();
        Assert.assertTrue(map.isEmpty());
    }

    @Test public void testKeysAndContainsKey() {
        ClassToInstanceMultiMap<AbstractInstant> map = new ClassToInstanceMultiMap<AbstractInstant>();
        populate(map);
        Assert.assertEquals( map.keys().size(), 2);
        Assert.assertFalse(map.containsKey(null));
        Assert.assertFalse(map.containsKey(Chronology.class));
        Assert.assertFalse(map.containsKey(AbstractInstant.class));
        Assert.assertFalse(map.containsKey(AbstractDateTime.class));
        Assert.assertFalse(map.containsKey(BaseDateTime.class));
        Assert.assertTrue(map.containsKey(DateTime.class));
        Assert.assertFalse(map.containsKey(Comparable.class));
        Assert.assertFalse(map.containsKey(ReadableDateTime.class));
        Assert.assertFalse(map.containsKey(ReadableInstant.class));
        Assert.assertFalse(map.containsKey(Serializable.class));
        Assert.assertTrue( map.containsKey(Instant.class));

        map = new ClassToInstanceMultiMap<AbstractInstant>(true);
        populate(map);
        Assert.assertEquals(map.keys().size(), 9);
        Assert.assertFalse(map.containsKey(null));
        Assert.assertFalse(map.containsKey(Chronology.class));
        Assert.assertTrue( map.containsKey(AbstractInstant.class));
        Assert.assertTrue( map.containsKey(AbstractDateTime.class));
        Assert.assertTrue( map.containsKey(BaseDateTime.class));
        Assert.assertTrue( map.containsKey(DateTime.class));
        Assert.assertTrue( map.containsKey(Comparable.class));
        Assert.assertTrue( map.containsKey(ReadableDateTime.class));
        Assert.assertTrue( map.containsKey(ReadableInstant.class));
        Assert.assertTrue( map.containsKey(Serializable.class));
        Assert.assertTrue( map.containsKey(Instant.class));
    }

    @Test public void testValuesAndContainsValues() {
        ClassToInstanceMultiMap<AbstractInstant> map = new ClassToInstanceMultiMap<AbstractInstant>();

        DateTime now = new DateTime();
        map.put(now);

        DateTime now100 = now.plus(100);
        map.put(now100);

        Instant instant = new Instant();
        map.put(instant);

        Assert.assertEquals(map.values().size(), 3);
        Assert.assertFalse(map.containsValue(null));
        Assert.assertFalse(map.containsValue(now.minus(100)));
        Assert.assertFalse(map.containsValue(instant.minus(100)));
        Assert.assertTrue( map.containsValue(instant));
        Assert.assertTrue(map.containsValue(now));
        Assert.assertTrue(map.containsValue(now100));
    }

    @Test public void testEquals() {
        // TODO
    }

    @Test public void testGet() {
        ClassToInstanceMultiMap<AbstractInstant> map = new ClassToInstanceMultiMap<AbstractInstant>();
        populate(map);

        List<?> values = map.get(null);
        Assert.assertEquals(values.size(), 0);

        values = map.get(DateTime.class);
        Assert.assertEquals(values.size(), 2);

        values = map.get(Instant.class);
        Assert.assertEquals(values.size(), 1);
    }

    protected void populate(ClassToInstanceMultiMap<AbstractInstant> map) {
        DateTime now = new DateTime();
        map.put(now);

        DateTime now100 = now.plus(100);
        map.put(now100);

        Instant instant = new Instant();
        map.put(instant);
    }
}