
package net.shibboleth.metadata.dom.saml.mdattr;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;

import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.EntityAttributeContext;
import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.ContextImpl;

public class RegistrationAuthorityMatcherTest {

    private void test(final boolean expected, final Predicate<EntityAttributeContext> matcher,
            final EntityAttributeContext context) {
        Assert.assertEquals(matcher.apply(context), expected, context.toString());
    }
    
    @Test
    public void testWithRA() {
        final Predicate<EntityAttributeContext> matcher = new RegistrationAuthorityMatcher("registrar");
        
        test(true, matcher, new ContextImpl("a", "b", "c", "registrar"));
        test(false, matcher, new ContextImpl("a", "b", "c", "registrar2"));
        test(false, matcher, new ContextImpl("a", "b", "c", null));
    }

    @Test
    public void testNoRA() {
        final Predicate<EntityAttributeContext> matcher = new RegistrationAuthorityMatcher(null);
        
        test(false, matcher, new ContextImpl("a", "b", "c", "registrar"));
        test(false, matcher, new ContextImpl("a", "b", "c", "registrar2"));
        test(true, matcher, new ContextImpl("a", "b", "c", null));
    }

}
