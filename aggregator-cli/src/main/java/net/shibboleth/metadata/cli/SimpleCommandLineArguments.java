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

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.OptionException;

import java.io.PrintStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Command line arguments for the {@link SimpleCommandLine} command line tool. */
public class SimpleCommandLineArguments {

    // Non-option arguments
    
    /**
     * Provided input file name for the Spring configuration file.
     */
    @Nullable private String inFile;

    /**
     * Provided name for the pipeline bean to execute.
     */
    @Nullable private String pipelineName;

    // Logging
    
    /**
     * Verbose logging has been requested.
     */
    private boolean verbose;

    /**
     * Option object for the <code>--verbose</code> option.
     */
    @Nonnull private final CmdLineParser.Option verboseOption;

    /**
     * Quiet logging has been requested.
     */
    private boolean quiet;

    /**
     * Option object for the <code>--quiet</code> option.
     */
    @Nonnull private final CmdLineParser.Option quietOption;

    /**
     * Name of a specific logging configuration, if one has been requested.
     */
    @Nullable private String logConfig;

    /**
     * Option object for the <code>--logConfig</code> option.
     */
    @Nonnull private final CmdLineParser.Option logConfigOption;

    // Help
    
    /**
     * Help has been requested.
     */
    private boolean help;

    /**
     * Option object for the <code>--help</code> option.
     */
    @Nonnull private final CmdLineParser.Option helpOption;

    /**
     * The configured command line parser.
     */
    @Nonnull private final CmdLineParser cliParser;

    /**
     * Constructor.
     */
    public SimpleCommandLineArguments() {
        cliParser = new CmdLineParser();

        verboseOption = cliParser.addBooleanOption("verbose");
        quietOption = cliParser.addBooleanOption("quiet");
        logConfigOption = cliParser.addStringOption("logConfig");
        helpOption = cliParser.addBooleanOption("help");
    }

    /**
     * Parse an array of command-line arguments as passed to the main program.
     *
     * @param args  array of command-line arguments to parse.
     */
    public void parseCommandLineArguments(String[] args) {
        try {
            cliParser.parse(args);
            
            String[] otherArgs = cliParser.getRemainingArgs();
            if (otherArgs.length != 2) {
                printHelp(System.out);
                System.out.flush();
                System.exit(SimpleCommandLine.RC_INIT);
            }
            inFile = otherArgs[0];
            pipelineName = otherArgs[1];

            verbose = (Boolean) cliParser.getOptionValue(verboseOption, Boolean.FALSE);
            quiet = (Boolean) cliParser.getOptionValue(quietOption, Boolean.FALSE);
            logConfig = (String) cliParser.getOptionValue(logConfigOption);
            help = (Boolean) cliParser.getOptionValue(helpOption, false);
            validateCommandLineArguments();
        } catch (OptionException e) {
            errorAndExit(e.getMessage());
        }
    }

    /**
     * Gets the parsed input file name from the command line.
     * 
     * @return the input file name argument
     */
    @Nullable public String getInputFile() {
        return inFile;
    }

    /**
     * Returns the name of the pipeline bean to execute from the command line.
     * 
     * @return the pipeline bean name argument
     */
    @Nullable public String getPipelineName() {
        return pipelineName;
    }

    /**
     * Indicates the presence of the <code>--verbose</code> option.
     * 
     * @return <code>true</code> if the user requested verbose logging.
     */
    public boolean doVerboseOutput() {
        return verbose;
    }

    /**
     * Indicates the presence of the <code>--quiet</code> option.
     * 
     * @return <code>true</code> if the user requested quiet logging.
     */
    public boolean doQuietOutput() {
        return quiet;
    }

    /**
     * Gets the name of the requested logging configuration file
     * from the command line.
     * 
     * @return the logging configuration file name, or <code>null</code>.
     */
    @Nullable public String getLoggingConfiguration() {
        return logConfig;
    }

    /**
     * Indicates the presence of the <code>--help</code> option.
     * 
     * @return <code>true</code> if the user requested help.
     */
    public boolean doHelp() {
        return help;
    }

    /**
     * Validate the provided command line arguments, for example issuing
     * an error if they are inconsistent.
     */
    private void validateCommandLineArguments() {
        if (doHelp()) {
            return;
        }

        if (doVerboseOutput() && doQuietOutput()) {
            errorAndExit("Verbose and quiet output are mutually exclusive");
        }
    }

    /**
     * Print command line help instructions.
     * 
     * @param out location where to print the output
     */
    public void printHelp(PrintStream out) {
        out.println("SimpleCommandLine");
        out.println("Provides a command line interface for the metadata aggregator.");
        out.println();
        out.println("   SimpleCommandLine [options] springConfiguration pipelineName");
        out.println();
        out.println("      springConfiguration      name of Spring configuration to use");
        out.println("      pipelineName             name of pipeline to invoke");
        out.println();
        out.println("==== Command Line Options ====");
        out.println();
        out.println(String.format("  --%-20s %s", helpOption.longForm(), "Prints this help information"));
        out.println();

        out.println("Logging Options - these options are mutually exclusive");
        out.println(String.format("  --%-20s %s", verboseOption.longForm(), "Turn on verbose messages."));
        out.println(String.format("  --%-20s %s", quietOption.longForm(),
                "Restrict output messages to errors and warnings."));
        out.println(String.format("  --%-20s %s", logConfigOption.longForm(),
                "Specifies a logback configuration file to use to configure logging."));
        out.println();
    }

    /**
     * Prints the error message to STDERR and then exits.
     * 
     * @param error the error message
     */
    private void errorAndExit(String error) {
        System.err.println(error);
        System.err.flush();
        System.out.println();
        printHelp(System.out);
        System.out.flush();
        System.exit(SimpleCommandLine.RC_INIT);
    }
}
