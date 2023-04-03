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

import javax.annotation.Nonnull;

import net.shibboleth.metadata.Version;
import net.shibboleth.metadata.pipeline.Pipeline;
import net.shibboleth.metadata.pipeline.TerminationException;
import net.shibboleth.shared.primitive.LoggerFactory;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * A simple driver for the metadata aggregator.
 *
 * <p>
 * This class takes two parameters, the first is the file path to the Spring configuration file. The second parameter is
 * the name of bean ID of the Pipeline to be executed. If the pipeline is not initialized by Spring it will be
 * initialized by this CLI.
 * </p>
 * 
 * <p>
 * Logging is configured through the <code>logback.configurationFile</code> system property.
 * This property is set here depending on the command-line options selected. A value set
 * outside this code will be overwritten and therefore ignored.
 * </p>
 *
 * <p>
 * Because logback only looks at the <code>logback.configurationFile</code> once,
 * on the first call to <code>getLogger()</code>, it is important that this code
 * and anything referenced by it does <em>not</em> define static loggers, as this
 * may cause premature initialisation with the default settings.
 * </p>
 */
public final class SimpleCommandLine {

    /**
     * Exception to be thrown during processing. Carries an error
     * code to be reported as the CLI result.
     */
    private static class ErrorException extends Exception {
        /** serialVersionUID required for all exceptions. */
        private static final long serialVersionUID = 1L;

        /** CLI error code to return on exiting the JVM. */
        private final int error;

        /**
         * Constructor.
         *
         * @param errorCode error code to report on exit
         * @param message message to report on exit
         * @param cause underlying cause of the error
         */
        ErrorException(final int errorCode, @Nonnull final String message, final Throwable cause) {
            super(message, cause);
            error = errorCode;
        }

        /**
         * Constructor.
         *
         * @param errorCode error code to report on exit
         * @param message message to report on exit
         */
        ErrorException(final int errorCode, @Nonnull final String message) {
            super(message);
            error = errorCode;
        }
    }

    /** Return code indicating an initialization error, {@value} . */
    public static final int RC_INIT = 1;

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
    public static void main(final String[] args) {
        final SimpleCommandLineArguments cli = new SimpleCommandLineArguments();
        cli.parseCommandLineArguments(args);

        if (cli.doHelp()) {
            cli.printHelp(System.out);
            return;
        }

        if (cli.doVersion()) {
            System.out.println(Version.getVersion());
            return;
        }
        
        initLogging(cli);

        try {
            process(cli);
        } catch (final ErrorException e) {
            log.error(e.getMessage(), e.getCause());
            System.exit(e.error);
        }
    }

    /**
     * Build the context and run the pipeline.
     *
     * @param cli command line arguments
     *
     * @throws ErrorException if a problem requiring termination occurs
     */
    private static void process(@Nonnull final SimpleCommandLineArguments cli) throws ErrorException {
        final String fileUri = new File(cli.getInputFile()).toURI().toString();
        log.debug("Initializing Spring context with configuration file {}", fileUri);
        try (FileSystemXmlApplicationContext appCtx = new FileSystemXmlApplicationContext(fileUri)) {

            log.debug("Retrieving pipeline from Spring context");
            final String pipelineName = cli.getPipelineName();
            final Pipeline<?> pipeline = appCtx.getBean(pipelineName, Pipeline.class);

            try {
                if (!pipeline.isInitialized()) {
                    log.debug("Retrieved pipeline has not been initialized, initializing it now");
                    pipeline.initialize();
                } else {
                    log.debug("Retrieved pipeline has already been initialized");
                }

                final Date startTime = new Date();
                log.info("Pipeline '{}' execution starting at {}", pipelineName, startTime);
                pipeline.execute(new ArrayList<>());
                final Date endTime = new Date();
                log.info("Pipeline '{}' execution completed at {}; run time {} seconds",
                        pipelineName, endTime, (endTime.getTime()-startTime.getTime())/1000f);

            } catch (final TerminationException e) {
                if (cli.doVerboseOutput()) {
                    throw new ErrorException(RC_INIT, "TerminationException during processing", e);
                } else {
                    throw new ErrorException(RC_INIT, "Terminated: " + e.getMessage());
                }

            } catch (final Exception e) {
                throw new ErrorException(RC_INIT, "Error processing information", e);
            }

        } catch (final BeansException e) {
            throw new ErrorException(RC_INIT, "Unable to initialize Spring context", e);
        }
    }

    /**
     * Set the logback configuration to a specific location.
     * 
     * <p>
     * Note that this <strong>must</strong> be done before the
     * first logger is retrieved.
     * </p>
     *
     * @param value logback configuration location to set
     */
    private static void setLoggingProperty(@Nonnull final String value) {
        System.setProperty("logback.configurationFile", value);
    }
    
    /**
     * Set the logback configuration to a specific package-local resource.
     *
     * @param value name of resource to use as the logback configuration file
     */
    private static void setLoggingToLocalResource(@Nonnull final String value) {
        setLoggingProperty("net/shibboleth/metadata/cli/" + value);
    }

    /**
     * Initialize the logging subsystem.
     *
     * <p>
     * Because this sets the system property logback uses for configuration, it must
     * be called <em>before</em> any calls to {@link LoggerFactory#getLogger}.
     * </p>
     *
     * @param cli command line arguments
     */
    protected static void initLogging(final SimpleCommandLineArguments cli) {
        final var config = cli.getLoggingConfiguration();
        if (config != null) {
            setLoggingProperty(config);
        } else if (cli.doVerboseOutput()) {
            setLoggingToLocalResource("logger-verbose.xml");
        } else if (cli.doQuietOutput()) {
            setLoggingToLocalResource("logger-quiet.xml");
        } else {
            setLoggingToLocalResource("logger-normal.xml");
        }

        log = LoggerFactory.getLogger(SimpleCommandLine.class);
    }
}
