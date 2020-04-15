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

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.MockItem;

public class FilesInDirectoryMultiOutputStrategyTest {

    private void wipeDirectory(final File dir) {
        for (final File file : dir.listFiles()) {
            //System.out.println("delete: " + file.getAbsolutePath());
            file.delete();
        }
        dir.delete();
    }
    
    private void checkOneFile(final File dir, final String expectedName) {
        final File[] files = dir.listFiles();
        Assert.assertEquals(files.length, 1);
        Assert.assertEquals(files[0].getName(), expectedName);
    }

    // Test with a prefix, a suffix, and an ID transform that doubles the name
    @Test public void testFull() throws Exception {
        final File tempDir = Files.createTempDirectory("FilesInDirectoryMultiOutputStrategyTest").toFile();
        //System.out.println("temp dir: " + tempDir.getAbsolutePath());
        
        final FilesInDirectoryMultiOutputStrategy<String> strategy = new FilesInDirectoryMultiOutputStrategy<>();
        strategy.setDirectory(tempDir);
        strategy.setNamePrefix("pre");
        strategy.setNameTransformer(new Function<String, String>() {
            @Override
            public String apply(String input) {
                return input + input;
            }
            
        });
        strategy.setNameSuffix(".txt");
        strategy.initialize();
        
        final Item<String> item = new MockItem("mocked");
        item.getItemMetadata().put(new ItemId("abc"));
        final MultiOutputSerializationStage.Destination dest = strategy.getDestination(item);
        final OutputStream os = dest.getOutputStream();
        os.write('a');
        os.close();
        dest.close();
        
        checkOneFile(tempDir, "preabcabc.txt");
        wipeDirectory(tempDir);
    }

    // Test with defaults
    @Test public void testDefaults() throws Exception {
        final File tempDir = Files.createTempDirectory("FilesInDirectoryMultiOutputStrategyTest").toFile();
        //System.out.println("temp dir: " + tempDir.getAbsolutePath());

        final FilesInDirectoryMultiOutputStrategy<String> strategy = new FilesInDirectoryMultiOutputStrategy<>();
        strategy.setDirectory(tempDir);
        strategy.initialize();
        
        final Item<String> item = new MockItem("mocked");
        item.getItemMetadata().put(new ItemId("abc"));
        final MultiOutputSerializationStage.Destination dest = strategy.getDestination(item);
        final OutputStream os = dest.getOutputStream();
        os.write('a');
        os.close();
        dest.close();
        
        checkOneFile(tempDir, "abc");
        wipeDirectory(tempDir);
    }

}
