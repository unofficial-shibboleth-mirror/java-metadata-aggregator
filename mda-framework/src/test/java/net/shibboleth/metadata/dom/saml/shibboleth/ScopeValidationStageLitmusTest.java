
package net.shibboleth.metadata.dom.saml.shibboleth;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.pipeline.Stage;
import net.shibboleth.shared.xml.AttributeSupport;
import net.shibboleth.shared.xml.ElementSupport;

/**
 * A litmus test for {@link ScopeValidationStage} involving a set of valid and invalid
 * scope values, both for regular expression and plain cases.
 *
 * <p>
 * The configuration for the stage is taken from a Spring XML configuration file.
 * </p>
 */
@ContextConfiguration("ScopeValidationStageLitmusTest-config.xml")
public class ScopeValidationStageLitmusTest extends AbstractTestNGSpringContextTests {

    /** Build documents using this. */
    private DocumentBuilder dBuilder;

    /** {@link Stage} to run for each test. */
    private Stage<Element> stage;

    @BeforeClass
    private void setUp() throws Exception {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dBuilder = dbFactory.newDocumentBuilder();
        stage = makeStage();
    }

    /** Acquire the configured stage from the Spring context. */
    private Stage<Element> makeStage() throws Exception {
        assert applicationContext != null;
        @SuppressWarnings("unchecked")
        final Stage<Element> stage = applicationContext.getBean("litmusTest", Stage.class);
        stage.initialize();
        return stage;
    }

    /** Build a <code>shibmd:Scope</code> {@link Element}. */
    private Element buildScope(final @Nonnull Document document, final String value, final boolean isRegex) {
        final Element element = ElementSupport.constructElement(document, ShibbolethMetadataSupport.SCOPE_NAME);
        AttributeSupport.appendAttribute(element, ShibbolethMetadataSupport.REGEXP_ATTRIB_NAME,
                isRegex ? "true" : "false");
        element.setTextContent(value);
        return element;
    }

    /** Build a {@link Document} containing an appropriate <code>shibmd:Scope</code> {@link Element}. */
    private @Nonnull Document buildDocument(final String value, final boolean isRegex) {
        final Document document = dBuilder.newDocument();
        document.appendChild(buildScope(document, value, isRegex));
        return document;
    }

    /** Run the test stage on a single {@link Item}. */
    private List<ErrorStatus> runTest(final Item<Element> item) throws Exception {
        final List<Item<Element>> coll = new ArrayList<>();
        coll.add(item);
        stage.execute(coll);
        final List<ErrorStatus> errors = item.getItemMetadata().get(ErrorStatus.class);
        return errors;
    }

    /** Test a value-regexp combination we expect to be accepted. */
    private void good(final String value, final boolean isRegex) throws Exception {
        final Item<Element> item = new DOMElementItem(buildDocument(value, isRegex));
        final List<ErrorStatus> errors = runTest(item);
        if (errors.size() != 0) {
            Assert.fail("expected no errors for '" + value + "'[" + isRegex + "] " +
                "but saw \"" + errors.get(0).getStatusMessage() + "\"");
        }
    }

    /** Test a non-regexp value we expect to be accepted. */
    private void good(final String value) throws Exception {
        good(value, false);
    }

    /** Test a regexp value we expect to be accepted. */
    private void goodRegexp(final String value) throws Exception {
        good(value, true);
    }

    /** Test a value-regexp combination we expect to be rejected. */
    private void bad(final String value, final boolean isRegex, final String why) throws Exception {
        final Item<Element> item = new DOMElementItem(buildDocument(value, isRegex));
        final List<ErrorStatus> errors = runTest(item);
        Assert.assertEquals(errors.size(), 1, "expected an error for '" + value + "'[" + isRegex + "]");
        final ErrorStatus error = errors.get(0);
        final String message = error.getStatusMessage();
        Assert.assertTrue(message.contains(why), "error '" + message + "' didn't contain '" + why + "'");
    }

    /** Test a non-regexp value we expect to be rejected. */
    private void bad(final String value) throws Exception {
        bad(value, false, "");
    }

    /** Test a non-regexp value we expect to be rejected. */
    private void bad(final String value, final String why) throws Exception {
        bad(value, false, why);
    }

    /** Test a regexp value we expect to be rejected. */
    private void badRegexp(final String value, final String why) throws Exception {
        bad(value, true, why);
    }

    /** Test a regexp value we expect to be rejected. */
    private void badRegexp(final String value) throws Exception {
        bad(value, true, "");
    }

    @Test
    public void litmusTests() throws Exception {
        good("example.org");
        good("UGent.be");
        bad("", "empty");
        bad(" ");
        bad("  ");
        bad(" example.org", "white space");
        bad("example.org ", "white space");
        bad("example**.org", "scope is not a valid domain name: example**.org");
        bad("uk", "scope is a public suffix");
        bad("ac.uk", "scope is a public suffix");
        bad("random.nonsense", "scope is not under a public suffix");
        good("example.ac.uk");
        bad("adm.aau.dk@aau.dk"); // incommon/inc-meta#58
        bad("example .org", "white space");
        bad("\nexample.org", "white space");

        badRegexp("", "empty");
        badRegexp(" ");
        badRegexp("  ");
        badRegexp("aaaa$", "does not start with an anchor ('^')");
        badRegexp("^aaaa", "does not end with an anchor ('$')");
        goodRegexp("^([a-zA-Z0-9-]{1,63}\\.){0,2}vho\\.aaf\\.edu\\.au$");
        // don't use literal .s
        badRegexp("^([a-zA-Z0-9-]{1,63}.){0,2}vho.aaf.edu.au$", "does not end with a literal tail");
        // bad literal tail: no public suffix
        badRegexp("^([a-zA-Z0-9-]{1,63}\\.){0,2}vho\\.aaf\\.edu\\.nopublic$", "literal tail is not under a public suffix");
        // bad literal tail: is a public suffix
        badRegexp("^.*\\.ac\\.uk$", "literal tail is a public suffix");

        // UK federation examples
        goodRegexp("^.+\\.atomwide\\.com$");
        goodRegexp("^.+\\.856\\.eng\\.ukfederation\\.org\\.uk$");
        goodRegexp("^.+\\.scot\\.nhs\\.uk$");
        goodRegexp("^.+\\.login\\.groupcall\\.com$");
        goodRegexp("^.+\\.logintestingthirks\\.groupcall\\.com$");
        goodRegexp("^.+\\.logintest\\.me\\.e2bn\\.org$");
        goodRegexp("^.+\\.loginstaging\\.groupcall\\.com$");
        goodRegexp("^.+\\.identityfor\\.co\\.uk$");
        goodRegexp("^.+\\.rmunify\\.com$");

        // Combination regexp plus case significance
        goodRegexp("^.+\\.UGent\\.be$");
    }
}
