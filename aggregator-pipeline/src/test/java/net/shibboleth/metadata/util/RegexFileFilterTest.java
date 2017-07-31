package net.shibboleth.metadata.util;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

/** {@link RegexFileFilter} unit tests. */
public class RegexFileFilterTest {

    /**
     * Test acceptance of files corresponding to the UK federation use case.
     */
    @Test
    public void testAccept() {
        final RegexFileFilter a = new RegexFileFilter("uk\\d{6}\\.xml");
        Assert.assertTrue(a.accept(new File("uk123456.xml")));
        Assert.assertTrue(a.accept(new File("foo/uk123456.xml")));
        Assert.assertFalse(a.accept(new File("imported.xml")));
        Assert.assertFalse(a.accept(new File("uk123456.new")));
        Assert.assertFalse(a.accept(new File("uk123456.xml~")));
        Assert.assertFalse(a.accept(new File("x-123456.xml")));
        Assert.assertFalse(a.accept(new File("foo/baruk123456.xml")));
        Assert.assertFalse(a.accept(new File("uk123456.xml/bar")));
    }

}
