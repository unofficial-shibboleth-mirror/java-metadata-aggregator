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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.jcip.annotations.ThreadSafe;
import net.shibboleth.metadata.EntityIdInfo;
import net.shibboleth.metadata.Metadata;
import net.shibboleth.metadata.MetadataCollection;
import net.shibboleth.metadata.pipeline.AbstractComponent;
import net.shibboleth.metadata.pipeline.ComponentInfo;
import net.shibboleth.metadata.pipeline.Stage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.metadata.util.MetadataInfoHelper;

import org.opensaml.util.collections.CollectionSupport;
import org.opensaml.util.collections.LazyList;
import org.springframework.core.convert.converter.Converter;

import edu.vt.middleware.crypt.digest.MD5;
import edu.vt.middleware.crypt.digest.SHA1;
import edu.vt.middleware.crypt.util.HexConverter;

/**
 * A pipeline stage that, if present, takes each {@link EntityIdInfo} associated with a metadata element, transforms it
 * value using a set of registered transformers, and associates an additional {@link EntityIdInfo} (whose value is the
 * result of the transform) with the element.
 */
@ThreadSafe
public class EntityIdTransformStage extends AbstractComponent implements Stage<Metadata<?>> {

    /** Transformers used on IDs. */
    private Collection<Converter<String, String>> idTransformers = new LazyList<Converter<String, String>>();

    /**
     * Gets the transforms used to produce the transformed entity IDs.
     * 
     * @return transforms used to produce the transformed entity IDs, never null
     */
    public Collection<Converter<String, String>> getIdTransformers() {
        return idTransformers;
    }

    /**
     * Sets the transforms used to produce the transformed entity IDs.
     * 
     * @param transformers transforms used to produce the transformed entity IDs
     */
    public synchronized void setIdTransformers(final Collection<Converter<String, String>> transformers) {
        if (isInitialized()) {
            return;
        }
        idTransformers = CollectionSupport.addNonNull(transformers, new LazyList<Converter<String, String>>());
    }

    /** {@inheritDoc} */
    public MetadataCollection<Metadata<?>> execute(final MetadataCollection<Metadata<?>> metadataCollection)
            throws StageProcessingException {
        final ComponentInfo compInfo = new ComponentInfo(this);

        List<EntityIdInfo> ids;
        final List<EntityIdInfo> transformedIds = new ArrayList<EntityIdInfo>();
        String transformedId;
        for (Metadata<?> element : metadataCollection) {
            ids = element.getMetadataInfo().get(EntityIdInfo.class);
            for (EntityIdInfo id : ids) {
                for (Converter<String, String> idTransform : idTransformers) {
                    transformedId = idTransform.convert(id.getEntityId());
                    transformedIds.add(new EntityIdInfo(transformedId));
                }
            }
            MetadataInfoHelper.addToAll(element, transformedIds.toArray(new EntityIdInfo[] {}));
            element.getMetadataInfo().put(compInfo);
        }

        compInfo.setCompleteInstant();

        return metadataCollection;
    }

    /** Converts a string in to another string that is the SHA1 hash of the original string prepended with "{sha1}". */
    public static class Sha1Converter implements Converter<String, String> {

        /** {@inheritDoc} */
        public String convert(final String source) {
            SHA1 sha1 = new SHA1();
            return "{sha1}" + sha1.digest(source.getBytes(), new HexConverter());
        }
    }

    /** Converts a string in to another string that is the MD5 hash of the original string prepended with "{md5}". */
    public static class Md5Converter implements Converter<String, String> {

        /** {@inheritDoc} */
        public String convert(final String source) {
            MD5 md5 = new MD5();
            return "{md5}" + md5.digest(source.getBytes(), new HexConverter());
        }
    }
}