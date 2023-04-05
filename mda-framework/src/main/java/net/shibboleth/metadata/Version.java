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

/**
 * Class for getting and printing the version of the metadata pipeline.
 *
 * @since 0.5
 */
public final class Version {

    /** Metadata pipeline version. */
    private static final @Nonnull String VERSION;

    /** Metadata pipeline major version number. */
    private static final int MAJOR_VERSION;

    /** Metadata pipeline minor version number. */
    private static final int MINOR_VERSION;

    /** Metadata pipeline patch version number. */
    private static final int PATCH_VERSION;

    /** Constructor. */
    private Version() {
    }

    /**
     * Main entry point to program.
     * 
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        final Package pkg = Version.class.getPackage();
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
     * Gets the patch version number of the metadata pipeline.
     * 
     * @return patch version number of the metadata pipeline
     */
    public static int getPatchVersion() {
        return PATCH_VERSION;
    }

    static {
        final String version = Version.class.getPackage().getImplementationVersion();

        if (version != null) {
            // If we're running from a package with metadata (i.e., from a .jar)
            VERSION = version;
    
            // Semantic versioning: three dot-separated numbers, followed by extensions
            // separated by '-' and '+'.
            final String[] versionParts = VERSION.split("[\\.\\+\\-]");
    
            MAJOR_VERSION = Integer.parseInt(versionParts[0]);
            MINOR_VERSION = Integer.parseInt(versionParts[1]);
            PATCH_VERSION = Integer.parseInt(versionParts[2]);
        } else {
            // We don't have the package metadata available; probably a test environment
            VERSION = "unknown";
            MAJOR_VERSION = 0;
            MINOR_VERSION = 0;
            PATCH_VERSION = 0;
        }
    }
}
