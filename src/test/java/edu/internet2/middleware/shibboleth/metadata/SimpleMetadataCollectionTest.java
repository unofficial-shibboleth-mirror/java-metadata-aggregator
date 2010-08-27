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

import java.util.ArrayList;

import org.testng.annotations.Test;

import edu.internet2.middleware.shibboleth.metadata.SimpleMetadataCollection;

/**
 *
 */
public class SimpleMetadataCollectionTest {

    @Test
    public void test() {
        SimpleMetadataCollection<MockMetadata> collection = new SimpleMetadataCollection<MockMetadata>();
        assert collection.isEmpty();
        assert collection.size() == 0;

        assert !collection.add(null);
        assert !collection.addAll(null);
        assert collection.isEmpty();
        assert collection.size() == 0;
        assert !collection.contains(null);
        assert !collection.remove(null);
        assert !collection.removeAll(null);
        assert !collection.retainAll(null);

        MockMetadata md1 = new MockMetadata("one");
        assert collection.add(md1);
        assert !collection.isEmpty();
        assert collection.size() == 1;
        assert collection.contains(md1);

        MockMetadata md2 = new MockMetadata("two");
        MockMetadata md3 = new MockMetadata("three");
        ArrayList<MockMetadata> mdColl = new ArrayList<MockMetadata>();
        mdColl.add(md2);
        mdColl.add(md3);

        assert collection.addAll(mdColl);
        assert collection.size() == 3;
        assert collection.contains(md1);
        assert collection.contains(md2);
        assert collection.contains(md3);
        assert collection.containsAll(mdColl);

        assert collection.retainAll(mdColl);
        assert collection.size() == 2;
        assert !collection.contains(md1);
        assert collection.contains(md2);
        assert collection.contains(md3);
        assert collection.containsAll(mdColl);

        assert collection.remove(md2);
        assert collection.size() == 1;
        assert !collection.contains(md1);
        assert !collection.contains(md2);
        assert collection.contains(md3);
        assert !collection.containsAll(mdColl);

        assert collection.removeAll(mdColl);
        assert collection.size() == 0;
        assert !collection.contains(md1);
        assert !collection.contains(md2);
        assert !collection.contains(md3);
        assert !collection.containsAll(mdColl);
    }
}