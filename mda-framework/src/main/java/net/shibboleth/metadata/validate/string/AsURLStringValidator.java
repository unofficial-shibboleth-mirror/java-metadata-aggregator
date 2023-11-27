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

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nonnull;

import net.shibboleth.metadata.validate.BaseAsValidator;
import net.shibboleth.metadata.validate.Validator;

/**
 * A <code>Validator</code> that checks {@link String} values as URLs by converting the
 * value to an {@link URL} and applying a sequence of validators to that value.
 *
 * <p>
 * This validator fails (and returns {@link net.shibboleth.metadata.validate.Validator.Action#DONE}) if the
 * value can not be converted to a {@link URL}.
 * </p>
 *
 * <p>
 * Otherwise, the validator applies the sequence of validators to the {@link URL} and returns
 * the value of that sequence.
 * </p>
 *
 * @since 0.10.0
 */
public class AsURLStringValidator extends BaseAsValidator<String, URL>
    implements Validator<String> {

    @Override
    protected @Nonnull URL convert(@Nonnull final String value) throws IllegalArgumentException {
        try {
            final var result = new URL(value);
            assert result != null;
            return result;
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
