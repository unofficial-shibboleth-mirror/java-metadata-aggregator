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

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class AbstractStageTest {

    @Test
    public void getSetCollectionPredicate() {
        final CountingStage<String> c = new CountingStage<>();
        final Predicate<Collection<Item<String>>> pred = c.getCollectionPredicate();
        final List<Item<String>> list = new ArrayList<>();
        Assert.assertTrue(pred.apply(list));
        
        final Predicate<Collection<Item<String>>> pred2 = Predicates.alwaysFalse();
        Assert.assertNotSame(pred, pred2);
        c.setCollectionPredicate(pred2);
        Assert.assertSame(c.getCollectionPredicate(), pred2);
    }


    @Test
    public void conditionalExecution() throws Exception {
        final Item<String> md1 = new MockItem("one");
        final Item<String> md2 = new MockItem("two");
        final List<Item<String>> items = new ArrayList<>();
        items.add(md1);
        items.add(md2);

        final AtLeastCollectionPredicate<Item<String>> pred2 = new AtLeastCollectionPredicate<>();
        pred2.setMinimum(2);
        final CountingStage<String> stage2 = new CountingStage<>();
        stage2.setCollectionPredicate(pred2);
        
        final AtLeastCollectionPredicate<Item<String>> pred3 = new AtLeastCollectionPredicate<>();
        pred3.setMinimum(3);
        final CountingStage<String> stage3 = new CountingStage<>();
        stage3.setCollectionPredicate(pred3);
        
        final List<Stage<String>> stages = new ArrayList<>();
        stages.add(stage2);
        stages.add(stage3);
        
        final SimplePipeline<String> pipeline = new SimplePipeline<>();
        pipeline.setId("test");
        pipeline.setStages(stages);
        pipeline.initialize();

        pipeline.execute(items);
        
        // the >=2 stage should have executed
        Assert.assertEquals(stage2.getInvocationCount(), 1);
        Assert.assertEquals(stage2.getItemCount(), 2);
        
        // the >=3 stage should not have executed
        Assert.assertEquals(stage3.getInvocationCount(), 0);
        Assert.assertEquals(stage3.getItemCount(), 0);
    }

}
