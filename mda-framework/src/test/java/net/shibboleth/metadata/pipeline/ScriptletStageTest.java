
package net.shibboleth.metadata.pipeline;

import javax.script.Compilable;
import javax.script.ScriptEngineManager;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.shared.spring.resource.ResourceHelper;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.testing.BaseTest;
import net.shibboleth.metadata.testing.MockItem;
import net.shibboleth.metadata.testing.TestMarker;
import net.shibboleth.shared.collection.CollectionSupport;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.scripting.EvaluableScript;

public class ScriptletStageTest extends BaseTest {

    protected ScriptletStageTest() {
        super(ScriptletStage.class);
    }

    @Test(expectedExceptions = {ComponentInitializationException.class})
    public void testNoScript() throws Exception {
        final var stage = new ScriptletStage<String>();
        stage.setId("test");
        stage.initialize();
    }

    @Test
    public void testEngineFactories() throws Exception {
        // Enumerates the engines available and some of their attributes
        final var manager = new ScriptEngineManager();
        final var factories = manager.getEngineFactories();
        for (var factory : factories) {
            System.out.println(factory.getEngineName() + ": " + factory.getLanguageName());
            final var engine = factory.getScriptEngine();
            System.out.println("    Compilable: " + (engine instanceof Compilable));
            for (String name : factory.getNames()) {
                System.out.println("       " + name);
            }
        }
    }

    @Test
    public void testJavascript() throws Exception {
        // pick up the script
        final var scriptResource = getClasspathResource("script.js");
        
        final var script = new EvaluableScript();
        script.setScript(ResourceHelper.of(scriptResource));
        script.initialize();

        final var stage = new ScriptletStage<String>();
        stage.setId("test");
        stage.setScript(script);
        stage.initialize();

        final var items = CollectionSupport.<Item<String>>listOf(new MockItem("one"), new MockItem("two"));
        stage.execute(items);

        // The script should have added a TestMarker to each item.

        var item1 = items.get(0);
        var marks1 = item1.getItemMetadata().get(TestMarker.class);
        Assert.assertEquals(marks1.size(), 1);
        var mark1 = marks1.get(0).getMarker();
        Assert.assertEquals(mark1, "foo 0");

        var item2 = items.get(1);
        var marks2 = item2.getItemMetadata().get(TestMarker.class);
        Assert.assertEquals(marks2.size(), 1);
        var mark2 = marks2.get(0).getMarker();
        Assert.assertEquals(mark2, "foo 1");
    }

    @Test
    public void testBadJavascript() throws Exception {
        // pick up the script
        final var scriptResource = getClasspathResource("bad.js");
        
        final var script = new EvaluableScript();
        script.setScript(ResourceHelper.of(scriptResource));
        script.initialize();

        final var stage = new ScriptletStage<String>();
        stage.setId("test");
        stage.setScript(script);
        stage.initialize();
        
        final var items = CollectionSupport.<Item<String>>listOf(new MockItem("one"), new MockItem("two"));
        
        try {
            stage.execute(items);
            Assert.fail("expected an exception");
        } catch (StageProcessingException e) {
            // expected
        }

        // The script should have added a TestMarker to the first item, not to the second.

        var item1 = items.get(0);
        var marks1 = item1.getItemMetadata().get(TestMarker.class);
        Assert.assertEquals(marks1.size(), 1);
        var mark1 = marks1.get(0).getMarker();
        Assert.assertEquals(mark1, "foo 0");

        var item2 = items.get(1);
        var marks2 = item2.getItemMetadata().get(TestMarker.class);
        Assert.assertEquals(marks2.size(), 0);
    }

    @Test
    public void testRuby() throws Exception {
        // pick up the script
        final var scriptResource = getClasspathResource("script.rb");
        
        final var script = new EvaluableScript();
        script.setScript(ResourceHelper.of(scriptResource));
        script.setEngineName("ruby");
        script.initialize();

        final var stage = new ScriptletStage<String>();
        stage.setId("test");
        stage.setScript(script);
        stage.setVariableName("$items");
        stage.initialize();
        
        final var items = CollectionSupport.<Item<String>>listOf(new MockItem("one"), new MockItem("two"));
        stage.execute(items);
        
        // The script should have added a TestMarker to each item.

        var item1 = items.get(0);
        var marks1 = item1.getItemMetadata().get(TestMarker.class);
        Assert.assertEquals(marks1.size(), 1);
        var mark1 = marks1.get(0).getMarker();
        Assert.assertEquals(mark1, "foo 0");

        var item2 = items.get(1);
        var marks2 = item2.getItemMetadata().get(TestMarker.class);
        Assert.assertEquals(marks2.size(), 1);
        var mark2 = marks2.get(0).getMarker();
        Assert.assertEquals(mark2, "foo 1");
    }

    @Test
    public void testPython() throws Exception {
        // pick up the script
        final var scriptResource = getClasspathResource("script.py");
        
        final var script = new EvaluableScript();
        script.setScript(ResourceHelper.of(scriptResource));
        script.setEngineName("python");
        script.initialize();

        final var stage = new ScriptletStage<String>();
        stage.setId("test");
        stage.setScript(script);
        stage.initialize();
        
        final var items = CollectionSupport.<Item<String>>listOf(new MockItem("one"), new MockItem("two"));
        stage.execute(items);
        
        // The script should have added a TestMarker to each item.

        var item1 = items.get(0);
        var marks1 = item1.getItemMetadata().get(TestMarker.class);
        Assert.assertEquals(marks1.size(), 1);
        var mark1 = marks1.get(0).getMarker();
        Assert.assertEquals(mark1, "foo 0");

        var item2 = items.get(1);
        var marks2 = item2.getItemMetadata().get(TestMarker.class);
        Assert.assertEquals(marks2.size(), 1);
        var mark2 = marks2.get(0).getMarker();
        Assert.assertEquals(mark2, "foo 1");
    }
}
