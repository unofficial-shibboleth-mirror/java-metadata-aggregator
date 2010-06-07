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

package edu.internet2.middleware.shibboleth.metadata.core.pipeline.stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.opensaml.util.Assert;
import org.opensaml.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import edu.internet2.middleware.shibboleth.metadata.core.MetadataElement;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataElementCollection;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.AbstractComponent;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.PipelineInitializationException;

/** A pipeline stage that computes that transforms the collection of metadata via a script. */
public class ScriptletStage extends AbstractComponent implements Stage<MetadataElement<?>> {

    /** Name of the scriptlet attribute, {@value} , containing the metadata collection to be transformed. */
    public static final String METADATA = "metadata";

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(ScriptletStage.class);

    /** Name of the scripting language in use. */
    private String scriptLanguage;

    /** Filesystem path script file. */
    private File scriptFile;

    /** The script engine to execute the script. */
    private ScriptEngine scriptEngine;

    /** The compiled form of the script, if the script engine supports compiling. */
    private CompiledScript compiledScript;

    /**
     * Constructor.
     * 
     * @param id ID of this stage
     * @param lang name of the script language engine
     * @param path file system path to the script to be executed
     */
    public ScriptletStage(String id, String lang, String path) {
        super(id);

        scriptLanguage = Strings.trimOrNull(lang);
        Assert.isNull(scriptLanguage, "Scripting language may not be null or empty");

        scriptFile = new File(path);
        Assert.isTrue(scriptFile.exists(), "Script file " + path + " does not exist");
        Assert.isTrue(scriptFile.canRead(), "Script file " + path + " is not readbale");
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws PipelineInitializationException {
        ScriptEngineManager sem = new ScriptEngineManager();
        scriptEngine = sem.getEngineByName(scriptLanguage);

        try {
            if (scriptEngine != null && scriptEngine instanceof Compilable) {
                compiledScript = ((Compilable) scriptEngine).compile(new FileReader(scriptFile));
            }
        } catch (ScriptException e) {
            String errMsg = MessageFormatter.format(
                    "{} unable to compile even though the scripting engine supports this functionality.", getId());
            log.error(errMsg, e);
            throw new PipelineInitializationException(errMsg, e);
        } catch (IOException e) {
            String errMsg = MessageFormatter.format("{} unable to read script file {}", getId(), scriptFile.getPath());
            log.error(errMsg, e);
            throw new PipelineInitializationException(errMsg, e);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public MetadataElementCollection<MetadataElement<?>> execute(Map<String, Object> parameters,
            MetadataElementCollection<MetadataElement<?>> metadataCollection) throws PipelineStageException {

        Bindings bindings = scriptEngine.createBindings();
        if (parameters != null) {
            bindings.putAll(parameters);
        }
        bindings.put(METADATA, metadataCollection);

        try {
            if (compiledScript != null) {
                compiledScript.eval(bindings);
            } else {
                scriptEngine.eval(new FileReader(scriptFile), bindings);
            }

            return (MetadataElementCollection<MetadataElement<?>>) bindings.get(METADATA);
        } catch (ScriptException e) {
            String errMsg = MessageFormatter.format("{} pipeline stage unable to execut script", getId());
            log.error(errMsg, e);
            throw new PipelineStageException(errMsg, e);
        } catch (FileNotFoundException e) {
            String errMsg = MessageFormatter.format("{} pipeline stage unable to read script file {}", getId(),
                    scriptFile.getPath());
            log.error(errMsg, e);
            throw new PipelineStageException(errMsg, e);
        }
    }
}