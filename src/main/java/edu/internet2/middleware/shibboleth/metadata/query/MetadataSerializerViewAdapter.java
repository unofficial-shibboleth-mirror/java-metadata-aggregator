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

package edu.internet2.middleware.shibboleth.metadata.query;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

import edu.internet2.middleware.shibboleth.metadata.core.Metadata;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataSerializer;

/**
 *
 */
public class MetadataSerializerViewAdapter implements View {

    private MediaType mediaType;
    
    private MetadataSerializer<Metadata<?>> serializer;
    
    public MetadataSerializerViewAdapter(MediaType viewType, MetadataSerializer<Metadata<?>> serializer){
        
    }
    
    /** {@inheritDoc} */
    public String getContentType() {
        return mediaType.toString();
    }

    /** {@inheritDoc} */
    public void render(Map<String, ?> model, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        MetadataCollection<Metadata<?>> metadataCollection = null; //TODO
        
        serializer.serialize(metadataCollection, httpResponse.getOutputStream());
        //TODO HTTP response
    }

}
