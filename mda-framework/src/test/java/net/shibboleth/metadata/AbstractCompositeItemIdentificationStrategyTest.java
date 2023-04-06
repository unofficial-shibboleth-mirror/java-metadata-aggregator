
package net.shibboleth.metadata;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.testing.MockItem;

public class AbstractCompositeItemIdentificationStrategyTest {

    /**
     * Specialised identification strategy for {@link Item}&lt;{@link String}&gt;.
     */
    static class StringItemIdentificationStrategy extends AbstractCompositeItemIdentificationStrategy<String> {

        @Override
        String getBasicIdentifier(final @Nonnull Item<String> item) {
            return "[basic:" + item.unwrap() + "]";
        }

        @Override
        String getExtraIdentifier(final @Nonnull Item<String> item) {
            return "[extra:" + item.unwrap() + "]";
        }
        
    }

    /**
     * Test the construction and execution of a type-specialised item identification strategy.
     */
    @Test
    public void testSpecificType() {
        final Item<String> item = new MockItem("howdy");
        final ItemIdentificationStrategy<String> strat = new StringItemIdentificationStrategy();
        final var id = strat.getItemIdentifier(item);
        Assert.assertEquals(id, "[basic:howdy] ([extra:howdy])");
    }

}
