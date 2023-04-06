
package net.shibboleth.metadata.validate;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.metadata.validate.Validator.Action;

public class ValidatorSequenceTest {

    @Test
    public void testNoValidators() throws Exception {
        final ValidatorSequence<String> v = new ValidatorSequence<>();
        v.setId("seq");
        v.initialize();

        final Item<String> item = new MockItem("content");
        final Validator.Action action = v.validate("anything", item, "stage");
        Assert.assertEquals(action,  Action.CONTINUE);

        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 0);
    }

    @Test
    public void testAcceptReject() throws Exception {
        final AcceptAllValidator<String> accept = new AcceptAllValidator<>();
        accept.setId("accept");
        accept.initialize();

        final RejectAllValidator<String> reject = new RejectAllValidator<>();
        reject.setId("reject");
        reject.initialize();

        final List<Validator<String>> vv = new ArrayList<>();
        vv.add(accept);
        vv.add(reject);

        final ValidatorSequence<String> v = new ValidatorSequence<>();
        v.setId("seq");
        v.setValidators(vv);
        v.initialize();

        final Item<String> item = new MockItem("content");
        final Validator.Action action = v.validate("anything", item, "stage");
        Assert.assertEquals(action,  Action.DONE);

        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 0);
    }

    @Test
    public void testRejectAccept() throws Exception {
        final AcceptAllValidator<String> accept = new AcceptAllValidator<>();
        accept.setId("accept");
        accept.initialize();

        final RejectAllValidator<String> reject = new RejectAllValidator<>();
        reject.setId("reject");
        reject.initialize();

        final List<Validator<String>> vv = new ArrayList<>();
        vv.add(reject);
        vv.add(accept);

        final ValidatorSequence<String> v = new ValidatorSequence<>();
        v.setId("seq");
        v.setValidators(vv);
        v.initialize();

        final Item<String> item = new MockItem("content");
        final Validator.Action action = v.validate("anything", item, "stage");
        Assert.assertEquals(action,  Action.DONE);

        final List<ErrorStatus> errs = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errs.size(), 1);
        final ErrorStatus err = errs.get(0);
        Assert.assertEquals(err.getStatusMessage(), "value rejected: 'anything'");
        Assert.assertEquals(err.getComponentId(), "stage/reject");
    }

}
