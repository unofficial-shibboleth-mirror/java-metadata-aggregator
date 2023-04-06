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
import java.util.List;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.metadata.testing.MockItem;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** {@link ItemMetadataFilterStage} unit test. */
public class ItemMetadataFilterStageTest {

    /** Unmodifiable, prototype, collection of metadata elements. */
    private Collection<Item<String>> metadataCollectionPrototype;

    /** Metadata element which contains no {@link net.shibboleth.metadata.StatusMetadata} items. */
    private Item<String> md1;

    /** Metadata element which contains a {@link WarningStatus} item. */
    private Item<String> md2;

    /** Metadata element which contains no {@link net.shibboleth.metadata.StatusMetadata} items. */
    private Item<String> md3;

    /** Metadata element which contains a {@link WarningStatus} and {@link ErrorStatus} item. */
    private Item<String> md4;

    /** Unit test setup, initializes {@link #metadataCollectionPrototype} and metadata elements. */
    @BeforeMethod
    public void setup() {
        metadataCollectionPrototype = new ArrayList<>();

        md1 = new MockItem("1");
        metadataCollectionPrototype.add(md1);

        md2 = new MockItem("2");
        md2.getItemMetadata().put(new WarningStatus("2", "warning"));
        metadataCollectionPrototype.add(md2);

        md3 = new MockItem("3");
        metadataCollectionPrototype.add(md3);

        md4 = new MockItem("4");
        md4.getItemMetadata().put(new WarningStatus("4", "warning"));
        md4.getItemMetadata().put(new ErrorStatus("4", "error"));
        metadataCollectionPrototype.add(md4);
    }

    /**
     * Tests a {@link ItemMetadataFilterStage} without any filter requirements.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testNoFilterRequirements() throws Exception {
        final var metadataCollection = new ArrayList<>(metadataCollectionPrototype);

        ItemMetadataFilterStage<String> stage = new ItemMetadataFilterStage<>();
        stage.setId("test");
        stage.initialize();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 4);
        Assert.assertTrue(metadataCollection.contains(md1));
        Assert.assertTrue(metadataCollection.contains(md2));
        Assert.assertTrue(metadataCollection.contains(md3));
        Assert.assertTrue(metadataCollection.contains(md4));
    }

    /**
     * Tests a {@link ItemMetadataFilterStage} containing one filter requirement.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testSingleFilterRequirement() throws Exception {
        final List<Item<String>> metadataCollection = new ArrayList<>(metadataCollectionPrototype);

        Collection<Class<? extends ItemMetadata>> filterRequirements = new ArrayList<>();
        filterRequirements.add(ErrorStatus.class);

        ItemMetadataFilterStage<String> stage = new ItemMetadataFilterStage<>();
        stage.setId("test");
        stage.setSelectionRequirements(filterRequirements);
        stage.initialize();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 3);
        Assert.assertTrue(metadataCollection.contains(md1));
        Assert.assertTrue(metadataCollection.contains(md2));
        Assert.assertTrue(metadataCollection.contains(md3));
        Assert.assertFalse(metadataCollection.contains(md4));
    }

    /**
     * Tests a {@link ItemMetadataFilterStage} containing multiple filter requirements.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testMultiFilterRequirement() throws Exception {
        final List<Item<String>> metadataCollection = new ArrayList<>(metadataCollectionPrototype);

        Collection<Class<? extends ItemMetadata>> filterRequirements = new ArrayList<>();
        filterRequirements.add(InfoStatus.class);
        filterRequirements.add(WarningStatus.class);
        filterRequirements.add(ErrorStatus.class);

        ItemMetadataFilterStage<String> stage = new ItemMetadataFilterStage<>();
        stage.setId("test");
        stage.setSelectionRequirements(filterRequirements);
        stage.initialize();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 2);
        Assert.assertTrue(metadataCollection.contains(md1));
        Assert.assertFalse(metadataCollection.contains(md2));
        Assert.assertTrue(metadataCollection.contains(md3));
        Assert.assertFalse(metadataCollection.contains(md4));
    }
}
