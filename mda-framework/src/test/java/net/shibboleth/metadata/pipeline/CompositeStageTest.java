
package net.shibboleth.metadata.pipeline;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.TestMarker;

public class CompositeStageTest {

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeMethod
    private void initializeLogger() {
        // Find the (logback) logger for the class under test
        logger = (Logger)LoggerFactory.getLogger(CompositeStage.class);

        // Create and start a ListAppender
        listAppender = new ListAppender<>();
        listAppender.start();

        // Add the appender to the logger.
        logger.addAppender(listAppender);
    }
    
    @AfterMethod
    private void terminateLogger() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    public void doExecute0Test() throws Exception {
        final var stage = new CompositeStage<String>();
        stage.setId("test");
        stage.initialize();
        Assert.assertEquals(stage.getStages().size(), 0);
        
        final var items = List.<Item<String>>of(new MockItem("hello"));
        stage.execute(items);
        Assert.assertEquals(items.size(), 1);
        // No stages --> no errors added
        Assert.assertEquals(items.get(0).getItemMetadata().get(TestMarker.class).size(), 0);
    }

    @Test
    public void doExecute1Test() throws Exception {
        final var marker = new MarkerStage<String>();
        marker.setId("marker");
        marker.initialize();

        final var stage = new CompositeStage<String>();
        stage.setId("test");
        stage.setStages(List.of(marker));
        stage.initialize();
        Assert.assertEquals(stage.getStages().size(), 1);

        final var items = List.<Item<String>>of(new MockItem("hello"));
        stage.execute(items);
        Assert.assertEquals(items.size(), 1);
        // One stage --> one error added
        Assert.assertEquals(items.get(0).getItemMetadata().get(TestMarker.class).size(), 1);
    }

    @Test
    public void doExecute2Test() throws Exception {
        final var marker = new MarkerStage<String>();
        marker.setId("marker");
        marker.initialize();

        final var stage = new CompositeStage<String>();
        stage.setId("test");
        stage.setStages(List.of(marker, marker));
        stage.initialize();
        Assert.assertEquals(stage.getStages().size(), 2);

        final var items = List.<Item<String>>of(new MockItem("hello"));
        stage.execute(items);
        Assert.assertEquals(items.size(), 1);
        // Two stages --> two errors added
        Assert.assertEquals(items.get(0).getItemMetadata().get(TestMarker.class).size(), 2);
    }

    @Test
    public void doDestroyTest() throws Exception {
        final var stage = new CompositeStage<String>();
        stage.setId("test");
        stage.initialize();
        stage.destroy();
    }

    @Test
    public void testNotLogging() throws Exception {
    	final var marker = new MarkerStage<String>();
    	marker.setId("marker");
        marker.initialize();

        final var stage = new CompositeStage<String>();
        stage.setId("test");
        stage.setStages(List.of(marker, marker));
        stage.initialize();
        Assert.assertFalse(stage.isLoggingProgress());
        Assert.assertEquals(stage.getStages().size(), 2);

        final var items = List.<Item<String>>of(new MockItem("hello"));
        stage.execute(items);
        stage.destroy();
        
        marker.destroy();

        // No logging has been performed
        final var logsList = listAppender.list;
        Assert.assertEquals(logsList.size(), 0);
    }

    @Test
    public void testLogging() throws Exception {
    	final var marker = new MarkerStage<String>();
    	marker.setId("marker");
        marker.initialize();

        final var stage = new CompositeStage<String>();
        stage.setId("test");
        stage.setStages(List.of(marker, marker));
        stage.setLoggingProgress(true);
        stage.initialize();
        Assert.assertTrue(stage.isLoggingProgress());
        Assert.assertEquals(stage.getStages().size(), 2);

        final var items = List.<Item<String>>of(new MockItem("hello"));
        stage.execute(items);
        stage.destroy();
        
        marker.destroy();

        // Two log lines from each stage, plus one from the composite.
        final var logsList = listAppender.list;
        Assert.assertEquals(logsList.size(), 5);
        for (final var line : logsList) {
        	Assert.assertEquals(line.getLevel(), Level.INFO);
        }
    }

    @Test
    public void testDeprecatedMethods() throws Exception {
        final var marker = new MarkerStage<String>();
        marker.setId("marker");
        marker.initialize();

        final var stage = new CompositeStage<String>();
        stage.setId("test");
        Assert.assertEquals(stage.getComposedStages().size(), 0);

        stage.setComposedStages(List.of(marker));
        Assert.assertEquals(stage.getStages().size(), 1);

        stage.destroy();
    }
}
