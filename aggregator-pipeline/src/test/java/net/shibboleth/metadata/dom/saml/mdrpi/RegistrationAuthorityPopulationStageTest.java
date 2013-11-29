
package net.shibboleth.metadata.dom.saml.mdrpi;

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.dom.saml.mdrpi.RegistrationAuthority;
import net.shibboleth.metadata.dom.saml.mdrpi.RegistrationAuthorityPopulationStage;
import net.shibboleth.utilities.java.support.collection.ClassToInstanceMultiMap;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class RegistrationAuthorityPopulationStageTest extends BaseDOMTest {

    @BeforeClass
    private void init() {
        setTestingClass(RegistrationAuthorityPopulationStage.class);
    }

    private RegistrationAuthorityPopulationStage makeStage() throws ComponentInitializationException {
        final RegistrationAuthorityPopulationStage stage = new RegistrationAuthorityPopulationStage();
        stage.setId("test");
        stage.initialize();
        return stage; 
    }
    
    private DOMElementItem makeItem(final String which) throws XMLParserException {
        final Element doc = readXMLData(classRelativeResource(which + ".xml"));
        return new DOMElementItem(doc);
    }
    
    @Test
    public void populatePresent() throws Exception {
        final DOMElementItem item = makeItem("present");
        
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final RegistrationAuthorityPopulationStage stage = makeStage();
        
        stage.execute(items);
        
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        final List<ErrorStatus> errors = metadata.get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 0);
        
        final List<RegistrationAuthority> regAuths = metadata.get(RegistrationAuthority.class);
        Assert.assertEquals(regAuths.size(), 1);
        final RegistrationAuthority regAuth = regAuths.get(0);
        Assert.assertEquals(regAuth.getRegistrationAuthority(), "http://ukfederation.org.uk");
    }
    
    @Test
    public void populateAbsent() throws Exception  {
        final DOMElementItem item = makeItem("absent");
        
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final RegistrationAuthorityPopulationStage stage = makeStage();
        
        stage.execute(items);
        
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        final List<ErrorStatus> errors = metadata.get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 0);
        
        final List<RegistrationAuthority> regAuths = metadata.get(RegistrationAuthority.class);
        Assert.assertEquals(regAuths.size(), 0);
    }
    
    @Test
    public void populateNoExtensions() throws Exception  {
        final DOMElementItem item = makeItem("noext");
        
        final List<Item<Element>> items = new ArrayList<>();
        items.add(item);
        
        final RegistrationAuthorityPopulationStage stage = makeStage();
        
        stage.execute(items);
        
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        final List<ErrorStatus> errors = metadata.get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 0);
        
        final List<RegistrationAuthority> regAuths = metadata.get(RegistrationAuthority.class);
        Assert.assertEquals(regAuths.size(), 0);
    }
    
}
