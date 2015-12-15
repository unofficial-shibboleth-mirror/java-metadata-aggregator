
package net.shibboleth.metadata.dom;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.validate.BaseValidator;
import net.shibboleth.metadata.validate.Validator;

public class AbstractDOMValidationStageTest extends BaseDOMTest {

    protected AbstractDOMValidationStageTest() {
        super(AbstractDOMValidationStage.class);
    }

    private static class StringValidationStage extends AbstractDOMValidationStage<String> {

        @Override
        protected boolean applicable(Element element) {
            return "checkedElement".equals(element.getLocalName());
        }

        @Override
        protected void visit(Element element, TraversalContext context) throws StageProcessingException {
            applyValidators(element.getTextContent(), context);
        }
        
    }

    private static class FirstStringValidator extends BaseValidator implements Validator<String> {

        @Override
        public Action validate(String e, Item<?> item, String stageId)
                throws StageProcessingException {
            addError("element contains " + e, item, stageId);
            return Action.DONE; // once per value checked
        }
        
    }

    private static class UnexecutedStringValidator extends BaseValidator implements Validator<String> {

        @Override
        public Action validate(String e, Item<?> item, String stageId)
                throws StageProcessingException {
            Assert.fail("should not execute this");
            return Action.CONTINUE;
        }

    }

    @Test
    public void testEarlyCompletion() throws Exception {
        final FirstStringValidator v1 = new FirstStringValidator();
        v1.setId("v1");
        v1.initialize();
        final UnexecutedStringValidator v2 = new UnexecutedStringValidator();
        v2.setId("v2");
        v2.initialize();
        final List<Validator<String>> validators = new ArrayList<>();
        validators.add(v1);
        validators.add(v2);
        
        final StringValidationStage s1 = new StringValidationStage();
        s1.setId("s1");
        s1.setValidators(validators);
        s1.initialize();

        final Item<Element> i1 = readDOMItem("early.xml");
        final List<Item<Element>> items = new ArrayList<>();
        items.add(i1);
        
        s1.execute(items);
        Assert.assertEquals(2, countErrors(i1));
        final List<ErrorStatus> errors = i1.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.get(0).getStatusMessage(), "element contains one");
        Assert.assertEquals(errors.get(1).getStatusMessage(), "element contains two");
    }

}
