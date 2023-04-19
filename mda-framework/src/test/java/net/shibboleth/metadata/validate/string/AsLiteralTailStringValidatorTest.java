
package net.shibboleth.metadata.validate.string;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.metadata.validate.BaseValidator;
import net.shibboleth.metadata.validate.Validator;
import net.shibboleth.metadata.validate.Validator.Action;

public class AsLiteralTailStringValidatorTest {

    private static class CountingCapturingValidator extends BaseValidator implements Validator<String> {
        public int count;
        public String value;
        private final @Nonnull Action action;

        @Override
        public @Nonnull Action validate(@Nonnull String e, @Nonnull Item<?> item, @Nonnull String stageId) throws StageProcessingException {
            count++;
            value = e;
            return action;
        }

        /** Constructor. */
        public CountingCapturingValidator(final @Nonnull Action a) {
            action = a;
        }
    }

    @Test
    public void testAssumptions() throws Exception {
        final Pattern pattern = Pattern.compile(".*?\\\\.(([a-zA-Z0-9-]+\\\\.)+[a-zA-Z0-9-]+)\\$");
        final String value = "^([a-zA-Z0-9-]{1,63}\\.){0,2}vho\\.aaf\\.edu\\.au$";
        final Matcher matcher = pattern.matcher(value);
        Assert.assertTrue(matcher.matches());
    }

    @Test
    public void testExample() throws Exception {
        final CountingCapturingValidator ccv = new CountingCapturingValidator(Action.CONTINUE);
        ccv.setId("ccv");
        ccv.initialize();

        final List<Validator<String>> nvs = new ArrayList<>();
        nvs.add(ccv);

        final AsLiteralTailStringValidator val = new AsLiteralTailStringValidator();
        val.setId("val");
        val.setValidators(nvs);
        val.initialize();

        final Item<String> item = new MockItem("content");
        Assert.assertEquals(val.validate("^([a-zA-Z0-9-]{1,63}\\.){0,2}vho\\.aaf\\.edu\\.au$", item, "stage"), Action.CONTINUE);
        Assert.assertEquals(ccv.count, 1);
        Assert.assertEquals(ccv.value, "aaf.edu.au");
    }

    /*
     * Example from the REFEDS MRPS template document.
     *
     * See https://github.com/REFEDS/MRPS/blob/master/MRPS-templatev1.1.pdf
     */
    @Test
    public void testREFEDSExample() throws Exception {
        final CountingCapturingValidator ccv = new CountingCapturingValidator(Action.CONTINUE);
        ccv.setId("ccv");
        ccv.initialize();

        final List<Validator<String>> nvs = new ArrayList<>();
        nvs.add(ccv);

        final AsLiteralTailStringValidator val = new AsLiteralTailStringValidator();
        val.setId("val");
        val.setValidators(nvs);
        val.initialize();

        final Item<String> item = new MockItem("content");
        Assert.assertEquals(val.validate("^(foo|bar)\\.example\\.com$", item, "stage"), Action.CONTINUE);
        Assert.assertEquals(ccv.count, 1);
        Assert.assertEquals(ccv.value, "example.com");
    }
}
