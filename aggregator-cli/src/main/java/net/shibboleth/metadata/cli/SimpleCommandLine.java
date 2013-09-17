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

package net.shibboleth.metadata.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.Pipeline;
import net.shibboleth.metadata.pipeline.TerminationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * A simple driver for the metadata aggregator.
 * 
 * This class takes two parameters, the first is the file path to the Spring configuration file. The second parameter is
 * the name of bean ID of the Pipeline to be executed. If the pipeline is not initialized by Spring it will be
 * initialized by this CLI.
 * 
 * All logging is done in accordance with the logback.xml file included in command line JAR file. If you wish to use a
 * different logging configuration you may do so using the <code>-Dlogback.configurationFile=/path/to/logback.xml</code>
 * JVM configuration option.
 * 
 * This CLI is not terribly robust nor does it really offer much in the way of features. It's mostly meant for testing
 * purposes and will be replaced before the 1.0 release of the software.
 */
public final class SimpleCommandLine {

    /** Return code indicating command completed successfully, {@value} . */
    public static final int RC_OK = 0;

    /** Return code indicating an initialization error, {@value} . */
    public static final int RC_INIT = 1;

    /** Return code indicating an error reading files, {@value} . */
    public static final int RC_IO = 2;

    /** Return code indicating an unknown error occurred, {@value} . */
    public static final int RC_UNKNOWN = -1;

    /** Class logger. */
    private static Logger log;

    /** Constructor. */
    private SimpleCommandLine() {
        
    }
    
    /**
     * Main method.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SimpleCommandLineArguments cli = new SimpleCommandLineArguments();
        cli.parseCommandLineArguments(args);

        if (cli.doHelp()) {
            cli.printHelp(System.out);
            System.exit(RC_OK);
        }
        
        initLogging(cli);

        FileSystemXmlApplicationContext appCtx = null;
        try {
            String fileUri = new File(cli.getInputFile()).toURI().toString();
            log.debug("Initializing Spring context with configuration file {}", fileUri);
            appCtx = new FileSystemXmlApplicationContext(fileUri);
        } catch (BeansException e) {
            log.error("Unable to initialize Spring context", e);
            System.exit(RC_INIT);
        }

        log.debug("Retreiving pipeline from Spring context");
        String pipelineName = cli.getPipelineName();
        Pipeline pipeline = appCtx.getBean(pipelineName, Pipeline.class);
        if (pipeline == null) {
            log.error("No net.shibboleth.metadata.pipeline.Pipeline, with ID {}, defined in Spring configuration",
                    pipelineName);
            System.exit(RC_INIT);
        }

        try {
            if (!pipeline.isInitialized()) {
                log.debug("Retrieved pipeline has not been initialized, initializing it now");
                pipeline.initialize();
            } else {
                log.debug("Retrieved pipeline has already been initialized");
            }

            ArrayList<DomElementItem> item = new ArrayList<DomElementItem>();
            Date startTime = new Date();
            log.info("Pipeline '{}' execution starting at {}", pipelineName, startTime);
            pipeline.execute(item);
            Date endTime = new Date();
            log.info("Pipeline '{}' execution completed at {}; run time {} seconds",
                    new Object[]{pipelineName, endTime, (endTime.getTime()-startTime.getTime())/1000f});

            System.exit(RC_OK);
            
        } catch (TerminationException e) {
            if (cli.doVerboseOutput()) {
                log.error("TerminationException during processing", e);
            } else {
                log.error("Terminated: {}", e.getMessage());
            }
            System.exit(RC_INIT);
            
        } catch (Exception e) {
            log.error("Error processing information", e);
            System.exit(RC_INIT);
        }
    }

    /**
     * Initialize the logging subsystem.
     * 
     * @param cli command line arguments
     */
    protected static void initLogging(SimpleCommandLineArguments cli) {
        if (cli.getLoggingConfiguration() != null) {
            System.setProperty("logback.configurationFile", cli.getLoggingConfiguration());
        } else if (cli.doVerboseOutput()) {
            System.setProperty("logback.configurationFile", "logger-verbose.xml");
        } else if (cli.doQuietOutput()) {
            System.setProperty("logback.configurationFile", "logger-quiet.xml");
        } else {
            System.setProperty("logback.configurationFile", "logger-normal.xml");
        }

        log = LoggerFactory.getLogger(SimpleCommandLine.class);
    }
}