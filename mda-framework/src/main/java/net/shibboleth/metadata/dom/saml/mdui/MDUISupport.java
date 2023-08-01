/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.metadata.dom.saml.mdui;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

/** Support class for dealing with MDUI metadata. */
@ThreadSafe
public final class MDUISupport {

    /** MDUI namespace. */
    public static final @Nonnull String MDUI_NS = "urn:oasis:names:tc:SAML:metadata:ui";

    /** {@link QName} representing an <code>mdui:UIInfo</code>. */
    public static final @Nonnull QName UIINFO_NAME = new QName(MDUI_NS, "UIInfo");
    
    /** {@link QName} representing an <code>mdui:DisplayName</code>. */
    public static final @Nonnull QName DISPLAYNAME_NAME = new QName(MDUI_NS, "DisplayName");
    
    /** {@link QName} representing an <code>mdui:Description</code>. */
    public static final @Nonnull QName DESCRIPTION_NAME = new QName(MDUI_NS, "Description");
    
    /** {@link QName} representing an <code>mdui:Keywords</code>. */
    public static final @Nonnull QName KEYWORDS_NAME = new QName(MDUI_NS, "Keywords");
    
    /** {@link QName} representing an <code>mdui:Logo</code>. */
    public static final @Nonnull QName LOGO_NAME = new QName(MDUI_NS, "Logo");
    
    /** {@link QName} representing an <code>mdui:InformationURL</code>. */
    public static final @Nonnull QName INFORMATIONURL_NAME = new QName(MDUI_NS, "InformationURL");
    
    /** {@link QName} representing an <code>mdui:PrivacyStatementURL</code>. */
    public static final @Nonnull QName PRIVACYSTATEMENTURL_NAME = new QName(MDUI_NS, "PrivacyStatementURL");

    /**
     * {@link QName} representing an <code>mdui:IPHint</code>.
     *
     * @since 0.10.0
     */
    public static final @Nonnull QName IPHINT_NAME = new QName(MDUI_NS, "IPHint");

    /** Constructor. */
    private MDUISupport() {
    }

}
