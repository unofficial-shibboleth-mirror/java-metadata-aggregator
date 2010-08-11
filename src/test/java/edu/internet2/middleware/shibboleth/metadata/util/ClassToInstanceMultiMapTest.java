/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.shibboleth.metadata.util;

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
import org.testng.annotations.Test;

public class ClassToInstanceMultiMapTest {

    
    @Test
    public void testClearIsEmpty(){
        ClassToInstanceMultiMap<Object> map = new ClassToInstanceMultiMap<Object>();
        
        map.clear();
        assert map.isEmpty();
        
        map.put(new Object());
        assert !map.isEmpty();
        
        map.clear();
        assert map.isEmpty();
    }
    
    @Test
    public void testKeysAndContainsKey(){
        ClassToInstanceMultiMap<AbstractInstant> map = new ClassToInstanceMultiMap<AbstractInstant>();
        populate(map);
        assert map.keys().size() == 2;
        assert !map.containsKey(null);
        assert !map.containsKey(Chronology.class);
        assert !map.containsKey(AbstractInstant.class);
        assert !map.containsKey(AbstractDateTime.class);
        assert !map.containsKey(BaseDateTime.class);
        assert map.containsKey(DateTime.class);
        assert !map.containsKey(Comparable.class);
        assert !map.containsKey(ReadableDateTime.class);
        assert !map.containsKey(ReadableInstant.class);
        assert !map.containsKey(Serializable.class);
        assert map.containsKey(Instant.class);
        
        map = new ClassToInstanceMultiMap<AbstractInstant>(true);
        populate(map);
        assert map.keys().size() == 9;
        assert !map.containsKey(null);
        assert !map.containsKey(Chronology.class);
        assert map.containsKey(AbstractInstant.class);
        assert map.containsKey(AbstractDateTime.class);
        assert map.containsKey(BaseDateTime.class);
        assert map.containsKey(DateTime.class);
        assert map.containsKey(Comparable.class);
        assert map.containsKey(ReadableDateTime.class);
        assert map.containsKey(ReadableInstant.class);
        assert map.containsKey(Serializable.class);
        assert map.containsKey(Instant.class);
    }
    
    @Test
    public void testValuesAndContainsValues(){
        ClassToInstanceMultiMap<AbstractInstant> map = new ClassToInstanceMultiMap<AbstractInstant>();
        
        DateTime now = new DateTime();
        map.put(now);
        
        DateTime now100 = now.plus(100);
        map.put(now100);
        
        Instant instant = new Instant();
        map.put(instant);
        
        assert map.values().size() == 3;
        assert !map.containsValue(null);
        assert !map.containsValue(now.minus(100));
        assert !map.containsValue(instant.minus(100));
        assert map.containsValue(instant);
        assert map.containsValue(now);
        assert map.containsValue(now100);
    }
    
    @Test
    public void testEquals(){
        //TODO
    }
    
    @Test
    public void testGet(){
        ClassToInstanceMultiMap<AbstractInstant> map = new ClassToInstanceMultiMap<AbstractInstant>();
        populate(map);
        
        List<?> values = map.get(null);
        assert values.size() == 0;
        
        values = map.get(DateTime.class);
        assert values.size() == 2;
        
        values = map.get(Instant.class);
        assert values.size() == 1;
    }
    
    protected void populate(ClassToInstanceMultiMap<AbstractInstant> map){
        DateTime now = new DateTime();
        map.put(now);
        
        DateTime now100 = now.plus(100);
        map.put(now100);
        
        Instant instant = new Instant();
        map.put(instant);
    }
}