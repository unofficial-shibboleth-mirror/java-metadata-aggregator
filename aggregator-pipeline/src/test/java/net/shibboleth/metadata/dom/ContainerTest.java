
package net.shibboleth.metadata.dom;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ContainerTest extends BaseDOMTest {

    private final Document doc;

    protected ContainerTest() throws Exception {
        super(Container.class);
        setUp();
        doc = getParserPool().newDocument();
    }

    @Test
    public void prime() {
        // simple top-level element with no indentation
        final Element e1 = doc.createElementNS("ns", "el");
        final Container c1 = new Container(e1);
        c1.prime();
        Assert.assertEquals(e1.getTextContent(), "\n");

        // prime it again
        c1.prime();
        Assert.assertEquals(e1.getTextContent(), "\n");

        // an indented container
        final Element e2 = doc.createElementNS("ns", "el2");
        e1.insertBefore(e2, e1.getFirstChild());
        final Container c2 = c1.findChild(x -> true);
        Assert.assertNotNull(c2);
        c2.prime();
        Assert.assertEquals(e2.getTextContent(), "\n    ");

        // prime it again
        c2.prime();
        Assert.assertEquals(e2.getTextContent(), "\n    ");
    }

    @Test
    public void addChildElementFirst() throws Exception {
        final Element e1 = doc.createElementNS("ns", "root");
        final Element e2 = doc.createElementNS("ns", "child");
        e2.setTextContent("child value");
        final Container c1 = new Container(e1);
        c1.addChild(e2, Container.FIRST_CHILD);

        final Element ok = readXMLData("add1.xml");
        assertXMLIdentical(ok, e1);

        final Element e3 = doc.createElementNS("ns", "child2");
        e3.setTextContent("child 2 value");
        c1.addChild(e3, Container.FIRST_CHILD);
        final Element ok2 = readXMLData("addFirst.xml");
        assertXMLIdentical(ok2, e1);
    }

    @Test
    public void addChildElementLast() throws Exception {
        final Element e1 = doc.createElementNS("ns", "root");
        final Element e2 = doc.createElementNS("ns", "child");
        e2.setTextContent("child value");
        final Container c1 = new Container(e1);
        c1.addChild(e2, Container.LAST_CHILD);

        final Element ok = readXMLData("add1.xml");
        assertXMLIdentical(ok, e1);

        final Element e3 = doc.createElementNS("ns", "child2");
        e3.setTextContent("child 2 value");
        c1.addChild(e3, Container.LAST_CHILD);
        final Element ok2 = readXMLData("addLast.xml");
        assertXMLIdentical(ok2, e1);
    }

    @Test
    public void addChildElementNested() throws Exception {
        final Element root = doc.createElementNS("ns", "root");
        final Element mid = doc.createElementNS("ns", "mid");
        final Element leaf1 = doc.createElementNS("ns", "leaf");
        leaf1.setTextContent("leaf 1");
        final Element leaf2 = doc.createElementNS("ns", "leaf");
        leaf2.setTextContent("leaf 2");
        final Container rootContainer = new Container(root);
        final Container midContainer = rootContainer.addChild(mid, Container.FIRST_CHILD);
        midContainer.addChild(leaf1, Container.LAST_CHILD);
        midContainer.addChild(leaf2, Container.LAST_CHILD);

        final Element ok = readXMLData("nested.xml");
        assertXMLIdentical(ok, root);
    }

    @Test
    public void addChildFunctionFirst() throws Exception {
        final Element e1 = doc.createElementNS("ns", "root");
        final Container c1 = new Container(e1);

        c1.addChild(new Function<Container, Element>(){

            @Override
            public Element apply(Container input) {
                final Element e2 = doc.createElementNS("ns", "child");
                e2.setTextContent("child value");
                return e2;
            }
            
        }, Container.FIRST_CHILD);

        final Element ok = readXMLData("add1.xml");
        assertXMLIdentical(ok, e1);

        c1.addChild(new Function<Container, Element>(){

            @Override
            public Element apply(Container input) {
                final Element e3 = doc.createElementNS("ns", "child2");
                e3.setTextContent("child 2 value");
                return e3;
            }
            
        }, Container.FIRST_CHILD);

        final Element ok2 = readXMLData("addFirst.xml");
        assertXMLIdentical(ok2, e1);
    }

    @Test
    public void addChildFunctionLast() throws Exception {
        final Element e1 = doc.createElementNS("ns", "root");
        final Container c1 = new Container(e1);

        c1.addChild(new Function<Container, Element>(){

            @Override
            public Element apply(Container input) {
                final Element e2 = doc.createElementNS("ns", "child");
                e2.setTextContent("child value");
                return e2;
            }
            
        }, Container.LAST_CHILD);

        final Element ok = readXMLData("add1.xml");
        assertXMLIdentical(ok, e1);

        c1.addChild(new Function<Container, Element>(){

            @Override
            public Element apply(Container input) {
                final Element e3 = doc.createElementNS("ns", "child2");
                e3.setTextContent("child 2 value");
                return e3;
            }
            
        }, Container.LAST_CHILD);

        final Element ok2 = readXMLData("addLast.xml");
        assertXMLIdentical(ok2, e1);
    }

    @Test
    public void unwrap() {
        final Element leaf1 = doc.createElementNS("ns", "leaf");
        leaf1.setTextContent("leaf 1");
        final Container c = new Container(leaf1);
        Assert.assertEquals(c.unwrap().getTextContent(), "leaf 1");
    }

    @Test
    public void setText() {
        final Element e = doc.createElementNS("ns", "root");
        final Container c = new Container(e);
        c.setText("some text");
        Assert.assertEquals(e.getTextContent(), "some text");
    }

    @Test
    public void findChild() throws Exception {
        final Container root = new Container(readXMLData("find.xml"));
        final Container child = root.findChild(new Predicate<Element>(){

            @Override
            public boolean test(Element input) {
                return "findme".equals(input.getLocalName());
            }
            
        });
        Assert.assertNotNull(child);
        Assert.assertEquals(child.unwrap().getTextContent(), "find me 1");
    }

    @Test
    public void findChildren() throws Exception {
        final Container root = new Container(readXMLData("find.xml"));
        final List<Container> children = root.findChildren(new Predicate<Element>(){

            @Override
            public boolean test(Element input) {
                return "findme".equals(input.getLocalName());
            }
            
        });
        Assert.assertNotNull(children);
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).unwrap().getTextContent(), "find me 1");
        Assert.assertEquals(children.get(1).unwrap().getTextContent(), "find me 2");
        Assert.assertEquals(children.get(2).unwrap().getTextContent(), "find me 3");
    }

    @Test
    public void locateChild() throws Exception {
        final Element e1 = doc.createElementNS("ns", "root");
        final Container c1 = new Container(e1);

        c1.locateChild(new Predicate<Element>(){

            @Override
            public boolean test(Element input) {
                return "child".equals(input.getLocalName());
            }
            
            
        }, new Function<Container, Element>(){

            @Override
            public Element apply(Container input) {
                final Element e2 = doc.createElementNS("ns", "child");
                e2.setTextContent("child value");
                return e2;
            }
            
        }, Container.LAST_CHILD);

        final Element ok = readXMLData("add1.xml");
        assertXMLIdentical(ok, e1);

        // same again should NOT change the result for locate

        c1.locateChild(new Predicate<Element>(){

            @Override
            public boolean test(Element input) {
                return "child".equals(input.getLocalName());
            }
            
            
        }, new Function<Container, Element>(){

            @Override
            public Element apply(Container input) {
                final Element e2 = doc.createElementNS("ns", "child");
                e2.setTextContent("child value");
                return e2;
            }
            
        }, Container.LAST_CHILD);

        final Element ok2 = readXMLData("add1.xml");
        assertXMLIdentical(ok2, e1);

    }
}
