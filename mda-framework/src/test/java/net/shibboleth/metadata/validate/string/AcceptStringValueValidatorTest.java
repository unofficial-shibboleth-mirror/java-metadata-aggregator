
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

public class AcceptStringValueValidatorTest {

    @Test
    public void validateMatch() throws Exception {
        final AcceptStringValueValidator v = new AcceptStringValueValidator();
        v.setId("comp");
        v.setValue("match");
        v.initialize();

        final Item<String> item = new MockItem("content");
        final Validator.Action action = v.validate("match", item, "stage");
        Assert.assertEquals(action, Action.DONE);

        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 0);
    }

    @Test
    public void validateMismatch() throws Exception {
        final AcceptStringValueValidator v = new AcceptStringValueValidator();
        v.setId("comp");
        v.setValue("match");
        v.initialize();

        final Item<String> item = new MockItem("content");
        final Validator.Action action = v.validate("no match", item, "stage");
        Assert.assertEquals(action, Action.CONTINUE);

        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 0);
    }

    @Test(expectedExceptions = ComponentInitializationException.class)
    public void testNoValue() throws Exception {
        final var val = new AcceptStringValueValidator();
        val.setId("test");
        val.initialize();
    }
}
