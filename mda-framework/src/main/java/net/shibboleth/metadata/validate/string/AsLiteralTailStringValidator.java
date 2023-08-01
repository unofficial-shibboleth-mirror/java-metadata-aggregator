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

package net.shibboleth.metadata.validate.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import net.shibboleth.metadata.validate.BaseAsValidator;
import net.shibboleth.metadata.validate.Validator;

/**
 * A <code>Validator</code> that assists in the validation of regular-expression &lt;shibmd:Scope&gt;
 * values that include a literal tail.
 *
 * <p>A literal tail is:</p>
 *
 * <ul>
 *    <li>a sequence of at least two domain labels</li>
 *    <li>separated by literal <code>'.'</code> characters (encoded
 *        in the regular expression as <code>'\.'</code></li>
 *    <li>explicitly anchored at the end of the regular expression</li>
 *    <li>preceded by anything terminating with an encoded literal <code>'.'</code>
 * </ul>
 *
 * <p>For example, the literal tail in the regular expression
 * <code>'^([a-zA-Z0-9-]{1,63}\.){0,2}ddd\.ccc\.bbb\.aa$'</code> is <code>'ccc.bbb.aa'</code>.</p>
 *
 * <p>The literal tail is extracted from the regular expression, has its encoded <code>'.'</code>
 * characters converted to normal ones, and then validated as a {@link String} by a new sequence of
 * validators.</p>
 *
 * <p>This validator fails (and returns {@link net.shibboleth.metadata.validate.Validator.Action#DONE}) if the
 * value does not possess a literal tail.</p>
 *
 * <p>Otherwise, the validator applies the sequence of validators to the new value and returns
 * the value of that sequence.</p>
 *
 * @since 0.10.0
 */
public class AsLiteralTailStringValidator extends BaseAsValidator<String, String>
    implements Validator<String> {

    /**
     * Regular expression to match and extract the literal tail.
     *
     * <p>The component parts of this expression are:</p>
     *
     * <ul>
     *    <li>implicitly, anchored at the start of the string being matched</li>
     *    <li><code>.*?</code> matches non-greedily any text at the start of the regular expression,
     *       maximising the size of the later parts of the match</li>
     *    <li><code>\\\\.</code> matches <code>\.</code> in the regular expression, which in turn
     *       matches a literal <code>.</code> in the scope</li>
     *    <li>A group containing:
     *        <ul>
     *            <li>At least one DNS label terminated by a literal '.'</li>
     *            <li>A final DNS label</li>
     *        </ul>
     *    </li>
     *    <li><code>\\$</code> matches an end-string marker in the regular expression being matched</li>
     *    <li>implicitly, anchored at the end of the string being matched</li>
     * </ul>
     *
     * <p>The matching of DNS labels is not exact. For example, labels starting or ending with hyphens
     * are accepted as part of a literal tail. This will normally be detected by the nested validator
     * sequence applied to the result.</p>
     *
     * <p>Similarly, upper-case characters are permitted in the literal tail although these would
     * not normally be permitted in scopes. Again, these characters are permitted so that a more
     * specific error can be reported, rather than just a generic failure to convert.</p>
     */
    private final Pattern pattern = Pattern.compile(".*?\\\\.(([a-zA-Z0-9-]+\\\\.)+[a-zA-Z0-9-]+)\\$");

    @Override
    protected @Nonnull String convert(@Nonnull final String regex) throws IllegalArgumentException {
        // Match against the regular expression
        final Matcher matcher = pattern.matcher(regex);

        // If the pattern does not match, signal that the string does not have a literal tail
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }

        // Remove all '\' characters from the result.
        final var result = matcher.group(1).replaceAll("\\\\", "");
        assert result != null;
        return result;
    }

}
