
package net.shibboleth.metadata;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BeansFileTest {

    @Test
    public void testBeans() throws Exception {

        // Set of beans which don't follow some of the normal rules
        final Set<String> specialBeans = Set.of(
                "mda.MigrationAssistanceBean",
                "mda.MigrationClassMap",
                "mda.MigrationBeanMap"
                );

        // Create an application context, which also acts as a bean definition registry
        final GenericApplicationContext ctx = new GenericApplicationContext();

        // Load bean definitions into the registry
        final XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        xmlReader.loadBeanDefinitions(new ClassPathResource("net/shibboleth/metadata/beans.xml"));

        final String[] defNames = ctx.getBeanDefinitionNames();
        for (final String defName : defNames) {
            assert defName != null;
            final BeanDefinition def = ctx.getBeanDefinition(defName);

            // All bean definitions should start with "mda."
            Assert.assertTrue(defName.startsWith("mda."), "does not start with correct prefix: " + defName);

            // Skip special beans on the first pass, process them only after the context has
            // been refreshed.
            if (specialBeans.contains(defName)) {
                continue;
            }

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

        // Process the special beans
        for (final String defName : defNames) {
            switch (defName) {
                /*
                 * Check that the mapped classes in the migration class map
                 * resolve to a class that actually exists.
                 */
                case "mda.MigrationClassMap" -> {
                    @SuppressWarnings("unchecked")
                    final Map<String, String> map = ctx.getBean(defName, Map.class);
                    for (String toClass : map.values()) {
                        // check that the mapped class name can be loaded
                        Class.forName(toClass);
                    }
                }

                /*
                 * Check that the mapped beans in the migration bean map
                 * have definitions.
                 */
                case "mda.MigrationBeanMap" -> {
                    @SuppressWarnings("unchecked")
                    final Map<String, String> map = ctx.getBean(defName, Map.class);
                    for (String toBean : map.values()) {
                        assert toBean != null;
                        // check that the mapped bean name is defined
                        ctx.getBeanDefinition(toBean);
                    }
                }
                    
            }

        }
    }

}
