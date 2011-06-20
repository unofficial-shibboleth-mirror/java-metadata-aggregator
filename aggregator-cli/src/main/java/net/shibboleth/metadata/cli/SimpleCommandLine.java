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

import java.util.ArrayList;

import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.Pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * A simple driver for the metadata aggregator.
 * 
 * This class takes two parameters, the first is the file: URI to the Spring configuration file. The second parameter is
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
public class SimpleCommandLine {

    /**
     * Main method.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("This command line only supports two arguments, "
                    + "the file: URI path to the Spring configuration file describing the processing pipeline "
                    + "and bean ID of the Pipeline to execute.");
            System.exit(1);
        }

        Logger log = LoggerFactory.getLogger(SimpleCommandLine.class);

        FileSystemXmlApplicationContext appCtx = null;
        try {
            log.debug("Initializing Spring context with configuration file {}", args[0]);
            appCtx = new FileSystemXmlApplicationContext(args[0]);
        } catch (BeansException e) {
            log.error("Unable to initialize Spring context", e);
            System.exit(1);
        }

        log.debug("Retreiving pipeline from Spring context");
        Pipeline pipeline = appCtx.getBean(args[1], Pipeline.class);
        if (pipeline == null) {
            log.error("No net.shibboleth.metadata.pipeline.Pipeline, with ID {}, defined in Spring configuration",
                    args[1]);
            System.exit(1);
        }

        try {
            if (!pipeline.isInitialized()) {
                log.debug("Retrieved pipeline has not been initialized, initializing it now");
                pipeline.initialize();
            } else {
                log.debug("Retrieved pipeline has already been initialized");
            }

            log.debug("Executing pipeline");
            ArrayList<DomElementItem> item = new ArrayList<DomElementItem>();
            pipeline.execute(item);
            log.debug("Pipeline execution complete");

            System.exit(0);
        } catch (Exception e) {
            log.error("Error processing information", e);
            System.exit(1);
        }
    }
}