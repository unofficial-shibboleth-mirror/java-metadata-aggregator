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

package net.shibboleth.metadata.dom;

import java.util.ArrayList;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class DOMResourceSourceTest {

    @Test public void testSuccessfulFetchAndParse() throws Exception {
        Resource mdResource = buildHttpResource("https://issues.shibboleth.net/jira/Shibboleth.sso/Metadata");

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DOMResourceSourceStage source = new DOMResourceSourceStage();
        source.setId("test");
        source.setDOMResource(mdResource);
        source.setParserPool(parserPool);
        source.initialize();

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        source.execute(metadataCollection);
        Assert.assertNotNull(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 1);
    }

    @Test public void testSuccessfulFetchAndFailedParse() throws Exception {
        Resource mdResource = buildHttpResource("http://www.google.com/intl/en/images/about_logo.gif");

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DOMResourceSourceStage source = new DOMResourceSourceStage();
        source.setId("test");
        source.setDOMResource(mdResource);
        source.setParserPool(parserPool);
        source.initialize();

        try {
            final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
            source.execute(metadataCollection);
            throw new ConstraintViolationException("Invalid URL marked as parsed");
        } catch (StageProcessingException e) {
            // expected this
        }
    }

    @Test public void testFailedFetch() throws Exception {
        Resource mdResource = buildHttpResource("http://kslkjf.com/lkjlk3.dlw");

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DOMResourceSourceStage source = new DOMResourceSourceStage();
        source.setId("test");
        source.setDOMResource(mdResource);
        source.setParserPool(parserPool);

        try {
            source.initialize();
            Assert.fail();
        } catch (ComponentInitializationException e) {
            // expected this
        }
    }

    protected Resource buildHttpResource(String url) throws Exception {
        return new UrlResource(url);
    }
}