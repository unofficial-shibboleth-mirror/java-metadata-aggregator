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

package net.shibboleth.metadata.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.pipeline.BaseIteratingStage;
import net.shibboleth.metadata.pipeline.StageProcessingException;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.collection.LazyList;

import org.bouncycastle.crypto.digests.MD5Digest;
import org.cryptacular.util.CodecUtil;
import org.cryptacular.util.HashUtil;
import org.springframework.core.convert.converter.Converter;

import com.google.common.base.Predicates;

/**
 * A pipeline stage that, if present, takes each {@link ItemId} associated with a metadata element, transforms it
 * value using a set of registered transformers, and associates an additional {@link ItemId} (whose value is the
 * result of the transform) with the element.
 * 
 * @param <T> type of metadata this stage operates upon
 */
@ThreadSafe
public class EntityIdTransformStage<T> extends BaseIteratingStage<T> {

    /** Transformers used on IDs. */
    private Collection<Converter<String, String>> idTransformers = new LazyList<>();

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
        CollectionSupport.addIf(idTransformers, transformers, Predicates.notNull());
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doExecute(@Nonnull final Item<T> item) throws StageProcessingException {
        final List<ItemId> ids = item.getItemMetadata().get(ItemId.class);

        final List<ItemId> transformedIds = new ArrayList<>();
        for (ItemId id : ids) {
            for (Converter<String, String> idTransform : idTransformers) {
                final String transformedId = idTransform.convert(id.getId());
                transformedIds.add(new ItemId(transformedId));
            }
        }
        item.getItemMetadata().putAll(transformedIds);
        
        return true;
    }

    /** Converts a string in to another string that is the SHA1 hash of the original string prepended with "{sha1}". */
    public static class Sha1Converter implements Converter<String, String> {

        /** {@inheritDoc} */
        @Override
        public String convert(final String source) {
            return "{sha1}" + CodecUtil.hex(HashUtil.sha1(source.getBytes()));
        }
    }

    /** Converts a string in to another string that is the MD5 hash of the original string prepended with "{md5}". */
    public static class Md5Converter implements Converter<String, String> {

        /** {@inheritDoc} */
        @Override
        public String convert(final String source) {
            return "{md5}" + CodecUtil.hex(HashUtil.hash(new MD5Digest(), source.getBytes()));
        }
    }
}