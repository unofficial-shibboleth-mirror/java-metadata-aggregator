
package net.shibboleth.metadata.validate.string;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.metadata.validate.Validator.Action;
import net.shibboleth.shared.component.ComponentInitializationException;

public class AcceptStringRegexValidatorTest {

    @Test
    public void validateMatch() throws Exception {
        final var v = new AcceptStringRegexValidator();
        v.setId("comp");
        v.setRegex("a*b");
        v.initialize();

        final Item<String> item = new MockItem("content");
        final Validator.Action action = v.validate("aaaab", item, "stage");
        Assert.assertEquals(action, Action.DONE);

        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 0);
    }

    @Test
    public void validateMismatch() throws Exception {
        final var v = new AcceptStringRegexValidator();
        v.setId("comp");
        v.setRegex("a*b");
        v.initialize();

        final Item<String> item = new MockItem("content");
        final Validator.Action action = v.validate("aaaaaaabc", item, "stage");
        Assert.assertEquals(action, Action.CONTINUE);

        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 0);
    }

    @Test(expectedExceptions = ComponentInitializationException.class)
    public void testNoValue() throws Exception {
        final var val = new AcceptStringRegexValidator();
        val.setId("test");
        val.initialize();
    }

}
