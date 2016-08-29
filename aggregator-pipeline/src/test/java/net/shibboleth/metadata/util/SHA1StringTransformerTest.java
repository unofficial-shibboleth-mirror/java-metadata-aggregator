
package net.shibboleth.metadata.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Function;

public class SHA1StringTransformerTest {

    private static final Function<String, String> transform =
            new SHA1StringTransformer();

    @Test public void testMDQExample() {
        Assert.assertEquals(transform.apply("http://example.org/service"),
                "11d72e8cf351eb6c75c721e838f469677ab41bdb");
    }

}
