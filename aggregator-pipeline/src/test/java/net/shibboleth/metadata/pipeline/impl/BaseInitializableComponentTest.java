
package net.shibboleth.metadata.pipeline.impl;

import org.testng.annotations.Test;

import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;

public class BaseInitializableComponentTest {

    private class TestClass extends BaseInitializableComponent {
    }
    
    @Test(expectedExceptions = {DestroyedComponentException.class})
    public void ifDestroyedYes() throws Exception {
        final var c = new TestClass();
        c.initialize();
        c.destroy();
        c.ifDestroyedThrowDestroyedComponentException();
    }

    @Test
    public void ifDestroyedNo() throws Exception {
        final var c = new TestClass();
        c.initialize();
        c.ifDestroyedThrowDestroyedComponentException();
        c.destroy();
    }

    @Test(expectedExceptions = {UninitializedComponentException.class})
    public void ifNotInitializedYes() throws Exception {
        final var c = new TestClass();
        c.ifNotInitializedThrowUninitializedComponentException();
    }

    @Test
    public void ifNotInitializedNo() throws Exception {
        final var c = new TestClass();
        c.initialize();
        c.ifNotInitializedThrowUninitializedComponentException();
        c.destroy();
    }

    @Test(expectedExceptions = {UnmodifiableComponentException.class})
    public void ifInitializedYes() throws Exception {
        final var c = new TestClass();
        c.initialize();
        c.ifInitializedThrowUnmodifiabledComponentException();
    }

    @Test
    public void ifInitializedNo() throws Exception {
        final var c = new TestClass();
        c.ifInitializedThrowUnmodifiabledComponentException();
    }

}
