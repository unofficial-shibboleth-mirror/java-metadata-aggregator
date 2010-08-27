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

package edu.internet2.middleware.shibboleth.metadata;

import org.testng.annotations.Test;

public class TagInfoTest {

    @Test
    public void test() {
        TagInfo info = new TagInfo(" test ");
        assert info.getTag().equals("test");

        try {
            info = new TagInfo("");
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // expected this
        }

        try {
            info = new TagInfo(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // expected this
        }
    }
}