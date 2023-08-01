/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

public class MDQuerySHA1ItemIdTransformerTest {

    private static final Function<String, String> transform =
            new MDQuerySHA1ItemIdTransformer();

    @Test public void testMDQExample() {
        Assert.assertEquals(transform.apply("http://example.org/service"),
                "{sha1}11d72e8cf351eb6c75c721e838f469677ab41bdb");
    }

    @Test public void testUTF8Example() {
        Assert.assertEquals(transform.apply("Cherry \u03c0"), // pi
                "{sha1}2b159a35a20c66bfc91e3d7516db5d94ec6d2776");
    }
}
