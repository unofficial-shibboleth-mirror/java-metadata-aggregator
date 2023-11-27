package net.shibboleth.metadata.validate.string;

import java.net.URL;

import org.testng.annotations.Test;

public class AsURLStringValidatorTest {

    //@Test
    public void f() throws Exception {
        var v = new URL("http://wibble wobble");
        System.out.println(v);
        var v2 = new URL("data:asdadasd");
        System.out.println(v2);
    }

}
