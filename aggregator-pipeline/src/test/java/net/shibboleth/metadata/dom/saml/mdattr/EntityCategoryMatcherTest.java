
package net.shibboleth.metadata.dom.saml.mdattr;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;

import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.EntityAttributeContext;
import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.SimpleEntityAttributeContext;

public class EntityCategoryMatcherTest {

    private void test(final boolean expected, final Predicate<EntityAttributeContext> matcher,
            final EntityAttributeContext context) {
        Assert.assertEquals(matcher.apply(context), expected, context.toString());
    }
    
    @Test
    public void testNoRA() {
        final Predicate<EntityAttributeContext> matcher = new EntityCategoryMatcher("category");
        
        // all four components match
        test(true, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "whatever"));

        // context has no RA
        test(true, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", null));

        // these matches should fail because one component differs
        test(false, matcher, new SimpleEntityAttributeContext("category2", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", null));
        test(false, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category-support",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", null));
        test(false, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified", null));
    }

    @Test
    public void testWithRA() {
        final Predicate<EntityAttributeContext> matcher = new EntityCategoryMatcher("category", "registrar");
        
        // all four components match
        test(true, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "registrar"));

        // context has no RA
        test(false, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", null));

        // these matches should fail because one component differs
        test(false, matcher, new SimpleEntityAttributeContext("category2", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "registrar"));
        test(false, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category-support",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "registrar"));
        test(false, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified", "registrar"));
        test(false, matcher, new SimpleEntityAttributeContext("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "registrar2"));
    }

}
