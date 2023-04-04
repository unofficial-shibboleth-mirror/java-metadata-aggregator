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

package net.shibboleth.metadata.util;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import net.shibboleth.shared.annotation.constraint.NotEmpty;
import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.primitive.StringSupport;

/**
 * A {@link FileFilter} implementation that selects files on the basis of a regular expression match against the file
 * name.
 *
 * @since 0.10.0
 */
@Immutable
public class RegexFileFilter implements FileFilter {

    /**
     * Compiled regular expression.
     */
    private final Pattern pattern;

    /**
     * Constructor.
     *
     * @param regex Regular expression to match file names against.
     */
    public RegexFileFilter(@Nonnull @NotEmpty final String regex) {
        pattern = Pattern.compile(Constraint.isNotNull(StringSupport.trimOrNull(regex),
                "regex pattern argument may not be null or empty"));
    }

    /**
     * Matches the regular expression against the last file name component of the supplied {@link File} object. As the
     * whole of the name is being matched against, it is not necessary for the regular expression to contain anchor
     * characters such as '^' and '$'.
     *
     * @param pathname {@link File} to match against the regular expression.
     *
     * @return <code>true</code> iff <code>pathname</code> matches the regular expression.
     */
    @Override
    public boolean accept(final File pathname) {
        return pattern.matcher(pathname.getName()).matches();
    }

}
