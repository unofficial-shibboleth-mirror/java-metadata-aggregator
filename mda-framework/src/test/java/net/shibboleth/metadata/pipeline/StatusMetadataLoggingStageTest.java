
package net.shibboleth.metadata.pipeline;

import java.util.List;

import javax.annotation.Nonnull;

import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.InfoStatus;
import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemIdentificationStrategy;
import net.shibboleth.metadata.MockItem;
import net.shibboleth.metadata.WarningStatus;
import net.shibboleth.shared.collection.CollectionSupport;


public class StatusMetadataLoggingStageTest {

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeMethod
    private void initializeLogger() {
        // Find the (logback) logger for the class under test
        logger = (Logger)LoggerFactory.getLogger(StatusMetadataLoggingStage.class);

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

    private static class StringSelfIdentificationStrategy implements ItemIdentificationStrategy<String> {

        @Override
        public @Nonnull String getItemIdentifier(@Nonnull Item<String> item) {
            return item.unwrap();
        }
        
    }

    private @Nonnull List<Item<String>> getItems() {
        final @Nonnull List<Item<String>> items = CollectionSupport.listOf(new MockItem("item1"),
                new MockItem("item2"), new MockItem("item3"));
        items.get(0).getItemMetadata().put(new ErrorStatus("comp1", "err1"));
        items.get(0).getItemMetadata().put(new ErrorStatus("comp1", "err2"));
        items.get(0).getItemMetadata().put(new WarningStatus("comp1", "warn1"));
        items.get(0).getItemMetadata().put(new InfoStatus("comp1", "info1"));
        items.get(2).getItemMetadata().put(new WarningStatus("comp1", "warn2"));
        return items;
    }

    @Test
    public void testLoggingAllKinds() throws Exception {
        final var items = getItems();
        final var stage = new StatusMetadataLoggingStage<String>();
        stage.setId("test");
        stage.setIdentificationStrategy(new StringSelfIdentificationStrategy());
        stage.setSelectionRequirements(CollectionSupport.setOf(ErrorStatus.class, InfoStatus.class, WarningStatus.class));
        stage.initialize();
        
        stage.execute(items);

        stage.destroy();
        
        final var logsList = listAppender.list;
        Assert.assertEquals(logsList.size(), 9); // 2+1 + 1+1 + 1+1 + 1+1
    }

    @Test
    public void testLoggingJustErrors() throws Exception {
        final var items = getItems();
        final var stage = new StatusMetadataLoggingStage<String>();
        stage.setId("test");
        stage.setIdentificationStrategy(new StringSelfIdentificationStrategy());
        stage.setSelectionRequirements(CollectionSupport.setOf(ErrorStatus.class));
        stage.initialize();
        
        stage.execute(items);

        stage.destroy();
        
        final var logsList = listAppender.list;
        Assert.assertEquals(logsList.size(), 3); // 2+1
    }
}
