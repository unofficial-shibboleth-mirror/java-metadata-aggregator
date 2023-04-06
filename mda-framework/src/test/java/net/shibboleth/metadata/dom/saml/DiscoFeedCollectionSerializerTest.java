
package net.shibboleth.metadata.dom.saml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemCollectionSerializer;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.xml.XMLParserException;

public class DiscoFeedCollectionSerializerTest extends BaseDOMTest {

    protected DiscoFeedCollectionSerializerTest() {
        super(DiscoFeedCollectionSerializer.class);
    }

    @Test
    public void testEmptyCollection() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.initialize();
        final String output;
        try (final var out = new ByteArrayOutputStream()) {
            ser.serializeCollection(CollectionSupport.emptyList(), out);
            output = out.toString();
        }
        final var read = Json.createReader(new StringReader(output));
        final var array = read.readArray();
        Assert.assertEquals(array.size(), 0);
    }

    private @Nonnull JsonArray fetchJSONArray(@Nonnull final String path) throws IOException {
        try (var in = BaseDOMTest.class.getResourceAsStream(classRelativeResource(path))) {
            try (var reader = Json.createReader(in)) {
                var array = reader.readArray();
                assert array != null;
                return array;
            }
        }
    }

    private void checkEntity(@Nullable final JsonObject entity) {
        assert entity != null;
        Assert.assertEquals(entity.getString("entityID"), "https://idp.example.com/idp/shibboleth");
    }

    private void checkEntityArray(@Nonnull final JsonArray array) {
        Assert.assertEquals(array.size(), 1);
        checkEntity((JsonObject)array.get(0));
    }

    @Test
    public void testPreCooked() throws Exception {
        var entity = fetchJSONArray("base.json");
        checkEntityArray(entity);
    }

    private void compareCollections(@Nonnull final JsonArray actual, @Nonnull final JsonArray expected) {
        // The two collections must match as to size
        if (actual.size() != expected.size()) {
            Assert.assertEquals(actual.size(), expected.size(), "array sizes differ");
        }
        // The two collections must match entity by entity
        for (int i = 0; i < actual.size(); i++) {
            final var actualEntity = (JsonObject)actual.get(i);
            final var expectedEntity = (JsonObject)expected.get(i);

            // Get an identifier for the entity
            final String id = "entity " + i + " ('" + expectedEntity.getString("entityID") + "')";

            // Compare the key sets
            final var actualKeys = actualEntity.keySet();
            final var expectedKeys = expectedEntity.keySet();
            Assert.assertEquals(actualKeys, expectedKeys, "key sets differ for " + id);

            // Key sets are the same... compare each key value
            for (final var key : actualKeys) {
                final var actualValue = actualEntity.get(key);
                final var expectedValue = expectedEntity.get(key);
                Assert.assertEquals(actualValue, expectedValue, "key " + key + " differs for " + id);
            }

            // Backstop
            Assert.assertEquals(actualEntity, expectedEntity, id + " differs:");
        }
    }

    /**
     * Checks the serialization of a single entity using a given serializer
     * configuration.
     * 
     * @param name base name for the resources
     * @param ser configured and initialized serializer to use
     * 
     * @return the single {@link JsonObject} for further tests
     * 
     * @throws IOException if there are parsing issues with the JSON file
     * @throws XMLParserException if there are parsing issues with the XML file
     */
    private JsonObject checkSingle(@Nonnull final String name, @Nonnull final ItemCollectionSerializer<Element> ser)
            throws IOException, XMLParserException {
        final String output;
        try (final var out = new ByteArrayOutputStream()) {
            ser.serializeCollection(CollectionSupport.listOf(readDOMItem(name + ".xml")), out);
            output = out.toString();
        }
        //System.out.println(output);
        final var read = Json.createReader(new StringReader(output));
        final var array = read.readArray();
        Assert.assertEquals(array.size(), 1);
        
        final var entity = (JsonObject)array.get(0);
        checkEntity(entity);
        final var expected = fetchJSONArray(name + ".json");
        compareCollections(array, expected);
        Assert.assertEquals(array, expected);
        
        return entity;
    }

    // Test the base case, derived from a single standard IdP
    // with default generator settings.
    @Test
    public void testBase() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.initialize();
        final var entity = checkSingle("base", ser);
        Assert.assertTrue(entity.containsKey("Descriptions"));
        Assert.assertTrue(entity.containsKey("DisplayNames"));
        Assert.assertFalse(entity.containsKey("InformationURLs"));
        Assert.assertFalse(entity.containsKey("PrivacyStatementURLs"));
    }

    // Test the absence of an mdui:DisplayName
    // with default generator settings.
    @Test
    public void testNoLegacy() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.initialize();
        final var entity = checkSingle("nolegacy", ser);
        Assert.assertFalse(entity.containsKey("Descriptions"));
        Assert.assertFalse(entity.containsKey("DisplayNames"));
    }

    // Test the inclusion of a legacy DisplayName when the
    // serializer is set to allow this.
    @Test
    public void testLegacy() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.setIncludingLegacyDisplayNames(true);
        ser.setPrettyPrinting(true);
        ser.initialize();
        final var entity = checkSingle("legacy", ser);
        Assert.assertFalse(entity.containsKey("Descriptions"));
        Assert.assertTrue(entity.containsKey("DisplayNames"));
    }

    // Test an entity with a couple of InformationURLs.
    @Test
    public void testInformationURLs() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.initialize();
        final var entity = checkSingle("infourl", ser);
        Assert.assertTrue(entity.containsKey("Descriptions"));
        Assert.assertTrue(entity.containsKey("DisplayNames"));
        Assert.assertTrue(entity.containsKey("InformationURLs"));
    }

    // Test an entity with a couple of PrivacyStatementURLs.
    @Test
    public void testPrivacyStatementURLs() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.initialize();
        final var entity = checkSingle("privacyurl", ser);
        Assert.assertTrue(entity.containsKey("Descriptions"));
        Assert.assertTrue(entity.containsKey("DisplayNames"));
        Assert.assertTrue(entity.containsKey("PrivacyStatementURLs"));
    }

    // Test the optional nature of xml:lang on mdui:Logo elements.
    @Test
    public void testLogoLang() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.initialize();
        final var entity = checkSingle("logolang", ser);
        final var logos = entity.getJsonArray("Logos");
        Assert.assertEquals(logos.size(), 3);
        Assert.assertFalse(logos.getJsonObject(0).containsKey("lang"));
        Assert.assertTrue(logos.getJsonObject(1).containsKey("lang"));
        Assert.assertEquals(logos.getJsonObject(1).getString("lang"), "en");
        Assert.assertFalse(logos.getJsonObject(2).containsKey("lang"));
    }
    
    // Test entity attributes present (base tests absent)
    @Test
    public void testEntityAttributes() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.setIncludingEntityAttributes(true);
        ser.initialize();
        final var entity = checkSingle("entattr", ser);
        final var attrs = entity.getJsonArray("EntityAttributes");
        Assert.assertEquals(attrs.size(), 3);
        Assert.assertEquals(attrs.getJsonObject(2).getString("name"), "something");
        Assert.assertEquals(attrs.getJsonObject(2).getJsonArray("values").size(), 1);
        Assert.assertEquals(attrs.getJsonObject(2).getJsonArray("values").getString(0), "whatever");
    }

    // Test a case where we include only an IdP entity
    @Test
    public void testOnlyIdP() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.initialize();
        final String output;
        try (final var out = new ByteArrayOutputStream()) {
            ser.serializeCollection(CollectionSupport.listOf(readDOMItem("noidp.xml")), out);
            output = out.toString();
        }
        Assert.assertEquals(output, "[]");
    }

    // Test output for a non-entity
    @Test
    public void testNonEntity() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.initialize();
        final String output;
        try (final var out = new ByteArrayOutputStream()) {
            ser.serializeCollection(CollectionSupport.listOf(readDOMItem("nonentity.xml")), out);
            output = out.toString();
        }
        Assert.assertEquals(output, "[]");
    }

    // Test an entity with no UIInfo container
    @Test
    public void testNoUIInfo() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.initialize();
        final var entity = checkSingle("no-uiinfo", ser);
        Assert.assertEquals(entity.keySet().size(), 1); // just the entityID
        Assert.assertFalse(entity.containsKey("DisplayNames"));
    }

    // Test an entity with no UIInfo container, with legacy display names
    @Test
    public void testNoUIInfoLegacy() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.setIncludingLegacyDisplayNames(true);
        ser.initialize();
        final var entity = checkSingle("no-uiinfo-leg", ser);
        Assert.assertTrue(entity.containsKey("DisplayNames"));
    }

    // Test a large aggregate. This test is normally disabled, but it
    // can be brought back to life if necessary to test a new corpus.
    @Test(enabled=false)
    public void testAll() throws Exception {
        final var ser = new DiscoFeedCollectionSerializer();
        ser.initialize();

        // Build a collection to serialize.
        final var item = readDOMItem("all.xml");
        final var items = new ArrayList<Item<Element>>();
        items.add(item);
        
        // Disaggregate the collection into individual entities
        final var disassembler = new EntitiesDescriptorDisassemblerStage();
        disassembler.setId("disassemble");
        disassembler.initialize();
        disassembler.execute(items);
        
        // Now generate the disco feed
        final String output;
        try (final var out = new ByteArrayOutputStream()) {
            ser.serializeCollection(items, out);
            output = out.toString();
        }
        final var read = Json.createReader(new StringReader(output));
        final var array = read.readArray();
        assert array != null;

        final var expected = fetchJSONArray("all.json");
        compareCollections(array, expected);
        Assert.assertEquals(array, expected);
    }
}
