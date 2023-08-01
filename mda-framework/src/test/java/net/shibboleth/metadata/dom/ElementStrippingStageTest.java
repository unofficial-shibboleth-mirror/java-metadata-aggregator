/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;
import net.shibboleth.shared.component.ComponentInitializationException;

import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class ElementStrippingStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public ElementStrippingStageTest() {
        super(ElementStrippingStage.class);
    }

    @Test
    public void doExecute() throws Exception {
        final Element doc = readXMLData("in.xml");
        final Item<Element> item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final ElementStrippingStage stage = new ElementStrippingStage();
        stage.setId("stripTest");
        stage.setElementNamespace("urn:namespace:beta");
        stage.setElementName("StripMe");
        stage.initialize();
        
        stage.execute(items);
        
        final Element out = readXMLData("out.xml");
        assertXMLIdentical(out, item.unwrap());
    }
    
    @Test(expectedExceptions = {ComponentInitializationException.class})
    public void testNoElementName() throws ComponentInitializationException {
        final var stage = new ElementStrippingStage();
        stage.setId("test");
        stage.setElementNamespace("foo");
        stage.initialize();
    }

    @Test(expectedExceptions = {ComponentInitializationException.class})
    public void testNoElementNamespace() throws ComponentInitializationException {
        final var stage = new ElementStrippingStage();
        stage.setId("test");
        stage.setElementName("bar");
        stage.initialize();
    }

    @Test
    public void testDestroy() throws ComponentInitializationException {
        final var stage = new ElementStrippingStage();
        stage.setId("test");
        stage.setElementNamespace("foo");
        stage.setElementName("bar");
        stage.initialize();
        stage.destroy();
    }

}
