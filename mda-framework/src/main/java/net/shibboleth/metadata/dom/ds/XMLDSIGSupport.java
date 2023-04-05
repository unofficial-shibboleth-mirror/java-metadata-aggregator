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

package net.shibboleth.metadata.dom.ds;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.namespace.QName;

/**
 * Support class for dealing with the XML DSIG specification.
 *
 * @since 0.9.0
 */
@ThreadSafe
public final class XMLDSIGSupport {

    /** Signature element name. */
    public static final @Nonnull QName SIGNATURE_NAME = new QName(XMLSignature.XMLNS, "Signature");

    /** Constructor. */
    private XMLDSIGSupport() {
    }

}
