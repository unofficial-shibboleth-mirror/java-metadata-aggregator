
package net.shibboleth.metadata.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Function;

public class PathSegmentStringTransformerTest {
    
    private static final Function<String, String> transform =
            new PathSegmentStringTransformer();

    @Test public void testMDQExample() {
        Assert.assertEquals(transform.apply("http://example.org/idp"),
                "http:%2F%2Fexample.org%2Fidp");
    }

    @Test public void testSpace() {
        Assert.assertEquals(transform.apply("a b"), "a%20b");
    }
    
    // https://www.talisman.org/~erlkonig/misc/lunatech%5Ewhat-every-webdev-must-know-about-url-encoding/#Thereservedcharactersaredifferentforeachpart
    @Test public void testBlue() {
        Assert.assertEquals(transform.apply("blue+light blue"),
                "blue+light%20blue");
    }

    @Test public void testMDQExample2() {
        Assert.assertEquals(transform.apply("blue/green+light blue"),
                "blue%2Fgreen+light%20blue");
    }
    @Test public void testPercent() {
        Assert.assertEquals(transform.apply("%"), "%25");
    }
}
