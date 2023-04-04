
package net.shibboleth.metadata.pipeline;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemCollectionSerializer;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.component.ComponentInitializationException;

public class SerializationStageTest {
    
    private static class StringSerializer implements ItemCollectionSerializer<String> {

        public void serializeCollection(@Nonnull Collection<Item<String>> items,
                @Nonnull OutputStream output) throws IOException {
            for (final var item : items) {
                output.write(item.unwrap().getBytes(StandardCharsets.UTF_8));
                output.write('\n');
            }
        }

    }
    
    @Test
    public void testNormal() throws Exception {
        // Create the output file. Note that the default is to be able to
        // overwrite the output file even if it exists, so that's fine.
        final var file = File.createTempFile("testNormal", null);
        final var path = file.toPath();

        try {
            final var stage = new SerializationStage<String>();
            stage.setId("test");
            stage.setOutputFile(file);
            stage.setSerializer(new StringSerializer());
            stage.initialize();
            
            stage.execute(CollectionSupport.listOf(new MockItem("one"), new MockItem("two")));

            // Read the file back in.
            var text = Files.readString(path);
            Assert.assertEquals(text, "one\ntwo\n");
            
            stage.destroy();
        } finally {
            Files.delete(path);
        }

    }
    
    @Test(expectedExceptions= {ComponentInitializationException.class})
    public void testNoFile() throws Exception {
        final var stage = new SerializationStage<String>();
        stage.setId("test");
        stage.setSerializer(new StringSerializer());
        stage.initialize();
    }
}
