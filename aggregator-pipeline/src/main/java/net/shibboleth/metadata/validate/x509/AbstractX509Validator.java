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

package net.shibboleth.metadata.validate.x509;

import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.validate.BaseValidator;
import net.shibboleth.metadata.validate.Validator;

/**
 * Abstract class implementing validation on {@link X509Certificate} objects.
 *
 * X.509 certificate issues are almost always independent, so simplify the validator interface
 * by delegation.
 */
public abstract class AbstractX509Validator extends BaseValidator implements Validator<X509Certificate> {

    /**
     * Apply the validator to the object in the given {@link Item} context.
     * 
     * The validator influences future processing by adding item metadata to the {@link Item}.
     * 
     * @param cert the certificate to be validated
     * @param item the {@link Item} context for the validation
     * @param stageId the identifier for the stage that is requesting the validation, for
     *      inclusion in status metadata
     * @throws StageProcessingException if an error occurs during validation
     */
    protected abstract void doValidate(@Nonnull final X509Certificate cert, @Nonnull final Item<?> item,
            @Nonnull final String stageId) throws StageProcessingException;

    @Override
    public Action validate(@Nonnull final X509Certificate cert, @Nonnull final Item<?> item,
            @Nonnull final String stageId) throws StageProcessingException {
        doValidate(cert, item, stageId);
        return Action.CONTINUE;
    }

}
