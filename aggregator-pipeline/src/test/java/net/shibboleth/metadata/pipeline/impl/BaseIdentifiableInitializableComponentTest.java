
package net.shibboleth.metadata.pipeline.impl;

import org.testng.annotations.Test;

import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.component.UnmodifiableComponentException;

public class BaseIdentifiableInitializableComponentTest {

    private class TestClass extends BaseIdentifiableInitializableComponent {
    }
    
    
    @Test(expectedExceptions = {DestroyedComponentException.class})
    public void ifDestroyedYes() throws Exception {
        final var c = new TestClass();
        c.setId("test");
        c.initialize();
        c.destroy();
        c.ifDestroyedThrowDestroyedComponentException();
    }

    @Test
    public void ifDestroyedNo() throws Exception {
        final var c = new TestClass();
        c.ifDestroyedThrowDestroyedComponentException();
        c.setId("test");
        c.initialize();
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
        c.setId("test");
        c.initialize();
        c.ifNotInitializedThrowUninitializedComponentException();
        c.destroy();
    }

    @Test(expectedExceptions = {UnmodifiableComponentException.class})
    public void ifInitializedYes() throws Exception {
        final var c = new TestClass();
        c.setId("test");
        c.initialize();
        c.ifInitializedThrowUnmodifiabledComponentException();
    }

    @Test
    public void ifInitializedNo() throws Exception {
        final var c = new TestClass();
        c.ifInitializedThrowUnmodifiabledComponentException();
    }


}
