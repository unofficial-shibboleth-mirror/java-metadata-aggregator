package net.shibboleth.metadata.dom.saml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration({"SetValidUntilStageSpringTest-config.xml"})
public class SetValidUntilStageSpringTest extends AbstractTestNGSpringContextTests {
    
    @Autowired
    SetValidUntilStage stage;
    
    @Test
    public void testDuration() throws Exception {
        // one day, 17 hours, 34 minutes and 19 seconds = P1DT17H34M19S
        Assert.assertEquals(stage.getValidityDuration(), 1000L * (86400 + 17*3600 + 34*60 + 19));
    }

}
