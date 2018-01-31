
package net.shibboleth.metadata.validate;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.validate.Validator.Action;

public class BaseValidatorTest {

    /**
     * Test String validator which always rejects.
     */
    private class BoomValidator extends BaseValidator implements Validator<String> {

        public Action validate(String e, Item<?> item, String stageId) {
            addErrorMessage(e, item, stageId);
            return Action.DONE;
        }

    }

    @Test
    public void getMessage() throws Exception {
        final BoomValidator b = new BoomValidator();
        b.setId("test");
        b.initialize();
        Assert.assertEquals(b.getMessage(), "value rejected: '%s'");
        final Item<String> item = new MockItem("test");
        Assert.assertEquals(b.validate("foo", item, "stage"), Action.DONE);
        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 1);
        Assert.assertEquals(errs.get(0).getStatusMessage(), "value rejected: 'foo'");
    }

    @Test
    public void setMessage() throws Exception {
        final BoomValidator b = new BoomValidator();
        b.setId("test");
        b.setMessage("%s is bad");
        b.initialize();
        Assert.assertEquals(b.getMessage(), "%s is bad");
        final Item<String> item = new MockItem("test");
        Assert.assertEquals(b.validate("foo", item, "stage"), Action.DONE);
        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 1);
        Assert.assertEquals(errs.get(0).getStatusMessage(), "foo is bad");
    }
}
