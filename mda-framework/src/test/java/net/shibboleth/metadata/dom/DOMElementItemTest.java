
package net.shibboleth.metadata.dom;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.shared.logic.ConstraintViolationException;

public class DOMElementItemTest extends BaseDOMTest {

    protected DOMElementItemTest() {
        super(DOMElementItem.class);
    }

    @SuppressWarnings("null")
    @Test(expectedExceptions=ConstraintViolationException.class)
    public void testNullDocument() {
        new DOMElementItem((Document)null);
    }

    @SuppressWarnings("null")
    @Test(expectedExceptions=ConstraintViolationException.class)
    public void testNullElement() {
        new DOMElementItem((Element)null);
    }
    
    @Test
    public void testCopy() throws Exception {
        // Build an item with some metadata
        final var e = readXMLData("in.xml");
        final Item<Element> orig = new DOMElementItem(e);
        final var origId = new ItemId("item");
        orig.getItemMetadata().put(origId);

        // Make a copy
        final Item<Element> copy = orig.copy();

        // The original and copy should be different, their metadata collections
        // different, the actual metadata the same.
        final var copyId = copy.getItemMetadata().get(ItemId.class).get(0);
        Assert.assertNotNull(copyId);
        Assert.assertSame(copyId, origId);
        Assert.assertNotSame(copy, orig);
        Assert.assertNotSame(copy.getItemMetadata(), orig.getItemMetadata());
        
        // Values should be identical
        assertXMLIdentical(copy.unwrap(), orig.unwrap());
        
        // Values should be in different documents.
        Assert.assertNotSame(copy.unwrap().getOwnerDocument(), orig.unwrap().getOwnerDocument());
    }
}
