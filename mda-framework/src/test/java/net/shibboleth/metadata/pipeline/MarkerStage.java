package net.shibboleth.metadata.pipeline;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.TestMarker;

/**
 * A stage which adds a sequentially numbered {link @TestMarker} to item metadata.
 * 
 * <p>
 * An instance of this class applies a <em>different</em> marker every time
 * it is applied to an item.
 * </p>
 *
 * @param <T> type of item to be processed by the stage
 */
@ThreadSafe
public class MarkerStage<T> extends AbstractIteratingStage<T> {

    @GuardedBy("this") private int sequence = 1;
    
    private final synchronized String nextMessage() {
        return "marker #" + sequence++;
    }

    @Override
    protected void doExecute(Item<T> item) throws StageProcessingException {
        item.getItemMetadata().put(new TestMarker(nextMessage()));
    }

}
