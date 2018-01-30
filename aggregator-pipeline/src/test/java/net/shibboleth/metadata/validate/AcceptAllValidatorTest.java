
package net.shibboleth.metadata.validate;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.metadata.validate.Validator.Action;

public class AcceptAllValidatorTest {

    @Test
    public void validate() throws Exception {
        final AcceptAllValidator<String> v = new AcceptAllValidator<>();
        v.setId("comp");
        v.initialize();

        final Item<String> item = new MockItem("test");
        final Validator.Action action = v.validate("foo", item, "stage");
        Assert.assertEquals(action, Action.DONE);

        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 0);
    }

}
