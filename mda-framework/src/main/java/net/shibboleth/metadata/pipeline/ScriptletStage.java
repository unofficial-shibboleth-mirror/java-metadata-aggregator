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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import net.shibboleth.metadata.Item;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

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
 * 
 * @param <T> type of item the stage operates on
 */
@ThreadSafe
public class ScriptletStage<T> extends AbstractStage<T> {

    /**
     * Name of the scriptlet attribute containing the {@link Item} collection to be transformed.
     *
     * <p>Defaults to <code>items</code>.</p>
     */
    @GuardedBy("this") @Nonnull @NotEmpty private String variableName = "items";

    /** Script executed by this stage. */
    @NonnullAfterInit @GuardedBy("this")
    private EvaluableScript script;

    /**
     * Gets the variable name to contain the list of items.
     * 
     * @return the variable name
     */
    public final synchronized String getVariableName() {
        return variableName;
    }
    
    /**
     * Sets the variable name to contain the list of items.
     *
     * @param name the variable name
     */
    public final synchronized void setVariableName(@Nonnull @NotEmpty final String name) {
        checkSetterPreconditions();
        variableName = Constraint.isNotNull(StringSupport.trimOrNull(name),
                "variable name may not be null or empty");
    }

    /**
     * Gets the script executed by this stage.
     * 
     * @return the script executed by this stage
     */
    @Nullable public final synchronized EvaluableScript getScript() {
        return script;
    }

    /**
     * Sets the script executed by this stage.
     * 
     * @param stageScript the script executed by this stage
     */
    public synchronized void setScript(@Nonnull final EvaluableScript stageScript) {
        checkSetterPreconditions();
        script = Constraint.isNotNull(stageScript, "Stage script can not be null");
    }

    @Override
    protected void doExecute(@Nonnull @NonnullElements final List<Item<T>> items)
            throws StageProcessingException {
        final SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute(getVariableName(), items, ScriptContext.ENGINE_SCOPE);

        try {
            getScript().eval(context);
        } catch (final ScriptException e) {
            throw new StageProcessingException("unable to execute script", e);
        }
    }

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (script == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId() + ", script may not be null");
        }
    }
}
