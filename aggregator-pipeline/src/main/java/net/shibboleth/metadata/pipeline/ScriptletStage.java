/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.pipeline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.MetadataCollection;

import org.opensaml.util.StringSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** A pipeline stage that computes that transforms the collection of metadata via a script. */
@ThreadSafe
public class ScriptletStage extends AbstractComponent implements Stage<Metadata<?>> {

    /** Name of the scriptlet attribute, {@value} , containing the metadata collection to be transformed. */
    public static final String METADATA = "metadata";

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ScriptletStage.class);

    /** Name of the scripting language in use. */
    private String scriptLanguage = "ecmascript";

    /** Filesystem path script file. */
    private File scriptFile;

    /** The script engine to execute the script. */
    private ScriptEngine scriptEngine;

    /** The compiled form of the script, if the script engine supports compiling. */
    private CompiledScript compiledScript;

    /**
     * Gets the scripting language used.
     * 
     * @return scripting language used
     */
    public String getScriptLanguage() {
        return scriptLanguage;
    }

    /**
     * Sets the scripting language used.
     * 
     * @param language scripting language used
     */
    public synchronized void setScriptLanguage(final String language) {
        if (isInitialized()) {
            return;
        }
        scriptLanguage = StringSupport.trimOrNull(language);
    }

    /**
     * Gets the script file used.
     * 
     * @return script file used
     */
    public File getScriptFile() {
        return scriptFile;
    }

    /**
     * Sets the script file used.
     * 
     * @param file script file used
     */
    public synchronized void setScriptFile(final File file) {
        if (isInitialized()) {
            return;
        }
        scriptFile = file;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public MetadataCollection<Metadata<?>> execute(final MetadataCollection<Metadata<?>> metadataCollection)
            throws StageProcessingException {

        final Bindings bindings = scriptEngine.createBindings();
        bindings.put(METADATA, metadataCollection);

        try {
            if (compiledScript != null) {
                compiledScript.eval(bindings);
            } else {
                scriptEngine.eval(new FileReader(scriptFile), bindings);
            }

            return (MetadataCollection<Metadata<?>>) bindings.get(METADATA);
        } catch (ScriptException e) {
            String errMsg = getId() + " pipeline stage unable to execut script";
            log.error(errMsg, e);
            throw new StageProcessingException(errMsg, e);
        } catch (FileNotFoundException e) {
            String errMsg = getId() + " pipeline stage unable to read script file " + scriptFile.getPath();
            log.error(errMsg, e);
            throw new StageProcessingException(errMsg, e);
        }
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        if (scriptLanguage == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", ScriptLanguage may not be null");
        }

        if (scriptFile == null) {
            throw new ComponentInitializationException("Unable to initialize " + getId() + ", Source may not be null");
        }

        if (!scriptFile.exists() || !scriptFile.canRead()) {
            throw new ComponentInitializationException("Unable to initialize " + getId() + ", source file/directory "
                    + scriptFile.getPath() + " can not be read");
        }

        ScriptEngineManager sem = new ScriptEngineManager();
        scriptEngine = sem.getEngineByName(scriptLanguage);

        try {
            if (scriptEngine != null && scriptEngine instanceof Compilable) {
                compiledScript = ((Compilable) scriptEngine).compile(new FileReader(scriptFile));
            }
        } catch (ScriptException e) {
            throw new ComponentInitializationException(
                    "Unable to initialize " + getId() + ", unable to compile script", e);
        } catch (IOException e) {
            throw new ComponentInitializationException("Unable to initialize " + getId()
                    + ", unable to read script file", e);
        }
    }
}