
package net.shibboleth.metadata;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BeansFileTest {

    @Test
    public void testBeans() throws Exception {
        // Create an application context, which also acts as a bean definition registry
        final GenericApplicationContext ctx = new GenericApplicationContext();

        // Load bean definitions into the registry
        final XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        xmlReader.loadBeanDefinitions(new ClassPathResource("net/shibboleth/metadata/beans.xml"));

        final String[] defNames = ctx.getBeanDefinitionNames();
        for (final String defName : defNames) {
            final BeanDefinition def = ctx.getBeanDefinition(defName);

            // All bean definitions should start with "mda."
            Assert.assertTrue(defName.startsWith("mda."), "does not start with correct prefix: " + defName);

            // All bean definitions should be abstract
            Assert.assertTrue(def.isAbstract(), "not abstract: " + defName);

            // Deal with the class name, if the definition has one
            final String className = def.getBeanClassName();
            if (className != null) {
                try {
                    // Check that the class can be loaded
                    Class.forName(className);

                    // The name of the class within its package should be included in the bean
                    // name's prefix; there may be more after that
                    final String classLastPart = className.replaceFirst("^.*\\.", "");
                    Assert.assertTrue(defName.startsWith("mda." + classLastPart), "does not start with correct prefix: " + defName);

                    // If the class represents a stage, its parent should be the stage parent
                    if (className.endsWith("Stage")) {
                        Assert.assertEquals(def.getParentName(), "mda.stage_parent");
                    }
                } catch (ClassNotFoundException e) {
                    // Could not load class
                    Assert.fail("could not load class " + className + " for bean " + defName);
                }
            }
        }

        // Refresh the context to process the bean definitions
        ctx.refresh();

    }

}
