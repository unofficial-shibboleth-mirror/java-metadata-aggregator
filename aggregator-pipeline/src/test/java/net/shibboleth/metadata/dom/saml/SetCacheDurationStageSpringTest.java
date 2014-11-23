package net.shibboleth.metadata.dom.saml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration({"SetCacheDurationStageSpringTest-config.xml"})
public class SetCacheDurationStageSpringTest extends AbstractTestNGSpringContextTests {
    
    @Autowired
    SetCacheDurationStage stage;
    
    @Test
    public void testDuration() throws Exception {
        // one day, 17 hours, 34 minutes and 19 seconds = P1DT17H34M19S
        Assert.assertEquals(stage.getCacheDuration(), 1000L * (86400 + 17*3600 + 34*60 + 19));
    }

}
