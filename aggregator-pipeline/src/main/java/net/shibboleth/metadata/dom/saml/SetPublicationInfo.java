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

package net.shibboleth.metadata.dom.saml;

import java.util.Collection;

import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.dom.DomElementItem;
import net.shibboleth.metadata.pipeline.BaseStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;

/**
 * This stage is not yet implemented.
 * 
 * Sets the publication information on every EntityDescriptor or EntitiesDescriptor within the metadata collection. If a
 * given descriptor already contains publication information than the existing information is moved to the publication
 * path.
 */
@ThreadSafe
public class SetPublicationInfo extends BaseStage<DomElementItem> {

    /** {@inheritDoc} */
    protected void doExecute(Collection<DomElementItem> metadataCollection) throws StageProcessingException {
        // TODO Auto-generated method stub

    }
}