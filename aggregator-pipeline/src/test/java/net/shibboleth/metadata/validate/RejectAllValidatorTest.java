
package net.shibboleth.metadata.validate;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.metadata.validate.Validator.Action;

public class RejectAllValidatorTest {

    @Test
    public void validate() throws Exception {
        final RejectAllValidator<String> v = new RejectAllValidator<>();
        v.setId("comp");
        v.initialize();

        final Item<String> item = new MockItem("test");
        final Validator.Action action = v.validate("foo", item, "stage");
        Assert.assertEquals(action, Action.DONE);

        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 1);

        final ErrorStatus err = errs.get(0);
        Assert.assertEquals(err.getComponentId(), "stage/comp");
        Assert.assertEquals(err.getStatusMessage(), "value rejected: 'foo'");
    }

    @Test
    public void validateWithMessage() throws Exception {
        final RejectAllValidator<Double> v = new RejectAllValidator<>();
        v.setId("comp");
        v.setMessage("decimal %.2f");
        v.initialize();

        final Item<String> item = new MockItem("test");
        final Validator.Action action = v.validate(new Double(1.25), item, "stage");
        Assert.assertEquals(action, Action.DONE);

        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 1);

        final ErrorStatus err = errs.get(0);
        Assert.assertEquals(err.getComponentId(), "stage/comp");
        Assert.assertEquals(err.getStatusMessage(), "decimal 1.25");
    }

}
