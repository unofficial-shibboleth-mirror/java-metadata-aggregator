
package net.shibboleth.metadata.dom.saml.mdattr;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class MultiPredicateMatcherTest {

    private final EntityAttributeContext context =
            new SimpleEntityAttributeContext("valuevalue", "namename", "fmtfmt", "regreg");

    @Test
    public void testNothing() {
        final Predicate<EntityAttributeContext> matcher = new MultiPredicateMatcher();
        Assert.assertTrue(matcher.apply(context));
    }
    
    @Test
    public void setNameFormatPredicate() {
        final MultiPredicateMatcher matcher = new MultiPredicateMatcher();
        matcher.setNameFormatPredicate(Predicates.containsPattern("tfm"));
        Assert.assertTrue(matcher.apply(context));
        matcher.setNameFormatPredicate(Predicates.containsPattern("xxx"));
        Assert.assertFalse(matcher.apply(context));
    }

    @Test
    public void setNamePredicate() {
        final MultiPredicateMatcher matcher = new MultiPredicateMatcher();
        matcher.setNamePredicate(Predicates.containsPattern("ena"));
        Assert.assertTrue(matcher.apply(context));
        matcher.setNamePredicate(Predicates.containsPattern("xxx"));
        Assert.assertFalse(matcher.apply(context));
    }

    @Test
    public void setRegistrationAuthorityPredicate() {
        final MultiPredicateMatcher matcher = new MultiPredicateMatcher();
        matcher.setRegistrationAuthorityPredicate(Predicates.containsPattern("egr"));
        Assert.assertTrue(matcher.apply(context));
        matcher.setRegistrationAuthorityPredicate(Predicates.containsPattern("xxx"));
        Assert.assertFalse(matcher.apply(context));
    }

    @Test
    public void setValuePredicate() {
        final MultiPredicateMatcher matcher = new MultiPredicateMatcher();
        matcher.setValuePredicate(Predicates.containsPattern("eva"));
        Assert.assertTrue(matcher.apply(context));
        matcher.setValuePredicate(Predicates.containsPattern("xxx"));
        Assert.assertFalse(matcher.apply(context));
    }

}
