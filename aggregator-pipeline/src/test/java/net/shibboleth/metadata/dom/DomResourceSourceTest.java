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

import java.io.File;
import java.util.ArrayList;

import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.httpclient.HttpClientBuilder;
import net.shibboleth.utilities.java.support.httpclient.HttpResource;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DomResourceSourceTest {

    @Test public void testSuccessfulFetchAndParse() throws Exception {
        HttpResource mdResource = buildHttpResource("https://issues.shibboleth.net/jira/Shibboleth.sso/Metadata");

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DOMResourceSourceStage source = new DOMResourceSourceStage();
        source.setId("test");
        source.setDOMResource(mdResource);
        source.setParserPool(parserPool);
        source.initialize();

        final ArrayList<DOMElementItem> metadataCollection = new ArrayList<>();
        source.execute(metadataCollection);
        Assert.assertNotNull(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 1);
    }

    @Test public void testSuccessfulFetchAndFailedParse() throws Exception {
        HttpResource mdResource = buildHttpResource("http://www.google.com/intl/en/images/about_logo.gif");

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DOMResourceSourceStage source = new DOMResourceSourceStage();
        source.setId("test");
        source.setDOMResource(mdResource);
        source.setParserPool(parserPool);
        source.initialize();

        try {
            final ArrayList<DOMElementItem> metadataCollection = new ArrayList<>();
            source.execute(metadataCollection);
            throw new ConstraintViolationException("Invalid URL marked as parsed");
        } catch (StageProcessingException e) {
            // expected this
        }
    }

    @Test public void testFailedFetch() throws Exception {
        HttpResource mdResource = buildHttpResource("http://kslkjf.com/lkjlk3.dlw");

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

    protected HttpResource buildHttpResource(String url) throws Exception {
        HttpClientBuilder builder = new HttpClientBuilder();
        builder.setConnectionDisregardSslCertificate(true);
        builder.setConnectionStalecheck(false);

        File tmp = File.createTempFile(Long.toString(System.currentTimeMillis()), null);
        tmp.deleteOnExit();

        return new HttpResource(builder.buildClient(), url);
    }
}