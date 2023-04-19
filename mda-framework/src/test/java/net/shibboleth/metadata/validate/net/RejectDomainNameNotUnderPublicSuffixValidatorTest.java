package net.shibboleth.metadata.validate.net;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.net.InternetDomainName;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.metadata.validate.Validator.Action;

public class RejectDomainNameNotUnderPublicSuffixValidatorTest {

    @Test
    public void normal() throws Exception {
        final Item<String> item = new MockItem("content");
        final RejectDomainNameNotUnderPublicSuffixValidator val =
                new RejectDomainNameNotUnderPublicSuffixValidator();
        val.setId("validate");
        val.initialize();

        final InternetDomainName domain = InternetDomainName.from("example.org");
        assert domain != null;
        final Action res = val.validate(domain, item, "stage");
        Assert.assertNotNull(res);
        Assert.assertEquals(res, Action.CONTINUE);
        Assert.assertEquals(item.getItemMetadata().get(ErrorStatus.class).size(), 0);

        final var domain2 = InternetDomainName.from("ed.ac.uk");
        assert domain2 != null;
        Assert.assertEquals(val.validate(domain2, item, "stage"), Action.CONTINUE);
        Assert.assertEquals(item.getItemMetadata().get(ErrorStatus.class).size(), 0);
    }

    @Test
    public void uk() throws Exception {
        final Item<String> item = new MockItem("content");
        final RejectDomainNameNotUnderPublicSuffixValidator val =
                new RejectDomainNameNotUnderPublicSuffixValidator();
        val.setId("validate");
        val.initialize();

        final InternetDomainName domain = InternetDomainName.from("uk");
        assert domain != null;
        final Action res = val.validate(domain, item, "stage");
        Assert.assertNotNull(res);
        Assert.assertEquals(res, Action.DONE);
        Assert.assertEquals(item.getItemMetadata().get(ErrorStatus.class).size(), 1);
        Assert.assertTrue(item.getItemMetadata().get(ErrorStatus.class).get(0).getStatusMessage().contains("rejected"));
    }

    @Test
    public void ac_uk() throws Exception {
        final Item<String> item = new MockItem("content");
        final RejectDomainNameNotUnderPublicSuffixValidator val =
                new RejectDomainNameNotUnderPublicSuffixValidator();
        val.setId("validate");
        val.initialize();

        final InternetDomainName domain = InternetDomainName.from("ac.uk");
        assert domain != null;
        final Action res = val.validate(domain, item, "stage");
        Assert.assertNotNull(res);
        Assert.assertEquals(res, Action.DONE);
        Assert.assertEquals(item.getItemMetadata().get(ErrorStatus.class).size(), 1);
        Assert.assertTrue(item.getItemMetadata().get(ErrorStatus.class).get(0).getStatusMessage().contains("rejected"));
    }

    @Test
    public void wibble_wobble() throws Exception {
        final Item<String> item = new MockItem("content");
        final RejectDomainNameNotUnderPublicSuffixValidator val =
                new RejectDomainNameNotUnderPublicSuffixValidator();
        val.setId("validate");
        val.setMessage("scope is not under a public suffix: '%s'");
        val.initialize();

        // This is (currently) just a nonsense value, so it doesn't have a public suffix
        // and isn't under one either.
        final InternetDomainName domain = InternetDomainName.from("wibble.wobble");
        assert domain != null;
        final Action res = val.validate(domain, item, "stage");
        Assert.assertNotNull(res);
        Assert.assertEquals(res, Action.DONE);
        Assert.assertEquals(item.getItemMetadata().get(ErrorStatus.class).size(), 1);
        Assert.assertEquals(item.getItemMetadata().get(ErrorStatus.class).get(0).getStatusMessage(),
                "scope is not under a public suffix: 'wibble.wobble'");
    }

}
