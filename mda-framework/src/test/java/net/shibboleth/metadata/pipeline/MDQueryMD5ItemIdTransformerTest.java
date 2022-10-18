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

import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MDQueryMD5ItemIdTransformerTest {

    private static final Function<String, String> transform =
            new MDQueryMD5ItemIdTransformer();

    @Test public void testMDQExample() {
        Assert.assertEquals(transform.apply("http://example.org/service"),
                "{md5}f3678248a29ab8e8e5b1b00bee4060e0");
    }

    @Test public void testUTF8Example() {
        Assert.assertEquals(transform.apply("Cherry \u03c0"), // pi
                "{md5}37b47884fe3429710db40d81de044a7d");
    }
}
