
package net.shibboleth.metadata;

import org.testng.annotations.Test;

import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

public class MockItemTest {

    @Test(expectedExceptions=ConstraintViolationException.class)
    public void testNull() {
        new MockItem(null);
    }

}
