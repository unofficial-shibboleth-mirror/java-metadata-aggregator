package net.shibboleth.metadata.validate.string;

import java.net.URL;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.metadata.validate.Validator.Action;
import net.shibboleth.metadata.validate.testing.CollectingValidator;
import net.shibboleth.shared.collection.CollectionSupport;

public class AsURLStringValidatorTest {

    /**
     * Generic test for a good URL.
     *
     * @throws Exception if something goes wrong
     */
    private @Nonnull URL good(@Nonnull final String value) throws Exception {
        final var cv = CollectingValidator.<URL>getInstance("collect");
        final var item = new MockItem("item");
        final var v = new AsURLStringValidator();
        v.setId("test");
        v.setValidators(CollectionSupport.singletonList(cv));
        v.initialize();
        var result = v.validate(value, item, "stage");
        Assert.assertEquals(result, Action.CONTINUE);
        Assert.assertTrue(item.getItemMetadata().isEmpty());
        var collected = cv.getValues();
        Assert.assertEquals(collected.size(), 1);
        var url = collected.get(0);
        cv.destroy();
        v.destroy();
        assert url != null;
        return url;
    }

    @Test
    public void testSuccess() throws Exception {
        final var url = good("HTTPS://example.org:1234/example");
        // Confirm lower-casing of protocol field.
        Assert.assertEquals(url.getProtocol(), "https");
    }

    /**
     * Generic test for a bad URL.
     * 
     * @param bad   bad URL to test
     */
    private @Nonnull ErrorStatus badURL(@Nonnull String value) throws Exception {
        final var item = new MockItem("item");
        final var v = new AsURLStringValidator();
        v.setId("test");
        v.initialize();
        var result = v.validate(value, item, "stage");
        Assert.assertEquals(result, Action.DONE);
        final var errors = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 1);
        final var error = errors.get(0);
        assert error != null;
        v.destroy();
        return error;
    }

    @Test
    public void testBadPort() throws Exception {
        String bad = "https://example.org:port/example";
        badURL(bad);
    }

    
    /**
     * Test the case where the authority's port field is present but empty.
     * 
     * This is valid by the specification, but is regarded as invalid by
     * libxml2's xs:anyURI checker.
     */
//    @Test
//    public void testEmptyPort() throws Exception {
//        badURL("http://example.org:/example/");
//    }
    
//    @Test
//    public void testDoubleScheme() throws Exception {
//        badURL("http://http://example.org/example/");
//    }
    
    @Test
    public void testBareDomain() throws Exception {
        badURL("www.example.org");
    }
    
    @Test
    public void testEmptyUrl() throws Exception {
        badURL("");
    }

//    @Test
//    public void testEmptyAuthority() throws Exception {
//        badURL("http:///foo/");
//    }
    
    @Test
    public void testFillInHostName() throws Exception {
        badURL("http://*** FILL IN ***/");
    }

}
