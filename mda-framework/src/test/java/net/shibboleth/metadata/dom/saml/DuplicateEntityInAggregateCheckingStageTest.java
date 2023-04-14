package net.shibboleth.metadata.dom.saml;

import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.ErrorStatus;
import net.shibboleth.metadata.dom.testing.BaseDOMTest;
import net.shibboleth.shared.collection.CollectionSupport;

public class DuplicateEntityInAggregateCheckingStageTest extends BaseDOMTest {

    protected DuplicateEntityInAggregateCheckingStageTest() {
        super(DuplicateEntityInAggregateCheckingStage.class);
    }

    @Test
    public void hasDuplicates() throws Exception {
        final var item = readDOMItem("hasDuplicates.xml");
        
        final var stage = new DuplicateEntityInAggregateCheckingStage();
        stage.setId("test");
        stage.initialize();
        stage.execute(CollectionSupport.listOf(item));
        stage.destroy();
        
        final var errors = item.getItemMetadata().get(ErrorStatus.class);
        Assert.assertEquals(errors.size(), 2);
        
        // The errors can appear in any order in the item metadata.
        final var messages = new HashSet<String>();
        for (final var error : errors) {
            messages.add(error.getStatusMessage());
        }
        Assert.assertTrue(messages.contains("duplicate entityID: first"));
        Assert.assertTrue(messages.contains("duplicate entityID: second"));
    }
}
