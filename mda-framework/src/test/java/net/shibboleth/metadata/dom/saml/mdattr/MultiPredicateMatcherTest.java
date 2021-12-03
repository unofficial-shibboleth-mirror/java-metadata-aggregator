/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package net.shibboleth.metadata.dom.saml.mdattr;

import java.util.function.Predicate;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.ContextImpl;
import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.EntityAttributeContext;

public class MultiPredicateMatcherTest {

    private final EntityAttributeContext context =
            new ContextImpl("valuevalue", "namename", "fmtfmt", "regreg");

    @Test
    public void testNothing() {
        final Predicate<EntityAttributeContext> matcher = new MultiPredicateMatcher();
        Assert.assertTrue(matcher.test(context));
    }
    
    @Test
    public void setNameFormatPredicate() {
        final MultiPredicateMatcher matcher = new MultiPredicateMatcher();
        matcher.setNameFormatPredicate(x -> x.toString().contains("tfm"));
        Assert.assertTrue(matcher.test(context));
        matcher.setNameFormatPredicate(x -> x.toString().contains("xxx"));
        Assert.assertFalse(matcher.test(context));
    }

    @Test
    public void setNamePredicate() {
        final MultiPredicateMatcher matcher = new MultiPredicateMatcher();
        matcher.setNamePredicate(x -> x.toString().contains("ena"));
        Assert.assertTrue(matcher.test(context));
        matcher.setNamePredicate(x -> x.toString().contains("xxx"));
        Assert.assertFalse(matcher.test(context));
    }

    @Test
    public void setRegistrationAuthorityPredicate() {
        final MultiPredicateMatcher matcher = new MultiPredicateMatcher();
        matcher.setRegistrationAuthorityPredicate(x -> x.toString().contains("egr"));
        Assert.assertTrue(matcher.test(context));
        matcher.setRegistrationAuthorityPredicate(x -> x.toString().contains("xxx"));
        Assert.assertFalse(matcher.test(context));
    }

    @Test
    public void setValuePredicate() {
        final MultiPredicateMatcher matcher = new MultiPredicateMatcher();
        matcher.setValuePredicate(x -> x.toString().contains("eva"));
        Assert.assertTrue(matcher.test(context));
        matcher.setValuePredicate(x -> x.toString().contains("xxx"));
        Assert.assertFalse(matcher.test(context));
    }

}
