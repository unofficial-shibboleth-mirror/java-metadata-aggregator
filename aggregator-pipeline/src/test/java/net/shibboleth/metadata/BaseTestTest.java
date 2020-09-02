
package net.shibboleth.metadata;

import java.nio.charset.StandardCharsets;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BaseTestTest extends BaseTest {

    /**
     * Constructor.
     */
    protected BaseTestTest() {
        super(BaseTest.class);
    }

    @Test
    public void testReadBytes() throws Exception {
        final var bytes = readBytes("bytes.txt");
        Assert.assertEquals(bytes.length, 6);
        Assert.assertEquals(new String(bytes, StandardCharsets.UTF_8), "hello\n");
    }
}
