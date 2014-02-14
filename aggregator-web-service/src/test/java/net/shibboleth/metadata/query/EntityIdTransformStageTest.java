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
import java.util.List;

import net.shibboleth.metadata.Item;
import net.shibboleth.metadata.ItemId;
import net.shibboleth.metadata.pipeline.ComponentInfo;

import org.springframework.core.convert.converter.Converter;
import org.testng.annotations.Test;

public class EntityIdTransformStageTest {

    @Test
    public void test() throws Exception {
        MockItem item = new MockItem("test");
        item.getItemMetadata().put(new ItemId("http://example.org"));

        final ArrayList<Item<String>> mdColl = new ArrayList<>();
        mdColl.add(item);

        ArrayList<Converter<String, String>> transforms = new ArrayList<>();
        transforms.add(new EntityIdTransformStage.Md5Converter());
        transforms.add(new EntityIdTransformStage.Sha1Converter());

        final EntityIdTransformStage<String> stage = new EntityIdTransformStage<>();
        stage.setId("test");
        stage.setIdTransformers(transforms);
        assert !stage.isInitialized();

        stage.initialize();
        assert stage.isInitialized();

        stage.execute(mdColl);
        assert mdColl.size() == 1;
        assert item.getItemMetadata().values().size() == 4;

        ComponentInfo compInfo = item.getItemMetadata().get(ComponentInfo.class).get(0);
        assert compInfo.getCompleteInstant() != null;
        assert compInfo.getComponentId().equals("test");
        assert compInfo.getComponentType().equals(EntityIdTransformStage.class);
        assert compInfo.getStartInstant() != null;

        final List<ItemId> idInfos = item.getItemMetadata().get(ItemId.class);
        assert idInfos.size() == 3;

        boolean idMatch = false, sha1Match = false, md5Match = false;
        String id;
        for (ItemId info : idInfos) {
            id = info.getId();
            if (id.startsWith("{sha1}")) {
                sha1Match = "{sha1}ff7c1f10ab54968058fdcfaadf1b2457cd5d1a3f".equals(id);
            } else if (id.startsWith("{md5}")) {
                md5Match = "{md5}dab521de65f9250b4cca7383feef67dc".equals(id);
            } else {
                idMatch = "http://example.org".equals(id);
            }
        }

        assert idMatch;
        assert sha1Match;
        assert md5Match;
    }
}