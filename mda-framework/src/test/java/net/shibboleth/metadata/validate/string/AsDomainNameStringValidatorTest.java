
package net.shibboleth.metadata.validate.string;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.net.InternetDomainName;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.metadata.validate.BaseValidator;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.metadata.validate.Validator.Action;

public class AsDomainNameStringValidatorTest {

    private static class CountingValidator extends BaseValidator implements Validator<InternetDomainName> {
        public int count;
        private final @Nonnull Action action;

        @Override
        public @Nonnull Action validate(@Nonnull InternetDomainName e, @Nonnull Item<?> item, @Nonnull String stageId)
                throws StageProcessingException {
            count++;
            return action;
        }

        /** Constructor. */
        public CountingValidator(final @Nonnull Action a) {
            action = a;
        }
    }

    @Test
    public void testOK() throws Exception {
        final CountingValidator counter = new CountingValidator(Action.CONTINUE);
        counter.setId("counter");
        counter.initialize();

        final List<Validator<InternetDomainName>> nestedSequence = new ArrayList<>();
        nestedSequence.add(counter);

        final Item<String> item = new MockItem("content");

        final AsDomainNameStringValidator val = new AsDomainNameStringValidator();
        val.setId("id");
        val.setValidators(nestedSequence);
        val.initialize();

        final Action res = val.validate("example.org", item, "stage");
        Assert.assertEquals(res, Action.CONTINUE);
        Assert.assertEquals(item.getItemMetadata().get(ErrorStatus.class).size(), 0);
        Assert.assertEquals(counter.count, 1);
    }

    @Test
    public void testNoConvertDefault() throws Exception {
        final CountingValidator counter = new CountingValidator(Action.CONTINUE);
        counter.setId("counter");
        counter.initialize();

        final List<Validator<InternetDomainName>> nestedSequence = new ArrayList<>();
        nestedSequence.add(counter);

        final Item<String> item = new MockItem("content");

        final AsDomainNameStringValidator val = new AsDomainNameStringValidator();
        val.setId("id");
        val.setValidators(nestedSequence);
        val.setMessage("quick brown %s");
        val.initialize();

        final Action res = val.validate("example**.org", item, "stage");
        Assert.assertEquals(res, Action.DONE);
        Assert.assertEquals(item.getItemMetadata().get(ErrorStatus.class).size(), 1);
        final ErrorStatus err = item.getItemMetadata().get(ErrorStatus.class).get(0);
        Assert.assertTrue(err.getStatusMessage().contains("quick brown example**.org"));
        Assert.assertEquals(counter.count, 0);
    }

    @Test
    public void testNoConvertFalse() throws Exception {
        final CountingValidator counter = new CountingValidator(Action.CONTINUE);
        counter.setId("counter");
        counter.initialize();

        final List<Validator<InternetDomainName>> nestedSequence = new ArrayList<>();
        nestedSequence.add(counter);

        final Item<String> item = new MockItem("content");

        final AsDomainNameStringValidator val = new AsDomainNameStringValidator();
        val.setId("id");
        val.setConversionRequired(false);
        val.setValidators(nestedSequence);
        val.initialize();

        final Action res = val.validate("example**.org", item, "stage");
        Assert.assertEquals(res, Action.CONTINUE);
        Assert.assertEquals(item.getItemMetadata().get(ErrorStatus.class).size(), 0);
        Assert.assertEquals(counter.count, 0);
    }

}
