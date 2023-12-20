
package net.shibboleth.metadata.validate.string;

import javax.annotation.Nonnull;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.metadata.validate.Validator.Action;

/**
 * A litmus test for {@link AsURLStringValidator} involving a set of valid and invalid
 * values.
 *
 * <p>
 * The configuration for the stage is taken from a Spring XML configuration file.
 * </p>
 */
@ContextConfiguration("AsURLStringValidatorLitmusTest-config.xml")
public class AsURLStringValidatorLitmusTest extends AbstractTestNGSpringContextTests {

    /** {@link AsURLStringValidator} to run for each test. */
    private AsURLStringValidator validator;

    @BeforeClass
    private void setUp() throws Exception {
        validator = makeValidator();
    }

    /** Acquire the configured validator from the Spring context. */
    private AsURLStringValidator makeValidator() throws Exception {
        assert applicationContext != null;
        final AsURLStringValidator validator = applicationContext.getBean("litmusTest", AsURLStringValidator.class);
        validator.initialize();
        return validator;
    }

    /**
     * Test a value we expect to be accepted.
     *
     * @param value value to test
     * @throws Exception if something goes wrong
     */
    private void good(@Nonnull final String value) throws Exception {
        final var item = new MockItem("item");
        var result = validator.validate(value, item, "stage");
        Assert.assertEquals(result, Action.CONTINUE);
        Assert.assertTrue(item.getItemMetadata().isEmpty());
    }

    /**
     * Test a value we expect to be rejected.
     *
     * @param value value to test
     * @throws Exception if something goes wrong
     */
    private void bad(@Nonnull final String value) throws Exception {
        final var item = new MockItem("item");
        var result = validator.validate(value, item, "stage");
        Assert.assertEquals(result, Action.DONE);
        final var errors = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 1);
    }

    /**
     * Litmus tests from <code>URLCheckerTest</code>.
     *
     * <p>
     * We want the <em>configured</em> {@link AsURLStringValidator} to have
     * the same behaviour as the old Xalan extension.
     * </p>
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void testsFromURLCheckerTest() throws Exception {
        good("https://example.org:1234/example");
        
        // Non-integer port number
        bad("https://example.org:port/example");

        /**
         * Test the case where the authority's port field is present but empty.
         * 
         * This is valid by the specification, but is regarded as invalid by
         * libxml2's xs:anyURI checker.
         */
        //bad("http://example.org:/example/");
        
        // Doubled scheme looks like empty port field
        //bad("http://http://example.org/example/");
        
        // Bare domain
        bad("www.example.org");
        
        // Empty value
        bad("");
        
        // Missing authority caused by extra slash
        //bad("http:///foo/");
        
        // Import transform artifact
        bad("http://*** FILL IN ***/");

    }
}
