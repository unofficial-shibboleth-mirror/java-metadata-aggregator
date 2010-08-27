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

package edu.internet2.middleware.shibboleth.metadata.query;

import java.util.ArrayList;

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.Test;

import edu.internet2.middleware.shibboleth.metadata.EntityIdInfo;
import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.MockMetadata;
import edu.internet2.middleware.shibboleth.metadata.TagInfo;
import edu.internet2.middleware.shibboleth.metadata.pipeline.MockSource;
import edu.internet2.middleware.shibboleth.metadata.pipeline.Pipeline;
import edu.internet2.middleware.shibboleth.metadata.pipeline.SimplePipeline;
import edu.internet2.middleware.shibboleth.metadata.pipeline.Stage;

/**
 *
 */
public class QueryControllerTest {

    @Test
    public void testQueryController() throws Exception {
        QueryController controller = new QueryController(buildPipeline(), 1);

        // since the controller loads its metadata in a background thread we wait a second to let it do its thing
        Thread.sleep(1000);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo("/entities/one");
        MetadataCollection<?> result = controller.queryMetadata(request);
        assert result.size() == 1;

        request = new MockHttpServletRequest();
        request.setPathInfo("/entities/test");
        result = controller.queryMetadata(request);
        assert result.size() == 2;

        request = new MockHttpServletRequest();
        request.setPathInfo("/entities/test+two");
        result = controller.queryMetadata(request);
        assert result.size() == 1;

        request = new MockHttpServletRequest();
        request.setPathInfo("/entities/one+test");
        result = controller.queryMetadata(request);
        assert result.size() == 0;

        request = new MockHttpServletRequest();
        request.setPathInfo("/entities/prod");
        result = controller.queryMetadata(request);
        assert result.size() == 0;

        request = new MockHttpServletRequest();
        request.setPathInfo("/entities/two+prod");
        result = controller.queryMetadata(request);
        assert result.size() == 0;
    }

    private Pipeline<MockMetadata> buildPipeline() {
        MockMetadata mdElem1 = new MockMetadata("one");
        mdElem1.getMetadataInfo().put(new EntityIdInfo("one"));

        MockMetadata mdElem2 = new MockMetadata("two");
        mdElem2.getMetadataInfo().put(new EntityIdInfo("two"));
        mdElem2.getMetadataInfo().put(new TagInfo("test"));

        MockMetadata mdElem3 = new MockMetadata("three");
        mdElem3.getMetadataInfo().put(new EntityIdInfo("three"));
        mdElem3.getMetadataInfo().put(new TagInfo("test"));

        MockSource mdSource = new MockSource(mdElem1, mdElem2, mdElem3);

        return new SimplePipeline<MockMetadata>("pipeline", mdSource, new ArrayList<Stage<MockMetadata>>());
    }
}