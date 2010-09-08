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

package edu.internet2.middleware.shibboleth.metadata.dom;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.opensaml.util.http.HttpClientBuilder;
import org.opensaml.util.http.HttpResource;
import org.opensaml.util.xml.StaticBasicParserPool;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import edu.internet2.middleware.shibboleth.metadata.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.dom.source.DomHttpSource;
import edu.internet2.middleware.shibboleth.metadata.pipeline.SourceProcessingException;

/**
 *
 */
public class DomHttpSourceTest {

    @Test
    public void testSuccessfulFetchAndParse() throws Exception {
        HttpResource mdResource = buildHttpResource("http://metadata.ukfederation.org.uk/ukfederation-metadata.xml");

        StaticBasicParserPool parserPool = new StaticBasicParserPool();
        parserPool.initialize();

        DomHttpSource source = new DomHttpSource("test", mdResource, parserPool);
        assert source.getCachedMetadata() == null;

        MetadataCollection<DomMetadata> mc = source.execute();
        Document metadata = source.getCachedMetadata();
        assert mc != null;
        assert mc.size() == 1;
        assert metadata != null;

        mc = source.execute();
        assert mc != null;
        assert mc.size() == 1;
        assert source.getCachedMetadata() == metadata;
    }

    @Test
    public void testSuccessfulFetchAndFailedParse() throws Exception {
        HttpResource mdResource = buildHttpResource("http://www.google.com/intl/en/images/about_logo.gif");

        StaticBasicParserPool parserPool = new StaticBasicParserPool();
        parserPool.initialize();

        DomHttpSource source = new DomHttpSource("test", mdResource, parserPool);

        try {
            source.execute();
            throw new AssertionError("Invalid URL marked as parsed");
        } catch (SourceProcessingException e) {
            // expected this
        }
    }

    @Test
    public void testFailedFetch() throws Exception {
        HttpResource mdResource = buildHttpResource("http://kslkjf.com/lkjlk3.dlw");

        StaticBasicParserPool parserPool = new StaticBasicParserPool();
        parserPool.initialize();

        DomHttpSource source = new DomHttpSource("test", mdResource, parserPool);

        try {
            source.execute();
            throw new AssertionError("Invalid URL processed");
        } catch (SourceProcessingException e) {
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

        return new HttpResource(url, builder.buildClient(), tmp.getAbsolutePath());
    }
}