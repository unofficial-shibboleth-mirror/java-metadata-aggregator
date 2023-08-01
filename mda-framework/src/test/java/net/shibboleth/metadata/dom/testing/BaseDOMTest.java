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

package net.shibboleth.metadata.dom.testing;

import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.transform.Source;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.input.NormalizedSource;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemMetadata;
import net.shibboleth.metadata.dom.DOMElementItem;
import net.shibboleth.metadata.testing.BaseTest;
import net.shibboleth.shared.collection.ClassToInstanceMultiMap;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.primitive.StringSupport;
import net.shibboleth.shared.xml.impl.BasicParserPool;
import net.shibboleth.shared.xml.ParserPool;
import net.shibboleth.shared.xml.SerializeSupport;
import net.shibboleth.shared.xml.XMLParserException;

/** A base class for DOM related tests. */
public abstract class BaseDOMTest extends BaseTest {

    /**
     * Initialized parser pool used to parse data.
     *
     * Parsers produced by this pool are set up slightly differently
     * than the default Shibboleth {@link BasicParserPool}.
     * In particular, they do <i>not</i> ignore either comment
     * nodes or "ignoreable" whitespace in element content.
     *
     * This means that tests have access to <i>all</i> of the
     * contents of test resources. It is the responsibility of
     * each test to perform appropriate XMLUnit source wrapping
     * or comparisons if that is appropriate in any given case.
     */
    private BasicParserPool parserPool;

    /**
     * Constructor
     * 
     * @param clazz class under test
     */
    protected BaseDOMTest(final @Nonnull Class<?> clazz) {
        super(clazz);
    }

    /**
     * Setup test class. Creates and initializes the parser pool.
     * 
     * @throws ComponentInitializationException if there is a problem initializing the parser pool
     */
    @BeforeClass
    public void setUp() throws ComponentInitializationException {
        // Use defaults of BasicParserPool as a basis.
        parserPool = new BasicParserPool();

        // Override defaults to present ALL of the XML in the resource.
        parserPool.setIgnoreComments(false);
        parserPool.setIgnoreElementContentWhitespace(false);

        parserPool.initialize();
    }

    /**
     * Gets an initialized parser pool.
     * 
     * @return initialized parser pool, never null
     */
    public ParserPool getParserPool() {
        return parserPool;
    }

    /**
     * Reads in an XML file, parses it, and returns the document element. If the given path is relative (i.e., does not
     * start with a '/') it is assumed to be relative to the class, or to /data if the class has not been set.
     * 
     * @param path classpath path to the data file, never null
     * 
     * @return the document root of the data file, never null
     * 
     * @throws XMLParserException thrown if the file does not exist or there is a problem parsing it
     */
    public @Nonnull Element readXMLData(final @Nonnull String path) throws XMLParserException {
        @Nonnull String trimmedPath = Constraint.isNotNull(StringSupport.trimOrNull(path), "Path may not be null or empty");        

        if (!trimmedPath.startsWith("/")) {
            trimmedPath = classRelativeResource(trimmedPath);
        }

        final InputStream input = BaseDOMTest.class.getResourceAsStream(trimmedPath);
        if (input == null) {
            throw new XMLParserException(trimmedPath + " does not exist or is not readable");
        }

        final var docElement = parserPool.parse(input).getDocumentElement();
        if (docElement == null) {
            throw new XMLParserException("could not parse " + trimmedPath);
        }
        return docElement;
    }

    /**
     * Reads in an XML file and returns it as a new {@link DOMElementItem}.
     * 
     * @param path classpath path to the data file, never null
     * 
     * @return an {@link Item} wrapping the document representing the data file, never <code>null</code>
     * 
     * @throws XMLParserException if the file does not exist or there is a problem parsing it
     */
    public @Nonnull Item<Element> readDOMItem(final @Nonnull String path) throws XMLParserException {
        final Element e = readXMLData(path);
        return new DOMElementItem(e);
    }

    /**
     * Create a DOM {@link Item} from a {@link String}.
     *
     * @param text text to turn into a DOM item
     * @return DOM item corresponding to the provided text
     * @throws XMLParserException 
     */
    protected @Nonnull Item<Element> parseDOMItem(final @Nonnull String text) throws XMLParserException {
        try (var reader = new StringReader(text)) {
            return new DOMElementItem(getParserPool().parse(reader));
        }
    }

    /**
     * Checks whether two nodes are identical.
     *
     * The only variation that is permitted for the purpose of comparison is that
     * adjacent text nodes will be coalesced.
     *
     * Tests requiring other semantics should call XMLUnit directly.
     *
     * @param expected the expected node against which the actual node will be tested, never null
     * @param actual the actual node tested against the expected node, never null
     */
    public void assertXMLIdentical(@Nonnull final Node expected, @Nonnull final Node actual) {
        Constraint.isNotNull(expected, "Expected Node may not be null");
        Constraint.isNotNull(actual, "Actual Node may not be null");

        /*
         * Normalize empty and adjacent text nodes within the source nodes.
         *
         * Don't try to simplify this by passing expected and actual directly to the
         * NormalizedSource(Node) constructor. That's much faster, as the constructor just
         * normalizes the provided node, but by the same token it causes the original
         * node to be changed, and side-effects are undesirable in a general-use method
         * like this one.
         */
        final Source expectedSource = new NormalizedSource(Input.fromNode(expected).build());
        final Source actualSource = new NormalizedSource(Input.fromNode(actual).build());

        final Diff diff = DiffBuilder.compare(expectedSource).withTest(actualSource)
                .checkForIdentical()
                .build();

        if (diff.hasDifferences()) {
            System.out.println("Expected:\n" + SerializeSupport.nodeToString(expected));
            System.out.println("Actual:\n" + SerializeSupport.nodeToString(actual));
            Assert.fail(diff.toString());
        }
    }

    protected int countErrors(final Item<Element> item) {
        final ClassToInstanceMultiMap<ItemMetadata> metadata = item.getItemMetadata();
        final List<ErrorStatus> errors = metadata.get(ErrorStatus.class);
        return errors.size();
    }

}
