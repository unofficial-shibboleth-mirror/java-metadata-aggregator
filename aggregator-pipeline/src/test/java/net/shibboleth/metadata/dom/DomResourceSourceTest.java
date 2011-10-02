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
import java.io.IOException;
import java.util.ArrayList;

import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.opensaml.util.net.HttpClientBuilder;
import org.opensaml.util.net.HttpResource;
import org.opensaml.util.xml.BasicParserPool;
import org.testng.annotations.Test;

public class DomResourceSourceTest {

    @Test
    public void testSuccessfulFetchAndParse() throws Exception {
        HttpResource mdResource = buildHttpResource("http://metadata.ukfederation.org.uk/ukfederation-metadata.xml");

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DomResourceSourceStage source = new DomResourceSourceStage();
        source.setId("test");
        source.setDomResource(mdResource);
        source.setParserPool(parserPool);

        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        source.execute(metadataCollection);
        assert metadataCollection != null;
        assert metadataCollection.size() == 1;
    }

    @Test
    public void testSuccessfulFetchAndFailedParse() throws Exception {
        HttpResource mdResource = buildHttpResource("http://www.google.com/intl/en/images/about_logo.gif");

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DomResourceSourceStage source = new DomResourceSourceStage();
        source.setId("test");
        source.setDomResource(mdResource);
        source.setParserPool(parserPool);

        try {
            ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
            source.execute(metadataCollection);
            throw new AssertionError("Invalid URL marked as parsed");
        } catch (StageProcessingException e) {
            // expected this
        }
    }

    @Test
    public void testFailedFetch() throws Exception {
        HttpResource mdResource = buildHttpResource("http://kslkjf.com/lkjlk3.dlw");

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DomResourceSourceStage source = new DomResourceSourceStage();
        source.setId("test");
        source.setDomResource(mdResource);
        source.setParserPool(parserPool);

        try {
            ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
            source.execute(metadataCollection);
            throw new AssertionError("Invalid URL processed");
        } catch (StageProcessingException e) {
            // expected this
        }
    }

    protected HttpResource buildHttpResource(String url) throws IOException {
        HttpClientBuilder builder = new HttpClientBuilder();
        builder.setConnectionDisregardSslCertificate(true);
        builder.setConnectionPooling(false);
        builder.setConnectionStalecheck(false);

        File tmp = File.createTempFile(Long.toString(System.currentTimeMillis()), null);
        tmp.deleteOnExit();

        return new HttpResource(builder.buildClient(), url);
    }
}