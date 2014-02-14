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

package net.shibboleth.metadata.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.ItemTag;
import net.shibboleth.metadata.pipeline.Pipeline;
import net.shibboleth.metadata.pipeline.SimplePipeline;
import net.shibboleth.metadata.pipeline.Stage;
import net.shibboleth.metadata.pipeline.StaticItemSourceStage;

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;


public class QueryControllerTest {

    @Test
    public void testQueryController() throws Exception {
        CountingStage<String> countingStage = new CountingStage<>();
        ArrayList<Stage<String>> postProcessStages = new ArrayList<>();
        postProcessStages.add(countingStage);

        QueryController<String> controller = new QueryController<>(buildPipeline(), 1, postProcessStages);

        // since the controller loads its metadata in a background thread we wait a second to let it do its thing
        Thread.sleep(1000);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo("/entities/one");
        Collection<Item<String>> result = controller.queryMetadata(request);
        assert result.size() == 1;
        assert countingStage.getCount() == 1;

        request = new MockHttpServletRequest();
        request.setPathInfo("/entities/test");
        result = controller.queryMetadata(request);
        assert result.size() == 2;
        assert countingStage.getCount() == 2;

        request = new MockHttpServletRequest();
        request.setPathInfo("/entities/test+two");
        result = controller.queryMetadata(request);
        assert result.size() == 1;
        assert countingStage.getCount() == 3;

        request = new MockHttpServletRequest();
        request.setPathInfo("/entities/one+test");
        result = controller.queryMetadata(request);
        assert result.size() == 0;
        assert countingStage.getCount() == 3;

        request = new MockHttpServletRequest();
        request.setPathInfo("/entities/prod");
        result = controller.queryMetadata(request);
        assert result.size() == 0;
        assert countingStage.getCount() == 3;

        request = new MockHttpServletRequest();
        request.setPathInfo("/entities/two+prod");
        result = controller.queryMetadata(request);
        assert result.size() == 0;
        assert countingStage.getCount() == 3;
    }

    private Pipeline<String> buildPipeline() throws Exception {
        MockItem mdElem1 = new MockItem("one");
        mdElem1.getItemMetadata().put(new ItemId("one"));

        MockItem mdElem2 = new MockItem("two");
        mdElem2.getItemMetadata().put(new ItemId("two"));
        mdElem2.getItemMetadata().put(new ItemTag("test"));

        MockItem mdElem3 = new MockItem("three");
        mdElem3.getItemMetadata().put(new ItemId("three"));
        mdElem3.getItemMetadata().put(new ItemTag("test"));
        
        final Collection<Item<String>> items = new ArrayList<>();
        items.add(mdElem1);
        items.add(mdElem2);
        items.add(mdElem3);

        final StaticItemSourceStage<String> mdSource = new StaticItemSourceStage<>();
        mdSource.setId("source");
        mdSource.setSourceItems(items);
        mdSource.initialize();
        
        final List<Stage<String>> stages = new ArrayList<>();
        stages.add(mdSource);

        SimplePipeline<String> pipeline = new SimplePipeline<>();
        pipeline.setId("pipeline");
        pipeline.setStages(stages);
        
        return pipeline;
    }
}