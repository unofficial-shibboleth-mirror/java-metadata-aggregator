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

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.EntityAttributeContext;
import net.shibboleth.metadata.dom.saml.mdattr.EntityAttributeFilteringStage.ContextImpl;

/**
 * This is the same as {@link AssuranceCertificationMatcherTest}, but pulls the matcher beans
 * from a configured application context. This is just to make sure that Spring can
 * distinguish between the two constructor signatures properly, so we don't need an
 * equivalent for every class under test.
 *
 * In addition, each matcher is tested against an expanded implementation typical of what
 * was required before this new matcher class was introduced.
 */
@ContextConfiguration("AssuranceCertificationMatcherSpringTest-config.xml")
public class AssuranceCertificationMatcherSpringTest extends AbstractTestNGSpringContextTests {

    private void test(final boolean expected,
            final Predicate<EntityAttributeContext> matcher,
            final Predicate<EntityAttributeContext> oldMatcher,
            final EntityAttributeContext context) {
        Assert.assertEquals(matcher.test(context), expected, context.toString());
        Assert.assertEquals(oldMatcher.test(context), expected, context.toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNoRA() {
        final var matcher = applicationContext.getBean("certificationMatcherNoRA", Predicate.class);
        final var oldMatcher = applicationContext.getBean("oldMatcherNoRA", Predicate.class);

        // all four components match
        test(true, matcher, oldMatcher,
                new ContextImpl("category", "urn:oasis:names:tc:SAML:attribute:assurance-certification",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "whatever"));

        // context has no RA
        test(true, matcher, oldMatcher,
                new ContextImpl("category", "urn:oasis:names:tc:SAML:attribute:assurance-certification",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", null));

        // these matches should fail because one component differs
        test(false, matcher, oldMatcher,
                new ContextImpl("category2", "urn:oasis:names:tc:SAML:attribute:assurance-certification",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", null));
        test(false, matcher, oldMatcher,
                new ContextImpl("category", "http://macedir.org/entity-category-support",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", null));
        test(false, matcher, oldMatcher,
                new ContextImpl("category", "urn:oasis:names:tc:SAML:attribute:assurance-certification",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified", null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWithRA() {
        final var matcher = applicationContext.getBean("certificationMatcherWithRA", Predicate.class);
        final var oldMatcher = applicationContext.getBean("oldMatcherWithRA", Predicate.class);

        // all four components match
        test(true, matcher, oldMatcher,
                new ContextImpl("category", "urn:oasis:names:tc:SAML:attribute:assurance-certification",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "registrar"));

        // context has no RA
        test(false, matcher, oldMatcher,
                new ContextImpl("category", "http://macedir.org/entity-category",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", null));

        // these matches should fail because one component differs
        test(false, matcher, oldMatcher,
                new ContextImpl("category2", "urn:oasis:names:tc:SAML:attribute:assurance-certification",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "registrar"));
        test(false, matcher, oldMatcher,
                new ContextImpl("category", "http://macedir.org/entity-category-support",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "registrar"));
        test(false, matcher, oldMatcher,
                new ContextImpl("category", "urn:oasis:names:tc:SAML:attribute:assurance-certification",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified", "registrar"));
        test(false, matcher, oldMatcher,
                new ContextImpl("category", "urn:oasis:names:tc:SAML:attribute:assurance-certification",
                "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "registrar2"));
    }

}
