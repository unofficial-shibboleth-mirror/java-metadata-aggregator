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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Function;

public class PathSegmentStringTransformerTest {
    
    private static final Function<String, String> transform =
            new PathSegmentStringTransformer();

    @Test public void testMDQExample() {
        Assert.assertEquals(transform.apply("http://example.org/idp"),
                "http:%2F%2Fexample.org%2Fidp");
    }

    @Test public void testSpace() {
        Assert.assertEquals(transform.apply("a b"), "a%20b");
    }
    
    // https://www.talisman.org/~erlkonig/misc/lunatech%5Ewhat-every-webdev-must-know-about-url-encoding/#Thereservedcharactersaredifferentforeachpart
    @Test public void testBlue() {
        Assert.assertEquals(transform.apply("blue+light blue"),
                "blue+light%20blue");
    }

    @Test public void testMDQExample2() {
        Assert.assertEquals(transform.apply("blue/green+light blue"),
                "blue%2Fgreen+light%20blue");
    }
    @Test public void testPercent() {
        Assert.assertEquals(transform.apply("%"), "%25");
    }
}
