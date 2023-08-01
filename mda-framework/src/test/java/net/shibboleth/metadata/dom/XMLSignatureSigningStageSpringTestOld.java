/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import net.shibboleth.metadata.dom.XMLSignatureSigningStage.SHAVariant;

@ContextConfiguration({"XMLSignatureSigningStageSpringTestOld-config.xml"})
public class XMLSignatureSigningStageSpringTestOld extends AbstractTestNGSpringContextTests {
    
    @Autowired
    XMLSignatureSigningStage stage;
    
    @SuppressWarnings("removal")
    @Test
    public void testSetSHAVariant() throws Exception {
        // Check use of the old deprecated getter.
        // The deprecated setter is used in the configuration file.
        // We should see deprecation warnings for both in the logs.
        Assert.assertEquals(stage.getShaVariant(), SHAVariant.SHA384);
    }

}
