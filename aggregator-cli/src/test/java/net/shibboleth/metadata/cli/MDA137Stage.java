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

package net.shibboleth.metadata.cli;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.pipeline.AbstractStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;

/**
 * A stage which does nothing except be created and destroyed.
 * 
 * @param <T> type of item to be processed by the stage
 */
public class MDA137Stage<T> extends AbstractStage<T> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(MDA137Stage.class);

    @Override
    protected void doExecute(Collection<Item<T>> itemCollection) throws StageProcessingException {
        // Do absolutely nothing
    }

    @Override
    protected void doInitialize() {
        log.info("MDA-137 initialized");
    }
    
    @Override
    protected void doDestroy() {
        log.info("MDA-137 destroyed");
    }

}
