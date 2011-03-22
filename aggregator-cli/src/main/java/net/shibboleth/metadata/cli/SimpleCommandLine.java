/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
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

package net.shibboleth.metadata.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.MetadataSerializer;
import net.shibboleth.metadata.pipeline.Pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * A simple driver for the metadata aggregator.
 * 
 * This class takes two parameters, the first is the file: URI to the Spring configuration file. The second parameter is
 * the file: URI to the file to which the results will be serialized. The Spring configuration file must define one, and
 * only one, {@link Pipeline} and {@link MetadataSerializer}. If the pipeline is not initialized by Spring it will be
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
                    + "the file: URI path to the Spring configuration file describing the metadata pipeline "
                    + "and the file: URI path to which the resultant metadata will be written.");
            System.exit(1);
        }

        Logger log = LoggerFactory.getLogger(SimpleCommandLine.class);

        try {
            log.debug("Initializing Spring context with configuration file {}", args[0]);
            FileSystemXmlApplicationContext appCtx = new FileSystemXmlApplicationContext(args[0]);

            log.debug("Retreiving pipeline from Spring context");
            Pipeline pipeline = appCtx.getBean(Pipeline.class);
            if (pipeline == null) {
                log.error("No net.shibboleth.metadata.pipeline.Pipeline defined in Spring configuration");
                System.exit(1);
            }

            if (!pipeline.isInitialized()) {
                log.debug("Retrieved pipeline has not been initialized, initializing it now");
                pipeline.initialize();
            } else {
                log.debug("Retrieved pipeline has already been initialized");
            }

            log.debug("Executing pipeline");
            MetadataCollection metadata = pipeline.execute();

            log.debug("Retrieving metadata serialized from Spring context");
            MetadataSerializer serializer = appCtx.getBean(MetadataSerializer.class);
            if (serializer == null) {
                log.error("No net.shibboleth.metadata.MetadataSerializer defined in Spring configuration");
                System.exit(1);
            }

            log.debug("Serializing metadata out to {}", args[1]);
            serializer.serialize(metadata, new FileOutputStream(new File(new URI(args[1]))));
            log.debug("Serialization complete.");

            System.exit(0);
        } catch (Exception e) {
            log.error("Error processing metadata", e);
            System.exit(1);
        }
    }
}