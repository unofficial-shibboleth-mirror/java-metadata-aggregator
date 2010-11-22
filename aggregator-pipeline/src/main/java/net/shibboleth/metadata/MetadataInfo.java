/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
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

package net.shibboleth.metadata;

import java.io.Serializable;

import net.jcip.annotations.ThreadSafe;

/**
 * Processing information about a given {@link Metadata}. To overload the term, this is metadata about the
 * metadata element.
 * 
 * Implementations of this class <strong>MUST</strong> be immutable. When an {@link Metadata} is cloned, the
 * clone will reference the same {@link MetadataInfo} objects as the original.
 */
@ThreadSafe
public interface MetadataInfo extends Serializable {

}