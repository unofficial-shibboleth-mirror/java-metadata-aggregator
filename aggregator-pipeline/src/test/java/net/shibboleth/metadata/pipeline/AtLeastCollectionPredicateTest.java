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
import java.util.Collection;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AtLeastCollectionPredicateTest {

    @Test
    public void defaultMinimum() {
        final AtLeastCollectionPredicate<String> pred = new AtLeastCollectionPredicate<>();
        Assert.assertEquals(pred.getMinimum(), 0);
        
        final Collection<String> empty = new ArrayList<>();
        Assert.assertTrue(pred.apply(empty));
        
        final Collection<String> single = new ArrayList<>();
        single.add("single");
        Assert.assertTrue(pred.apply(single));
    }
    
    @Test
    public void explicitMinimum() {
        final AtLeastCollectionPredicate<String> pred = new AtLeastCollectionPredicate<>();
        pred.setMinimum(1);
        Assert.assertEquals(pred.getMinimum(), 1);
        
        final Collection<String> empty = new ArrayList<>();
        Assert.assertFalse(pred.apply(empty));
        
        final Collection<String> single = new ArrayList<>();
        single.add("single");
        Assert.assertTrue(pred.apply(single));
    }
    
}
