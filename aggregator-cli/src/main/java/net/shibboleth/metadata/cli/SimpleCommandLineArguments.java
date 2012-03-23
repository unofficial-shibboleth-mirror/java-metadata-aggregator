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

/** Command line arguments for the {@link SimpleCommandLine} command line tool. */
public class SimpleCommandLineArguments {

    // Non-option arguments
    private String inFile;

    private String pipelineName;

    // Logging
    private boolean verbose;

    private CmdLineParser.Option VERBOSE_ARG;

    private boolean quiet;

    private CmdLineParser.Option QUIET_ARG;

    private String logConfig;

    private CmdLineParser.Option LOG_CONFIG_ARG;

    // Help
    private boolean help;

    private CmdLineParser.Option HELP_ARG;

    private CmdLineParser cliParser;

    public SimpleCommandLineArguments(String[] args) {
        cliParser = new CmdLineParser();

        VERBOSE_ARG = cliParser.addBooleanOption("verbose");
        QUIET_ARG = cliParser.addBooleanOption("quiet");
        LOG_CONFIG_ARG = cliParser.addStringOption("logConfig");
        HELP_ARG = cliParser.addBooleanOption("help");
    }

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

            verbose = (Boolean) cliParser.getOptionValue(VERBOSE_ARG, Boolean.FALSE);
            quiet = (Boolean) cliParser.getOptionValue(QUIET_ARG, Boolean.FALSE);
            logConfig = (String) cliParser.getOptionValue(LOG_CONFIG_ARG);
            help = (Boolean) cliParser.getOptionValue(HELP_ARG, false);
            validateCommandLineArguments();
        } catch (OptionException e) {
            errorAndExit(e.getMessage());
        }
    }

    public String getInputFile() {
        return inFile;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public boolean doVerboseOutput() {
        return verbose;
    }

    public boolean doQuietOutput() {
        return quiet;
    }

    public String getLoggingConfiguration() {
        return logConfig;
    }

    public boolean doHelp() {
        return help;
    }

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
        out.println(String.format("  --%-20s %s", HELP_ARG.longForm(), "Prints this help information"));
        out.println();

        out.println("Logging Options - these options are mutually exclusive");
        out.println(String.format("  --%-20s %s", VERBOSE_ARG.longForm(), "Turn on verbose messages."));
        out.println(String.format("  --%-20s %s", QUIET_ARG.longForm(),
                "Restrict output messages to errors and warnings."));
        out.println(String.format("  --%-20s %s", LOG_CONFIG_ARG.longForm(),
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