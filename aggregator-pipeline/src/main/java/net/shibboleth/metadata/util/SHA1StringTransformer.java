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

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.cryptacular.util.CodecUtil;
import org.cryptacular.util.HashUtil;

/**
 * A {@link Function} that transforms a {@link String} into a hex-encoded representation of
 * the SHA-1 digest of the string.
 *
 * @since 0.9.2
 */
@Immutable
public class SHA1StringTransformer implements Function<String, String> {

    @Override
    public String apply(@Nonnull final String input) {
        return CodecUtil.hex(HashUtil.sha1(input.getBytes()));
    }

}
