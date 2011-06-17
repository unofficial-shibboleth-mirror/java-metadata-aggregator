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

/** Unit tests for the {@link ItemId} class. */
public class ItemIdTest {

    @Test
    public void test() {
        ItemId info = new ItemId(" test ");
        assert info.getId().equals("test");

        try {
            info = new ItemId("");
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // expected this
        }

        try {
            info = new ItemId(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // expected this
        }
    }
    
    /**
     * Test the implementation of the <code>Comparable</code> interface.
     */
    @Test
    public void testCompareTo() {
        ItemId one = new ItemId("one");
        ItemId two = new ItemId("two");
        ItemId twoAgain = new ItemId("two");
        
        Assert.assertTrue(two.compareTo(two) == 0);
        Assert.assertTrue(two.compareTo(twoAgain) == 0);
        Assert.assertTrue(one.compareTo(two) < 0);
        Assert.assertTrue(two.compareTo(one) > 0);
    }
    
    /**
     * Test that the hash codes for different {@link ItemId}s are different.
     * Impossible to test for sure, because of course the strings chosen
     * have a very very low chance have the same hashCode.
     */
    @Test
    public void testHashCode() {
        ItemId one = new ItemId("one");
        ItemId two = new ItemId("two");
        Assert.assertFalse(one.hashCode() == two.hashCode());
    }
}