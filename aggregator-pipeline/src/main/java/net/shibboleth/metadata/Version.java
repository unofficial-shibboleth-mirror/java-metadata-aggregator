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

package net.shibboleth.metadata;

import javax.annotation.Nonnull;

/** Class for getting and printing the version of the metadata pipeline. */
public final class Version {

    /** metadata pipeline version. */
    private static final String VERSION;

    /** metadata pipeline major version number. */
    private static final int MAJOR_VERSION;

    /** metadata pipeline minor version number. */
    private static final int MINOR_VERSION;

    /** metadata pipeline micro version number. */
    private static final int MICRO_VERSION;

    /** Constructor. */
    private Version() {
    }

    /**
     * Main entry point to program.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Package pkg = Version.class.getPackage();
        System.out.println(pkg.getImplementationTitle() + " version " + VERSION);
    }

    /**
     * Gets the version of the metadata pipeline.
     * 
     * @return version of the metadata pipeline
     */
    @Nonnull public static String getVersion() {
        return VERSION;
    }

    /**
     * Gets the major version number of the metadata pipeline.
     * 
     * @return major version number of the metadata pipeline
     */
    public static int getMajorVersion() {
        return MAJOR_VERSION;
    }

    /**
     * Gets the minor version number of the metadata pipeline.
     * 
     * @return minor version number of the metadata pipeline
     */
    public static int getMinorVersion() {
        return MINOR_VERSION;
    }

    /**
     * Gets the micro version number of the metadata pipeline.
     * 
     * @return micro version number of the metadata pipeline
     */
    public static int getMicroVersion() {
        return MICRO_VERSION;
    }

    static {
        VERSION = Version.class.getPackage().getImplementationVersion();
        String[] versionParts = VERSION.split(".");
        MAJOR_VERSION = Integer.parseInt(versionParts[0]);
        MINOR_VERSION = Integer.parseInt(versionParts[1]);
        MICRO_VERSION = Integer.parseInt(versionParts[2]);
    }
}