/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
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

package net.shibboleth.metadata.pipeline;

import java.util.ArrayList;
import java.util.Collection;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.StatusMetadata;
import net.shibboleth.metadata.WarningStatus;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** {@link StatusMetadataFilterStage} unit test. */
public class StatusMetadataFilterStageTest {

    /** Unmodifiable, prototype, collection of metadata elements. */
    Collection<Item<?>> metadataCollectionPrototype;

    /** Metadata element which contains no {@link StatusMetadata} items. */
    Item md1;

    /** Metadata element which contains a {@link WarningStatus} item. */
    Item md2;

    /** Metadata element which contains no {@link StatusMetadata} items. */
    Item md3;

    /** Metadata element which contains a {@link WarningStatus} and {@link ErrorStatus} item. */
    Item md4;

    /** Unit test setup, initializes {@link #metadataCollectionPrototype} and metadata elements. */
    @BeforeTest
    public void setup() {
        metadataCollectionPrototype = new ArrayList<Item<?>>();

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

    /** Tests a {@link StatusMetadataFilterStage} without any filter requirements. */
    @Test
    public void testNoFilterRequirements() throws Exception {
        Collection<Item<?>> metadataCollection = new ArrayList<Item<?>>(metadataCollectionPrototype);

        StatusMetadataFilterStage stage = new StatusMetadataFilterStage();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 4);
        Assert.assertTrue(metadataCollection.contains(md1));
        Assert.assertTrue(metadataCollection.contains(md2));
        Assert.assertTrue(metadataCollection.contains(md3));
        Assert.assertTrue(metadataCollection.contains(md4));
    }

    /** Tests a {@link StatusMetadataFilterStage} containing one filter requirement. */
    @Test
    public void testSingleFilterRequirement() throws Exception {
        Collection<Item<?>> metadataCollection = new ArrayList<Item<?>>(metadataCollectionPrototype);

        Collection filterRequirements = new ArrayList();
        filterRequirements.add(ErrorStatus.class);

        StatusMetadataFilterStage stage = new StatusMetadataFilterStage();
        stage.setFilterRequirements(filterRequirements);
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 3);
        Assert.assertTrue(metadataCollection.contains(md1));
        Assert.assertTrue(metadataCollection.contains(md2));
        Assert.assertTrue(metadataCollection.contains(md3));
        Assert.assertFalse(metadataCollection.contains(md4));
    }

    /** Tests a {@link StatusMetadataFilterStage} containing multiple filter requirements. */
    @Test
    public void testMultiFilterRequirement() throws Exception {
        Collection<Item<?>> metadataCollection = new ArrayList<Item<?>>(metadataCollectionPrototype);

        Collection filterRequirements = new ArrayList();
        filterRequirements.add(InfoStatus.class);
        filterRequirements.add(WarningStatus.class);
        filterRequirements.add(ErrorStatus.class);

        StatusMetadataFilterStage stage = new StatusMetadataFilterStage();
        stage.setFilterRequirements(filterRequirements);
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 2);
        Assert.assertTrue(metadataCollection.contains(md1));
        Assert.assertFalse(metadataCollection.contains(md2));
        Assert.assertTrue(metadataCollection.contains(md3));
        Assert.assertFalse(metadataCollection.contains(md4));
    }
}