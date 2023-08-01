/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.dom.saml;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;
import net.shibboleth.shared.xml.AttributeSupport;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** Unit test for {@link ValidateValidUntilStage}. */
public class ValidateValidUntilStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public ValidateValidUntilStageTest() {
        super(ValidateValidUntilStage.class);
    }

    /**
     * Tests that optional check flag on the stage operates properly.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testOptionalCheck() throws Exception {
        ValidateValidUntilStage stage = new ValidateValidUntilStage();
        stage.setId("test");
        stage.setRequireValidUntil(false);
        stage.initialize();

        DOMElementItem item = buildDomElementItem(Duration.ZERO);
        stage.doExecute(item);
        Assert.assertFalse(item.getItemMetadata().containsKey(ErrorStatus.class));

        stage = new ValidateValidUntilStage();
        stage.setId("test");
        stage.setRequireValidUntil(true);
        stage.initialize();
        
        item = buildDomElementItem(Duration.ZERO);
        stage.doExecute(item);
        Assert.assertTrue(item.getItemMetadata().containsKey(ErrorStatus.class));
    }

    /**
     * Tests that Items within range are not marked as in error and Items outside range are marked as in error.
     * 
     * @throws Exception if something bad happens
     */
    @Test
    public void testValidUntilRangeCheck() throws Exception {
        ValidateValidUntilStage stage = new ValidateValidUntilStage();
        stage.setId("test");
        stage.setRequireValidUntil(false);
        stage.initialize();

        DOMElementItem item = buildDomElementItem(Duration.ofSeconds(10));
        stage.doExecute(item);
        Assert.assertFalse(item.getItemMetadata().containsKey(ErrorStatus.class));

        item = buildDomElementItem(Duration.ofSeconds(-10));
        stage.doExecute(item);
        Assert.assertTrue(item.getItemMetadata().containsKey(ErrorStatus.class));

        item = buildDomElementItem(Duration.ofDays(8));
        stage.doExecute(item);
        Assert.assertTrue(item.getItemMetadata().containsKey(ErrorStatus.class));
    }

    /**
     * Creates a {@link DOMElementItem} that contains a validUntil attribute whose value is now plus a given interval.
     * 
     * @param validUntilInterval interval for the valid until, interval of 0 indicates not to include the validUntil
     *            attribute
     * 
     * @return the created Item
     * 
     * @throws Exception if something bad happens
     */
    private @Nonnull DOMElementItem buildDomElementItem(final Duration validUntilInterval) throws Exception {
        assert validUntilInterval != null;
        Element descriptor = readXMLData("in.xml");
        if (!validUntilInterval.isZero()) {
            final var when = Instant.now().plus(validUntilInterval);
            assert when != null;
            AttributeSupport.appendDateTimeAttribute(descriptor, SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME,
                    when);
        }else{
            AttributeSupport.removeAttribute(descriptor, SAMLMetadataSupport.VALID_UNTIL_ATTRIB_NAME);
        }
        final var doc = descriptor.getOwnerDocument();
        assert doc != null;
        return new DOMElementItem(doc);
    }
}
