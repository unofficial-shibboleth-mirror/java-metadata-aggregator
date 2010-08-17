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

package edu.internet2.middleware.shibboleth.metadata.core.pipeline;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.opensaml.util.Assert;
import org.opensaml.util.codec.Base64;
import org.springframework.core.convert.converter.Converter;

import edu.internet2.middleware.shibboleth.metadata.core.EntityIdInfo;
import edu.internet2.middleware.shibboleth.metadata.core.Metadata;
import edu.internet2.middleware.shibboleth.metadata.core.MetadataCollection;

/**
 * A pipeline stage that, if present, takes each {@link EntityIdInfo} associated with a metadata element, transforms it
 * value using a set of registered transformers, and associates an additional {@link EntityIdInfo} (whose value is the
 * result of the transform) with the element.
 */
public class EntityIdTransformStage extends AbstractComponent implements Stage<Metadata<?>> {

    /** Transformers used on IDs. */
    public List<Converter<String, String>> idTransformers;

    /**
     * Constructor.
     * 
     * @param unqiue ID for this stage, never null
     */
    public EntityIdTransformStage(String stageId, List<Converter<String, String>> transformers) {
        super(stageId);

        Assert.isNotEmpty(transformers, "Identity transformers may not be null or empty");
    }

    /** {@inheritDoc} */
    public MetadataCollection<Metadata<?>> execute(MetadataCollection<Metadata<?>> metadataCollection)
            throws StageProcessingException {
        ComponentInfo compInfo = new ComponentInfo(this);

        List<EntityIdInfo> ids;
        String transformedId;
        for (Metadata<?> element : metadataCollection) {
            ids = element.getMetadataInfo().get(EntityIdInfo.class);
            for (EntityIdInfo id : ids) {
                for (Converter<String, String> idTransform : idTransformers) {
                    transformedId = idTransform.convert(id.getEntityId());
                    element.getMetadataInfo().put(new EntityIdInfo(transformedId));
                }
            }
            element.getMetadataInfo().put(compInfo);
        }

        compInfo.setCompleteInstant();

        return metadataCollection;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        // nothing to do
    }

    /** Converts a string in to another string that is the SHA1 hash of the original string prepended with "{sha1}". */
    public static class Sha1Converter implements Converter<String, String> {

        /** {@inheritDoc} */
        public String convert(String source) {
            try {
                MessageDigest shaDigester = MessageDigest.getInstance("SHA-1");
                String target = Base64.encodeBytes(shaDigester.digest(source.getBytes()));
                return "{sha1}" + target;
            } catch (NoSuchAlgorithmException e) {
                // nothing to do, this is required to be supported by the JVM
                return null;
            }
        }
    }

    /** Converts a string in to another string that is the MD5 hash of the original string prepended with "{md5}". */
    public static class Md5Converter implements Converter<String, String> {

        /** {@inheritDoc} */
        public String convert(String source) {
            try {
                MessageDigest shaDigester = MessageDigest.getInstance("MD5");
                String target = Base64.encodeBytes(shaDigester.digest(source.getBytes()));
                return "{md5}" + target;
            } catch (NoSuchAlgorithmException e) {
                // nothing to do, this is required to be supported by the JVM
                return null;
            }
        }
    }
}