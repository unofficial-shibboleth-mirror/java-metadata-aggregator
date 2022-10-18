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

package net.shibboleth.metadata.pipeline;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import javax.annotation.concurrent.ThreadSafe;

import org.bouncycastle.crypto.digests.MD5Digest;
import org.cryptacular.util.CodecUtil;
import org.cryptacular.util.HashUtil;

/**
 * Transforms a string into another string that is the MD5 hash of the UTF-8 encoding
 * of the original string, prepended with "{md5}".
 *
 * @since 0.9.0
 */
@ThreadSafe
public class MDQueryMD5ItemIdTransformer implements Function<String, String> {

    @Override
    public String apply(final String source) {
        return "{md5}" + CodecUtil.hex(HashUtil.hash(new MD5Digest(),
                source.getBytes(StandardCharsets.UTF_8)));
    }
}
