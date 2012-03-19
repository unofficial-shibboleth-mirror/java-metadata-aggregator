/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.pipeline;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Assert;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pipeline stage that computes that transforms the collection of {@link Item} via a script.
 * 
 * <p>
 * This stage requires the following properties be set prior to initialization:
 * <ul>
 * <li><code>scriptFile</code></li>
 * </ul>
 * 
 * <p>
 * This classes uses the JSR-223 scripting interface. As such, in order to use a language other than ECMAscript (a.k.a.
 * javascript), you must ensure the scripting engine and any associated libraries necessary for its operation are on the
 * classpath.
 */
@ThreadSafe
public class ScriptletStage extends BaseStage<Item<?>> {

    /** Name of the scriptlet attribute, {@value} , containing the Item collection to be transformed. */
    public static final String ITEMS = "items";

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ScriptletStage.class);

    /** Script executed by this stage. */
    private EvaluableScript script;

    /**
     * Gets the script executed by this stage.
     * 
     * @return the script executed by this stage
     */
    @Nullable public EvaluableScript getScript() {
        return script;
    }

    /**
     * Sets the script executed by this stage.
     * 
     * @param stageScript the script executed by this stage
     */
    public synchronized void setScript(@Nonnull final EvaluableScript stageScript) {
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        script = Assert.isNotNull(stageScript, "Stage script can not be null");
    }

    /** {@inheritDoc} */
    protected void doExecute(@Nonnull @NonnullElements final Collection<Item<?>> itemCollection)
            throws StageProcessingException {
        final SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute(ITEMS, itemCollection, SimpleScriptContext.ENGINE_SCOPE);

        try {
            script.eval(context);
        } catch (ScriptException e) {
            String errMsg = getId() + " pipeline stage unable to execute script";
            log.error(errMsg, e);
            throw new StageProcessingException(errMsg, e);
        }
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (script == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId() + ", script may not be null");
        }
    }
}