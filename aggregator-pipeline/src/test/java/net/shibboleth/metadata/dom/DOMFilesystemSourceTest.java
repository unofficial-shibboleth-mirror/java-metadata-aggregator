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
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;

import net.shibboleth.metadata.BaseTest;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** Unit test for {@link DOMFilesystemSourceStage}. */
public class DOMFilesystemSourceTest extends BaseTest {

    @BeforeClass
    private void init() {
        setTestingClass(DOMFilesystemSourceStage.class);
    }

    @Test public void testSuccessfulFileFetchAndParse() throws Exception {
        URL sourceUrl = getClasspathResource("in.xml").getURL();
        File sourceFile = new File(sourceUrl.toURI());

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DOMFilesystemSourceStage source = new DOMFilesystemSourceStage();
        source.setId("test");
        source.setParserPool(parserPool);
        source.setSource(sourceFile);
        source.initialize();

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        source.execute(metadataCollection);
        Assert.assertNotNull(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 1);
    }

    @Test public void testSuccessfulDirectoryFetchAndParse() throws Exception {
        URL sourceUrl = DOMFilesystemSourceTest.class.getResource("/data/samlMetadata");
        File sourceFile = new File(sourceUrl.toURI());

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DOMFilesystemSourceStage source = new DOMFilesystemSourceStage();
        source.setId("test");
        source.setParserPool(parserPool);
        source.setSource(sourceFile);
        source.initialize();

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        source.execute(metadataCollection);
        Assert.assertNotNull(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 6);
    }

    public void testSuccessfulDirectoryFetchWithFilterAndParse() throws Exception {
        URL sourceUrl = DOMFilesystemSourceTest.class.getResource("/data/samlMetadata/entityDescriptor1.xml");
        File sourceFile = new File(sourceUrl.toURI());

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DOMFilesystemSourceStage source = new DOMFilesystemSourceStage();
        source.setId("test");
        source.setParserPool(parserPool);
        source.setSource(sourceFile);
        source.setRecurseDirectories(true);
        source.setSourceFileFilter(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.getName().endsWith("xml");
            }
        });
        source.initialize();

        final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
        source.execute(metadataCollection);
        Assert.assertNotNull(metadataCollection);
        Assert.assertEquals(metadataCollection.size(), 7);
    }

    @Test public void testSuccessfulFetchAndFailedParse() throws Exception {
        final URL sourceUrl = getClasspathResource("lorem.txt").getURL();
        File sourceFile = new File(sourceUrl.toURI());

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DOMFilesystemSourceStage source = new DOMFilesystemSourceStage();
        source.setId("test");
        source.setParserPool(parserPool);
        source.setSource(sourceFile);
        source.initialize();

        try {
            final ArrayList<Item<Element>> metadataCollection = new ArrayList<>();
            source.execute(metadataCollection);
            throw new ConstraintViolationException("Source did not fail when given a non-XML file");
        } catch (StageProcessingException e) {
            // expected this
        }
    }

    @Test public void testFailedFetch() throws Exception {
        File sourceFile = new File("nonExistant");

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DOMFilesystemSourceStage source = new DOMFilesystemSourceStage();
        source.setId("test");
        source.setParserPool(parserPool);
        source.setSource(sourceFile);
        source.setNoSourceFilesAnError(true);

        try {
            source.initialize();
            Assert.fail();
        } catch (ComponentInitializationException e) {
            // expected this
        }
    }
}