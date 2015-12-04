
package net.shibboleth.metadata.dom.saml.mdattr;


import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleEntityAttributeContextTest {

    @Test
    public void testFour() {
        final EntityAttributeContext ctx = new SimpleEntityAttributeContext("a", "b", "c", "d");
        Assert.assertEquals(ctx.getValue(), "a");
        Assert.assertEquals(ctx.getName(), "b");
        Assert.assertEquals(ctx.getNameFormat(), "c");
        Assert.assertEquals(ctx.getRegistrationAuthority(), "d");
    }
    
    @Test
    public void testThree() {
        final EntityAttributeContext ctx = new SimpleEntityAttributeContext("a", "b", "c");
        Assert.assertEquals(ctx.getValue(), "a");
        Assert.assertEquals(ctx.getName(), "b");
        Assert.assertEquals(ctx.getNameFormat(), "c");
        Assert.assertNull(ctx.getRegistrationAuthority());
    }

    @Test
    public void stringFour() {
        final EntityAttributeContext ctx = new SimpleEntityAttributeContext("a", "b", "c", "d");
        Assert.assertEquals(ctx.toString(), "{v=a, n=b, f=c, r=d}");
    }

    @Test
    public void stringThree() {
        final EntityAttributeContext ctx = new SimpleEntityAttributeContext("a", "b", "c");
        Assert.assertEquals(ctx.toString(), "{v=a, n=b, f=c, r=(none)}");
    }
    
}
