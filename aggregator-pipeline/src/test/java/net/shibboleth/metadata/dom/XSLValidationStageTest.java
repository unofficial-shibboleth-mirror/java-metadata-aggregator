package net.shibboleth.metadata.dom;

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.AssertSupport;
import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.WarningStatus;

import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.Resource;
import org.opensaml.util.xml.XMLParserException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class XSLValidationStageTest extends BaseDomTest {

    /** Simple marker object to test correct passage of {@link ItemMetadata} through pipeline stages. */
    private static class TestInfo implements ItemMetadata {
        /** All {@link MetdataInfo} subclasses must declare version UIDs. */
        private static final long serialVersionUID = -4133926323393787487L;
    }

    /**
     * Utility method to grab our standard input file and turn it into a {@link DomElementItem}.
     * 
     * @throws XMLParserException
     */
    private DomElementItem makeInput() throws XMLParserException  {
        Element testInput = readXmlData("xsltStageInput.xml");
        DomElementItem metadata = new DomElementItem(testInput);
        // add a TestInfo so that we can check it is preserved by the stage.
        Assert.assertEquals(metadata.getItemMetadata().get(TestInfo.class).size(), 0);
        metadata.getItemMetadata().put(new TestInfo());
        Assert.assertEquals(metadata.getItemMetadata().get(TestInfo.class).size(), 1);
        return metadata;
    }

    /**
     * Test a validation transform.
     */
    @Test
    public void testValidation() throws Exception {
        
        ArrayList<DomElementItem> mdCol = new ArrayList<DomElementItem>();
        mdCol.add(makeInput());

        Resource transform = new ClasspathResource("data/xslValidator.xsl");

        XSLValidationStage stage = new XSLValidationStage();
        stage.setId("test");
        stage.setXslResource(transform);
        stage.initialize();

        stage.execute(mdCol);
        
        // The input element should still be the only thing in the collection
        Assert.assertEquals(mdCol.size(), 1);
        DomElementItem result = mdCol.get(0);

        // The XML should be unchanged
        Element expected = readXmlData("xsltStageInput.xml");
        assertXmlEqual(expected, result.unwrap());

        // It should have been processed by the appropriate stage
        AssertSupport.assertValidComponentInfo(result, 1, XSLValidationStage.class, "test");
            
        // result item should have preserved the TestInfo that was on the input
        Assert.assertEquals(result.getItemMetadata().get(TestInfo.class).size(), 1);
            
        // verify the presence of the InfoStatus on the output
        List<InfoStatus> infos = result.getItemMetadata().get(InfoStatus.class);
        Assert.assertEquals(infos.size(), 2);
        Assert.assertEquals(infos.get(0).getStatusMessage(), "second value");
        Assert.assertEquals(infos.get(1).getStatusMessage(), "second value second message");

        // verify the presence of the WarningStatus on the output
        List<WarningStatus> warnings = result.getItemMetadata().get(WarningStatus.class);
        Assert.assertEquals(warnings.size(), 1);
        Assert.assertEquals(warnings.get(0).getStatusMessage(), "first value");

        // verify the presence of the WarningStatus on the output
        List<ErrorStatus> errors = result.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 1);
        Assert.assertEquals(errors.get(0).getStatusMessage(), "error value");
    }
    
}
