/*
 * Licensed to the University Corporation for Advanced Internet Development, Inc.
 * under one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache 
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

import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.StageProcessingException;

import org.opensaml.util.xml.BasicParserPool;
import org.testng.annotations.Test;

/**
 *
 */
public class DomFilesystemSourceTest {

    @Test
    public void testSuccessfulFileFetchAndParse() throws Exception {
        URL sourceUrl = DomFilesystemSourceTest.class.getResource("/data/samlMetadata/entityDescriptor1.xml");
        File sourceFile = new File(sourceUrl.toURI());

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DomFilesystemSourceStage source = new DomFilesystemSourceStage();
        source.setId("test");
        source.setParserPool(parserPool);
        source.setSource(sourceFile);

        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        source.execute(metadataCollection);
        assert metadataCollection != null;
        assert metadataCollection.size() == 1;
    }

    @Test
    public void testSuccessfulDirectoryFetchAndParse() throws Exception {
        URL sourceUrl = DomFilesystemSourceTest.class.getResource("/data/samlMetadata");
        File sourceFile = new File(sourceUrl.toURI());

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DomFilesystemSourceStage source = new DomFilesystemSourceStage();
        source.setId("test");
        source.setParserPool(parserPool);
        source.setSource(sourceFile);

        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        source.execute(metadataCollection);
        assert metadataCollection != null;
        assert metadataCollection.size() == 4;
    }

    public void testSuccessfulDirectoryFetchWithFilterAndParse() throws Exception {
        URL sourceUrl = DomFilesystemSourceTest.class.getResource("/data/samlMetadata/entityDescriptor1.xml");
        File sourceFile = new File(sourceUrl.toURI());

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DomFilesystemSourceStage source = new DomFilesystemSourceStage();
        source.setId("test");
        source.setParserPool(parserPool);
        source.setSource(sourceFile);
        source.setRecurseDirectories(true);
        source.setSourceFileFilter(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.getName().endsWith("xml");
            }
        });

        ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
        source.execute(metadataCollection);
        assert metadataCollection != null;
        assert metadataCollection.size() == 7;
    }

    @Test
    public void testSuccessfulFetchAndFailedParse() throws Exception {
        URL sourceUrl = DomFilesystemSourceTest.class.getResource("/data/loremIpsum.txt");
        File sourceFile = new File(sourceUrl.toURI());

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DomFilesystemSourceStage source = new DomFilesystemSourceStage();
        source.setId("test");
        source.setParserPool(parserPool);
        source.setSource(sourceFile);

        try {
            ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
            source.execute(metadataCollection);
            throw new AssertionError("Source did not fail when given a non-XML file");
        } catch (StageProcessingException e) {
            // expected this
        }
    }

    @Test
    public void testFailedFetch() throws Exception {
        File sourceFile = new File("nonExistant");

        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        DomFilesystemSourceStage source = new DomFilesystemSourceStage();
        source.setId("test");
        source.setParserPool(parserPool);
        source.setSource(sourceFile);

        try {
            ArrayList<DomElementItem> metadataCollection = new ArrayList<DomElementItem>();
            source.execute(metadataCollection);
        } catch (StageProcessingException e) {
            throw new AssertionError("Source did failed when given a nonexistant file");
        }
    }
}