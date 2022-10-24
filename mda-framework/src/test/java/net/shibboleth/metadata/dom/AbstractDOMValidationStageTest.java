/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package net.shibboleth.metadata.dom;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

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

    private static class StringValidationStage extends AbstractDOMValidationStage<String, DOMTraversalContext> {

        @Override
        protected boolean applicable(final @Nonnull Element element, final @Nonnull DOMTraversalContext context) {
            return "checkedElement".equals(element.getLocalName());
        }

        @SuppressWarnings("null")
        @Override
        protected void visit(final @Nonnull Element element, final @Nonnull DOMTraversalContext context)
                throws StageProcessingException {
            applyValidators(element.getTextContent(), context);
        }

        @Override
        protected @Nonnull DOMTraversalContext buildContext(final @Nonnull Item<Element> item) {
            return new SimpleDOMTraversalContext(item);
        }

    }

    private static class FirstStringValidator extends BaseValidator implements Validator<String> {

        @Override
        public @Nonnull Action validate(final @Nonnull String e, final @Nonnull Item<?> item, final @Nonnull String stageId)
                throws StageProcessingException {
            addError("element contains " + e, item, stageId);
            return Action.DONE; // once per value checked
        }
        
    }

    private static class UnexecutedStringValidator extends BaseValidator implements Validator<String> {

        @Override
        public @Nonnull Action validate(final @Nonnull String e, final @Nonnull Item<?> item,
                final @Nonnull String stageId)
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

    @Test
    public void noValidatorsByDefault() throws Exception {
        var stage = new StringValidationStage();
        Assert.assertEquals(stage.getValidators().size(), 0);
    }
}
