package net.shibboleth.metadata.pipeline;

import java.util.Collection;

import net.shibboleth.metadata.Item;

/** Stage that throws a TerminationException when it is called. */
class TerminatingStage<ItemType extends Item<?>> extends BaseStage<ItemType> {

    /** Constructor. */
    public TerminatingStage() {
        setId("TerminatingStage");
    }

    /** {@inheritDoc} */
    protected void doExecute(Collection<ItemType> itemCollection) throws StageProcessingException {
        throw new TerminationException("from TerminatingStage");
    }
}
