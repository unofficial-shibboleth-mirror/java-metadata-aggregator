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

package edu.internet2.middleware.shibboleth.metadata.pipeline;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.opensaml.util.xml.StaticBasicParserPool;
import org.testng.annotations.Test;

import edu.internet2.middleware.shibboleth.metadata.core.pipeline.BasicPipeline;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.stage.Stage;
import edu.internet2.middleware.shibboleth.metadata.dom.DomMetadataElement;
import edu.internet2.middleware.shibboleth.metadata.dom.saml.SAMLConstants;
import edu.internet2.middleware.shibboleth.metadata.dom.saml.SAMLEntitiesDescriptorAssemblerStage;
import edu.internet2.middleware.shibboleth.metadata.dom.saml.SAMLEntityFilterStage;
import edu.internet2.middleware.shibboleth.metadata.dom.saml.SAMLEntitySplitterStage;
import edu.internet2.middleware.shibboleth.metadata.dom.sink.DomFilesystemSink;
import edu.internet2.middleware.shibboleth.metadata.dom.source.DomFilesystemSource;

/**
 *
 */
public class BasicPipelineTest {

    @Test
    public void testBasicPipelineWithoutStages() throws Exception {
        
        StaticBasicParserPool parserPool = new StaticBasicParserPool();
        parserPool.initialize();

        ArrayList<Stage<DomMetadataElement>> stages = new ArrayList<Stage<DomMetadataElement>>();

        SAMLEntitySplitterStage splitterStage = new SAMLEntitySplitterStage("splitter");
        stages.add(splitterStage);

//        SAMLEntityFilterStage filterStage = new SAMLEntityFilterStage("filter");
//        filterStage.setRemoveContactPerson(true);
//        filterStage.setRemoveContactPerson(true);
//        filterStage.setRemoveRolelessEntities(true);
//        filterStage.getWhitelistedRoles().add(new QName(SAMLConstants.MD_NS, "SPSSODescriptor"));
//        stages.add(filterStage);

        SAMLEntitiesDescriptorAssemblerStage assemblyStage = new SAMLEntitiesDescriptorAssemblerStage("assembly", parserPool);
        stages.add(assemblyStage);

        BasicPipeline<DomMetadataElement> pipeline = new BasicPipeline<DomMetadataElement>("pipe", stages);

        DomFilesystemSource source = new DomFilesystemSource("source", parserPool, "/Users/lajoie/Downloads/metadata");
        //source.setRecurseDirectories(true);
        //source.setErrorCausesSourceFailure(true);
        
        DomFilesystemSink sink = new DomFilesystemSink("sink", "/Users/lajoie/Downloads/mdcopy.xml");

        try{
        pipeline.execute(source, sink);
        }catch(Throwable t){
            t.printStackTrace();
        }
    }
}