
package net.shibboleth.metadata;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for the {@link Version} class.
 *
 * <p>
 * It's hard to do this justice because the package metadata the class
 * depends on isn't present in the test environment. We can at least
 * make sure that there are no exceptions thrown.
 * </p>
 */
public class VersionTest {

    @Test
    public void getMajorVersionTest() {
        Assert.assertEquals(Version.getMajorVersion(), 0);
    }

    @Test
    public void getMinorVersionTest() {
        Assert.assertEquals(Version.getMinorVersion(), 0);
    }

    @Test
    public void getPatchVersionTest() {
        Assert.assertEquals(Version.getPatchVersion(), 0);
    }

    @Test
    public void getVersionTest() {
        Assert.assertEquals(Version.getVersion(), "unknown");
    }

    @Test
    public void mainTest() {
        Version.main(new String[] {});
    }
}
