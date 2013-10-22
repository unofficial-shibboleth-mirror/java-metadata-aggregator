
package net.shibboleth.metadata.dom.saml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GenerateIdStageTest {

    /**
     * Test for MDA-90 (default identifier strategy used by GenerateIdStage
     * sometimes generates bad IDs).
     */
    @Test
    public void mda90() throws Exception {
        final GenerateIdStage stage = new GenerateIdStage();
        stage.setId("test");
        stage.initialize();
        
        final BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();
        final Document doc = parserPool.newDocument();
        final QName qname = SAMLMetadataSupport.ENTITY_DESCRIPTOR_NAME;
        final Element element = ElementSupport.constructElement(doc, qname);
        Assert.assertEquals(element.getLocalName(), qname.getLocalPart());
        doc.appendChild(element);
        
        final DOMElementItem item = new DOMElementItem(doc);
        final List<Item<Element>> metadataCollection = new ArrayList<>();
        metadataCollection.add(item);

        final Pattern ncNamePattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_\\-\\.]+$");
        final int howMany = 1000;
        final Set<String> values = new HashSet<>(howMany);
        for (int iteration = 1; iteration<=howMany; iteration++) {
            stage.execute(metadataCollection);
            final Element el = item.unwrap();
            Assert.assertEquals(el.getLocalName(), qname.getLocalPart());
            Assert.assertTrue(el.hasAttribute("ID"));
            final String value = el.getAttribute("ID");

            // we should get a value each time
            Assert.assertNotNull(value);
            
            // we shouldn't see the same value twice
            if (values.contains(value)) {
                Assert.fail("duplicate value '" + value + "' on iteration " + iteration);
            }
            values.add(value);
            
            // values should be valid NCNames
            final Matcher match = ncNamePattern.matcher(value);
            if (!match.matches()) {
                Assert.fail("value '" + value + "' is not a valid NCName on iteration " + iteration);
            }
        }
        
        Assert.assertEquals(values.size(), howMany);
    }

}
