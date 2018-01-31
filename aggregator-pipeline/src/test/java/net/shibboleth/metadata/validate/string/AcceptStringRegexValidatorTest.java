
package net.shibboleth.metadata.validate.string;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.metadata.validate.Validator.Action;

public class AcceptStringRegexValidatorTest {

    @Test
    public void validateMatch() throws Exception {
        final AcceptStringRegexValidator v = new AcceptStringRegexValidator();
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
        final AcceptStringValueValidator v = new AcceptStringValueValidator();
        v.setId("comp");
        v.setValue("a*b");
        v.initialize();

        final Item<String> item = new MockItem("content");
        final Validator.Action action = v.validate("aaaaaaabc", item, "stage");
        Assert.assertEquals(action, Action.CONTINUE);

        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 0);
    }

}
