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

public class NamespaceStrippingStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public NamespaceStrippingStageTest() {
        super(NamespaceStrippingStage.class);
    }

    @Test
    public void doExecute() throws Exception {
        final Element doc = readXMLData("in.xml");
        final Item<Element> item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final NamespaceStrippingStage stage = new NamespaceStrippingStage();
        stage.setId("stripTest");
        stage.setNamespace("urn:namespace:beta");
        stage.initialize();
        
        stage.execute(items);
        
        final Element out = readXMLData("out.xml");
        assertXMLIdentical(out, item.unwrap());
    }
    
    @Test(expectedExceptions= {ComponentInitializationException.class})
    public void testNoNamespace() throws ComponentInitializationException {
        final var stage = new NamespaceStrippingStage();
        stage.setId("test");
        stage.initialize();
    }

    @Test
    public void testDestroy() throws Exception {
        final var stage = new NamespaceStrippingStage();
        stage.setId("test");
        stage.setNamespace("namespace");
        stage.initialize();
        stage.destroy();
    }
}
