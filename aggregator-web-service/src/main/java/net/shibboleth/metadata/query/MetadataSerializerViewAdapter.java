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

package net.shibboleth.metadata.query;

import java.io.OutputStream;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.MetadataSerializer;

import org.opensaml.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;


/** Adapts a {@link MetadataSerializer} to a Spring {@link View}. */
public class MetadataSerializerViewAdapter implements View {

    /** Class logger. */
    private Logger log = LoggerFactory.getLogger(MetadataSerializerViewAdapter.class);

    /** Media type handled by this view. */
    private MediaType mediaType;

    /** Serializer used to serialize a {@link MetadataCollection} to an {@link OutputStream}. */
    private MetadataSerializer<Metadata<?>> serializer;

    /**
     * Constructor.
     * 
     * @param viewType media type serviced by this view
     * @param metadataSerializer serializer for resultant metadata collections
     */
    public MetadataSerializerViewAdapter(final MediaType viewType,
            final MetadataSerializer<Metadata<?>> metadataSerializer) {
        Assert.isNotNull(viewType, "View media type may not be null");
        mediaType = viewType;

        Assert.isNotNull(metadataSerializer, "Metadata serializer may not be null");
        serializer = metadataSerializer;
    }

    /** {@inheritDoc} */
    public String getContentType() {
        return mediaType.toString();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public void render(final Map<String, ?> model, final HttpServletRequest httpRequest,
            final HttpServletResponse httpResponse) throws Exception {
        final MetadataCollection<Metadata<?>> metadataCollection = (MetadataCollection<Metadata<?>>) model
                .get(QueryController.METADATA_MODEL_ATTRIB);

        if (metadataCollection == null || metadataCollection.isEmpty()) {
            httpResponse.sendError(404);
            return;
        }

        OutputStream out = httpResponse.getOutputStream();

        String acceptEncoding = httpRequest.getHeader("Accept-Encoding");
        if (acceptEncoding != null) {
            if (acceptEncoding.contains("gzip")) {
                httpResponse.setHeader("Content-Encoding", "gzip");
                out = new GZIPOutputStream(out);
            } else if (acceptEncoding.contains("compress")) {
                httpResponse.setHeader("Content-Encoding", "compress");
                out = new DeflaterOutputStream(out);
            }
        }

        try {
            serializer.serialize(metadataCollection, out);
            out.flush();
        } catch (Exception e) {
            log.warn("Unable to serialize metadata for request to " + httpRequest.getRequestURI(), e);
            httpResponse.sendError(500, "unable to serialize metadata");
        }
    }
}