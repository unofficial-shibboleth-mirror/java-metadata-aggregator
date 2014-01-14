package net.shibboleth.metadata.pipeline;

import java.util.Collection;

import net.shibboleth.metadata.Item;

/** Stage that throws a TerminationException when it is called. */
class TerminatingStage<T> extends BaseStage<T> {

    /** Constructor. */
    public TerminatingStage() {
        setId("TerminatingStage");
    }

    /** {@inheritDoc} */
    @Override protected void doExecute(Collection<Item<T>> itemCollection) throws StageProcessingException {
        throw new TerminationException("from TerminatingStage");
    }
}
