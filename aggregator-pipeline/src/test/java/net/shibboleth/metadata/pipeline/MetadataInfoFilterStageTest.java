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

import net.shibboleth.metadata.ErrorStatusInfo;
import net.shibboleth.metadata.InfoStatusInfo;
import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.MockMetadata;
import net.shibboleth.metadata.StatusInfo;
import net.shibboleth.metadata.WarningStatusInfo;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/** {@link MetadataInfoFilterStage} unit test. */
public class MetadataInfoFilterStageTest {

    /** Unmodifiable, prototype, collection of metadata elements. */
    Collection<Metadata<?>> metadataCollectionPrototype;

    /** Metadata element which contains no {@link StatusInfo} items. */
    Metadata md1;

    /** Metadata element which contains a {@link WarningStatusInfo} item. */
    Metadata md2;

    /** Metadata element which contains no {@link StatusInfo} items. */
    Metadata md3;

    /** Metadata element which contains a {@link WarningStatusInfo} and {@link ErrorStatusInfo} item. */
    Metadata md4;

    /** Unit test setup, initializes {@link #metadataCollectionPrototype} and metadata elements. */
    @BeforeTest
    public void setup() {
        metadataCollectionPrototype = new ArrayList<Metadata<?>>();

        md1 = new MockMetadata("1");
        metadataCollectionPrototype.add(md1);

        md2 = new MockMetadata("2");
        md2.getMetadataInfo().put(new WarningStatusInfo("2", "warning"));
        metadataCollectionPrototype.add(md2);

        md3 = new MockMetadata("3");
        metadataCollectionPrototype.add(md3);

        md4 = new MockMetadata("4");
        md4.getMetadataInfo().put(new WarningStatusInfo("4", "warning"));
        md4.getMetadataInfo().put(new ErrorStatusInfo("4", "error"));
        metadataCollectionPrototype.add(md4);
    }

    /** Tests a {@link MetadataInfoFilterStage} without any filter requirements. */
    @Test
    public void testNoFilterRequirements() throws Exception {
        Collection<Metadata<?>> metadataCollection = new ArrayList<Metadata<?>>(metadataCollectionPrototype);

        MetadataInfoFilterStage stage = new MetadataInfoFilterStage();
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 4);
        Assert.assertTrue(metadataCollection.contains(md1));
        Assert.assertTrue(metadataCollection.contains(md2));
        Assert.assertTrue(metadataCollection.contains(md3));
        Assert.assertTrue(metadataCollection.contains(md4));
    }

    /** Tests a {@link MetadataInfoFilterStage} containing one filter requirement. */
    @Test
    public void testSingleFilterRequirement() throws Exception {
        Collection<Metadata<?>> metadataCollection = new ArrayList<Metadata<?>>(metadataCollectionPrototype);

        Collection filterRequirements = new ArrayList();
        filterRequirements.add(ErrorStatusInfo.class);

        MetadataInfoFilterStage stage = new MetadataInfoFilterStage();
        stage.setFilterRequirements(filterRequirements);
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 3);
        Assert.assertTrue(metadataCollection.contains(md1));
        Assert.assertTrue(metadataCollection.contains(md2));
        Assert.assertTrue(metadataCollection.contains(md3));
        Assert.assertFalse(metadataCollection.contains(md4));
    }

    /** Tests a {@link MetadataInfoFilterStage} containing multiple filter requirements. */
    @Test
    public void testMultiFilterRequirement() throws Exception {
        Collection<Metadata<?>> metadataCollection = new ArrayList<Metadata<?>>(metadataCollectionPrototype);

        Collection filterRequirements = new ArrayList();
        filterRequirements.add(InfoStatusInfo.class);
        filterRequirements.add(WarningStatusInfo.class);
        filterRequirements.add(ErrorStatusInfo.class);

        MetadataInfoFilterStage stage = new MetadataInfoFilterStage();
        stage.setFilterRequirements(filterRequirements);
        stage.execute(metadataCollection);

        Assert.assertEquals(metadataCollection.size(), 2);
        Assert.assertTrue(metadataCollection.contains(md1));
        Assert.assertFalse(metadataCollection.contains(md2));
        Assert.assertTrue(metadataCollection.contains(md3));
        Assert.assertFalse(metadataCollection.contains(md4));
    }
}