
package net.shibboleth.metadata.pipeline;

import java.io.File;
import java.io.OutputStream;

import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.io.Files;

import junit.framework.Assert;
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
        Assert.assertEquals(1, files.length);
        Assert.assertEquals(expectedName, files[0].getName());
    }

    // Test with a prefix, a suffix, and an ID transform that doubles the name
    @Test public void testFull() throws Exception {
        final File tempDir = Files.createTempDir();
        //System.out.println("temp dir: " + tempDir.getAbsolutePath());
        
        final FilesInDirectoryMultiOutputStrategy<String> strategy = new FilesInDirectoryMultiOutputStrategy<>();
        strategy.setDirectory(tempDir);
        strategy.setNamePrefix("pre");
        strategy.setNameTransform(new Function<String, String>() {
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
        final File tempDir = Files.createTempDir();
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
