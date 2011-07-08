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

package net.shibboleth.metadata.dom;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** {@link SimpleNamespaceContext} test. */
public class SimpleNamespaceContextTest {
    
    /** Standard set of mappings. */
    private NamespaceContext stdContext;

    /**
     * Create standard set of mappings for use by multiple tests.
     */
    @BeforeClass
    void createStandardMappings() {
        Map<String, String> prefixMappings = new HashMap<String, String>();
        prefixMappings.put("a", "value:of:a");
        prefixMappings.put("b", "value:of:b");
        stdContext = new SimpleNamespaceContext(prefixMappings);
    }
    
    /**
     * Test for getNamespaceURI method.
     */
    @Test
    public void getNamespaceURI() {
        Assert.assertEquals(stdContext.getNamespaceURI("a"), "value:of:a");
        Assert.assertEquals(stdContext.getNamespaceURI("b"), "value:of:b");
        Assert.assertNull(stdContext.getNamespaceURI("c"));
    }
}
