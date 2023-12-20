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

import java.util.Collection;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for MDA-125.
 */
@Test
public class MDA125Test {

    @Test
    private void test125() throws Exception {
        final GenericXmlApplicationContext appCtx = new GenericXmlApplicationContext();
        appCtx.load(MDA125Test.class, "MDA125Test-config.xml");
        appCtx.refresh();
        final XMLSignatureValidationStage stage =
                (XMLSignatureValidationStage)appCtx.getBean("us_incommon_checkSignature");
        // do NOT initialise the bean, not all required properties have been supplied for the test
        final Collection<String> digests = stage.getBlacklistedDigests();
        Assert.assertEquals(digests.size(), 1);
        Assert.assertTrue(digests.contains("http://www.w3.org/2001/04/xmldsig-more#md5"));
        final Collection<String> methods = stage.getBlacklistedSignatureMethods();
        Assert.assertEquals(methods.size(), 1);
        Assert.assertTrue(methods.contains("http://www.w3.org/2001/04/xmldsig-more#rsa-md5"));
        appCtx.close();
    }

}