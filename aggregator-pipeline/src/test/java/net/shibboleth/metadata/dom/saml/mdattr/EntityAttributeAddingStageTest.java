
package net.shibboleth.metadata.dom.saml.mdattr;

import java.util.ArrayList;
import java.util.List;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.dom.BaseDOMTest;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.pipeline.Pipeline;
import net.shibboleth.metadata.pipeline.SimplePipeline;
import net.shibboleth.metadata.pipeline.Stage;

import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class EntityAttributeAddingStageTest extends BaseDOMTest {

    protected EntityAttributeAddingStageTest() throws Exception {
        super(EntityAttributeAddingStage.class);
        setUp();
    }

    private List<Item<Element>> makeItems(final String inputFile) throws Exception {
        final Element startElement = readXMLData(inputFile);
        final List<Item<Element>> items = new ArrayList<>();
        items.add(new DOMElementItem(startElement));
        return items;
    }

    private Stage<Element> makeStage(final String value) throws Exception {
        final EntityAttributeAddingStage stage = new EntityAttributeAddingStage();
        stage.setId("test");
        stage.setAttributeValue(value);
        stage.initialize();
        return stage;
    }

    private Stage<Element> makeStage(final String value, final String name) throws Exception {
        final EntityAttributeAddingStage stage = new EntityAttributeAddingStage();
        stage.setId("test");
        stage.setAttributeValue(value);
        stage.setAttributeName(name);
        stage.initialize();
        return stage;
    }

    private Stage<Element> makeStage(final String value, final String name, final String nameFormat) throws Exception {
        final EntityAttributeAddingStage stage = new EntityAttributeAddingStage();
        stage.setId("test");
        stage.setAttributeValue(value);
        stage.setAttributeName(name);
        stage.setAttributeNameFormat(nameFormat);
        stage.initialize();
        return stage;
    }

    private Pipeline<Element> makePipeline(final List<Stage<Element>> stages) throws Exception {
        final SimplePipeline<Element> pipeline = new SimplePipeline<>();
        pipeline.setId("test");
        pipeline.setStages(stages);
        pipeline.initialize();
        return pipeline;
    }

    /*
     * Checks creation of Extensions if none is there already.
     */
    @Test
    public void addNoExtensions() throws Exception {
        final List<Item<Element>> itemCollection = makeItems("noExtensions.xml");
        final List<Stage<Element>> stages = new ArrayList<>();
        stages.add(makeStage("http://www.geant.net/uri/dataprotection-code-of-conduct/v1"));
        stages.add(makeStage("another"));
        // add the same attributes twice, just to make sure duplicates are ignored
        stages.add(makeStage("http://www.geant.net/uri/dataprotection-code-of-conduct/v1"));
        stages.add(makeStage("another"));
        final Pipeline<Element> pipeline = makePipeline(stages);
        pipeline.execute(itemCollection);
        final Element result = itemCollection.get(0).unwrap();
        final Element expected = readXMLData("added1.xml");
        assertXMLEqual(expected, result);
    }

    /*
     * Input has Extensions but no EntityAttributes.
     */
    @Test
    public void addToExtensions() throws Exception {
        final List<Item<Element>> itemCollection = makeItems("extensions.xml");
        final List<Stage<Element>> stages = new ArrayList<>();
        stages.add(makeStage("http://www.geant.net/uri/dataprotection-code-of-conduct/v1"));
        stages.add(makeStage("http://example.org/category2"));
        stages.add(makeStage("http://example.org/category2support", "http://macedir.org/entity-category-support"));
        stages.add(makeStage("http://www.geant.net/uri/dataprotection-code-of-conduct/v1", "http://macedir.org/entity-category-support"));
        stages.add(makeStage("anotherValue", "anotherAttributeName", "anotherNameFormat"));
        final Pipeline<Element> pipeline = makePipeline(stages);
        pipeline.execute(itemCollection);
        final Element result = itemCollection.get(0).unwrap();
        final Element expected = readXMLData("added2.xml");
        assertXMLEqual(expected, result);
    }

    /*
     * Input has Extensions but no EntityAttributes; adding at the front.
     */
    @Test
    public void addToExtensionsFirst() throws Exception {
        final List<Item<Element>> itemCollection = makeItems("extensions.xml");
        final List<Stage<Element>> stages = new ArrayList<>();
        final EntityAttributeAddingStage stage = new EntityAttributeAddingStage();
        stage.setId("test");
        stage.setAttributeValue("http://www.geant.net/uri/dataprotection-code-of-conduct/v1");
        stage.setAddingFirstChild(true);
        stage.initialize();
        stages.add(stage);
        stages.add(makeStage("http://example.org/category2"));
        stages.add(makeStage("http://example.org/category2support", "http://macedir.org/entity-category-support"));
        stages.add(makeStage("http://www.geant.net/uri/dataprotection-code-of-conduct/v1", "http://macedir.org/entity-category-support"));
        stages.add(makeStage("anotherValue", "anotherAttributeName", "anotherNameFormat"));
        final Pipeline<Element> pipeline = makePipeline(stages);
        pipeline.execute(itemCollection);
        final Element result = itemCollection.get(0).unwrap();
        final Element expected = readXMLData("added3.xml");
        assertXMLEqual(expected, result);
    }

    /*
     * Check that adding something has no effect if it's already there.
     */
    @Test
    public void addDuplicates() throws Exception {
        final List<Item<Element>> itemCollection = makeItems("added2.xml");
        final List<Stage<Element>> stages = new ArrayList<>();
        stages.add(makeStage("http://www.geant.net/uri/dataprotection-code-of-conduct/v1"));
        stages.add(makeStage("http://example.org/category2"));
        stages.add(makeStage("http://example.org/category2support", "http://macedir.org/entity-category-support"));
        stages.add(makeStage("http://www.geant.net/uri/dataprotection-code-of-conduct/v1", "http://macedir.org/entity-category-support"));
        stages.add(makeStage("anotherValue", "anotherAttributeName", "anotherNameFormat"));
        final Pipeline<Element> pipeline = makePipeline(stages);
        pipeline.execute(itemCollection);
        final Element result = itemCollection.get(0).unwrap();
        final Element expected = readXMLData("added2.xml");
        assertXMLEqual(expected, result);
    }

    /*
     * Add some missing values to an existing EntityAttributes collection.
     */
    @Test
    public void addToExisting() throws Exception {
        final List<Item<Element>> itemCollection = makeItems("some.xml");
        final List<Stage<Element>> stages = new ArrayList<>();
        stages.add(makeStage("http://www.geant.net/uri/dataprotection-code-of-conduct/v1"));
        stages.add(makeStage("http://example.org/category2"));
        stages.add(makeStage("http://example.org/category2support", "http://macedir.org/entity-category-support"));
        stages.add(makeStage("http://www.geant.net/uri/dataprotection-code-of-conduct/v1", "http://macedir.org/entity-category-support"));
        stages.add(makeStage("anotherValue", "anotherAttributeName", "anotherNameFormat"));
        final Pipeline<Element> pipeline = makePipeline(stages);
        pipeline.execute(itemCollection);
        final Element result = itemCollection.get(0).unwrap();
        final Element expected = readXMLData("added2.xml");
        assertXMLEqual(expected, result);
    }
}
