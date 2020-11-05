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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import net.shibboleth.metadata.BaseTest;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;

public class DOMResourceSourceStageTest extends BaseTest {

    DOMResourceSourceStageTest() {
        super(DOMResourceSourceStage.class);
    }

    BasicParserPool parserPool;
    
    @BeforeClass void initialize() throws Exception {
        parserPool = new BasicParserPool();
        parserPool.initialize();
    }
    
    @Test public void testSuccessfulFetchAndParse() throws Exception {
        Resource mdResource = new ByteArrayResource("<test/>".getBytes("UTF-8"));

        DOMResourceSourceStage source = new DOMResourceSourceStage();
        source.setId("test");
        source.setDOMResource(mdResource);
        source.setParserPool(parserPool);
        source.initialize();

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        source.execute(metadataCollection);
        source.destroy();
        Assert.assertNotNull(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 1);
    }

    @Test public void testFailedParse() throws Exception {
        Resource mdResource = new ByteArrayResource("this is not valid XML".getBytes("UTF-8"));

        DOMResourceSourceStage source = new DOMResourceSourceStage();
        final String stageIdentifier = "testStage";
        source.setId(stageIdentifier);
        source.setDOMResource(mdResource);
        source.setParserPool(parserPool);
        source.initialize();

        try {
            final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
            source.execute(metadataCollection);
            Assert.fail("Invalid resource reported as parsed");
        } catch (StageProcessingException e) {
            // expected this
            final String message = e.getMessage();
            Assert.assertTrue(message.contains(stageIdentifier), "message should contain stage identifier");
            Assert.assertNotNull(e.getCause(), "exception should have cause");
            Assert.assertTrue(message.contains(mdResource.getDescription()),
                    "message should contain resource description");
        }
    }

    @Test public void testFailedFetch() throws Exception {
        final var collection = new ArrayList<Item<Element>>();
        final var mdResource = new UrlResource("http://kslkjf.com/lkjlk3.dlw");

        final var source = new DOMResourceSourceStage();
        source.setId("test");
        source.setDOMResource(mdResource);
        source.setParserPool(parserPool);
        source.initialize();

        try {
            source.execute(collection);
            Assert.fail();
        } catch (final StageProcessingException e) {
            final var cause = e.getCause();
            // System.out.println("Message: " + e.getMessage());
            // System.out.println("Cause: " + cause);
            Assert.assertNotNull(cause, "exception had no cause");
            Assert.assertTrue(cause instanceof IOException, "cause should have been an IOException");
        }
    }
    
    @Test
    public void testMDA130() throws Exception {
        /*
         * Mock up a resource which claims to exist, but then throws an
         * exception on access. This kind of thing tends to happen with
         * HTTP resources. MDA-130 reports that this causes an NPE.
         */
        final Resource mockResource = mock(Resource.class);
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.getInputStream()).thenThrow(new IOException());

        DOMResourceSourceStage source = new DOMResourceSourceStage();
        source.setId("test");
        source.setDOMResource(mockResource);
        source.setParserPool(parserPool);
        source.initialize();

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        try {
            source.execute(metadataCollection);
            Assert.fail(); // expected an exception to be thrown
        } catch (StageProcessingException e) {
            // this is the exception we expect
        }
        Assert.assertNotNull(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 0);
    }

    @Test
    public void mda219() throws Exception {
        final var stage = new DOMResourceSourceStage();
        final var resource = getClasspathResource("does-not-exist.xml");
        final var collection = new ArrayList<Item<Element>>();
        stage.setId("test");
        stage.setParserPool(parserPool);
        stage.setDOMResource(resource);
        // Pre-MDA-219, this will throw an exception
        stage.initialize();
        
        // Post-MDA-219, we expect a wrapped IOException when we execute the stage.
        try {
            stage.execute(collection);
            Assert.fail("expected exception");
        } catch (final StageProcessingException e) {
            final var cause = e.getCause();
            // System.out.println("Message: " + e.getMessage());
            // System.out.println("Cause: " + cause);
            Assert.assertNotNull(cause, "exception had no cause");
            Assert.assertTrue(cause instanceof IOException, "cause should have been an IOException");
        }
    }
}
