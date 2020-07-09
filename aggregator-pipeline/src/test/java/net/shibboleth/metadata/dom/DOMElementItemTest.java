
package net.shibboleth.metadata.dom;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

public class DOMElementItemTest {

    @Test(expectedExceptions=ConstraintViolationException.class)
    public void testNullDocument() {
        new DOMElementItem((Document)null);
    }

    @Test(expectedExceptions=ConstraintViolationException.class)
    public void testNullElement() {
        new DOMElementItem((Element)null);
    }
}
