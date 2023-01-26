
package net.shibboleth.metadata;

import org.testng.annotations.Test;

import net.shibboleth.shared.logic.ConstraintViolationException;

public class MockItemTest {

    @Test(expectedExceptions=ConstraintViolationException.class)
    public void testNull() {
        new MockItem(null);
    }

}
