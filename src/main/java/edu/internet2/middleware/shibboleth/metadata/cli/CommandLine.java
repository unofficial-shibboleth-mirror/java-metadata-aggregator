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

package edu.internet2.middleware.shibboleth.metadata.cli;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import edu.internet2.middleware.shibboleth.metadata.core.pipeline.Pipeline;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.PipelineResult;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.sink.Sink;
import edu.internet2.middleware.shibboleth.metadata.core.pipeline.source.Source;

/**
 *
 */
public final class CommandLine {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        ApplicationContext springContext = buildApplicationContext(args);

        Source source = springContext.getBean(Source.class);
        Sink sink = springContext.getBean(Sink.class);
        Pipeline pipeline = springContext.getBean(Pipeline.class);

        PipelineResult result = pipeline.execute(source, sink);
        if (result.wasSuccessful()) {
            System.out.println("sucess");
        } else {
            result.getException().printStackTrace(System.err);
        }
    }

    private static ApplicationContext buildApplicationContext(String[] contextFiles) {
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(contextFiles);
        context.start();
        return context;
    }
}