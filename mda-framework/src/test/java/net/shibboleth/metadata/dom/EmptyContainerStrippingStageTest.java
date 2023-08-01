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

import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class EmptyContainerStrippingStageTest extends BaseDOMTest {

    /** Constructor sets class under test. */
    public EmptyContainerStrippingStageTest() {
        super(EmptyContainerStrippingStage.class);
    }

    @Test
    public void doExecute() throws Exception {
        final Element doc = readXMLData("in.xml");
        final DOMElementItem item = new DOMElementItem(doc);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final NamespaceStrippingStage removeMdui = new NamespaceStrippingStage();
        removeMdui.setId("removeMdui");
        removeMdui.setNamespace("urn:oasis:names:tc:SAML:metadata:ui");
        removeMdui.initialize();
        removeMdui.execute(items);
        
        final NamespaceStrippingStage removeUk = new NamespaceStrippingStage();
        removeUk.setId("removeUk");
        removeUk.setNamespace("http://ukfederation.org.uk/2006/11/label");
        removeUk.initialize();
        removeUk.execute(items);
        
        final EmptyContainerStrippingStage stage = new EmptyContainerStrippingStage();
        stage.setId("emptyExtensionsTest");
        stage.setElementName("Extensions");
        stage.setElementNamespace("urn:oasis:names:tc:SAML:2.0:metadata");
        stage.initialize();
        stage.execute(items);
        stage.destroy();
        
        final Element out = readXMLData("out.xml");
        assertXMLIdentical(out, item.unwrap());
    }
}
