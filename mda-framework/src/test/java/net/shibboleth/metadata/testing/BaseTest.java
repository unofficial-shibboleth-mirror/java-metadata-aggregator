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

package net.shibboleth.metadata.testing;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import net.shibboleth.shared.logic.Constraint;

public abstract class BaseTest {

    /** Class being tested. */
    protected final @Nonnull Class<?> testingClass;
    
    /**
     * Base path for class-relative test resource references.
     * 
     * Will <em>not</em> end in a '/'.
     */
    private final @Nonnull String baseClassPath;
    
    /** Package for the class being tested. */
    private final @Nonnull Package testingPackage;
    
    /**
     * Base path for package-relative test resource references.
     * 
     * Will always end in a '/'.
     */
    private final @Nonnull String basePackagePath;
    
    /**
     * Constructor
     * 
     * @param clazz class under test
     */
    protected BaseTest(final @Nonnull Class<?> clazz) {
        testingClass = clazz;
        final @Nonnull String testingClassName = Constraint.isNotNull(testingClass.getName(),
                "testing class name can not be null");
        baseClassPath = nameToPath(testingClassName);
        testingPackage = Constraint.isNotNull(testingClass.getPackage(),
                "testing class package can not be null");
        final var testingPackageName = Constraint.isNotNull(testingPackage.getName(),
                "testing package name can not be null");
        basePackagePath = nameToPath(testingPackageName) + "/";
    }
    
    /**
     * Converts the "."-separated name of a class or package into an
     * absolute path.
     * 
     * @param name name to be converted
     * @return path to resources associated with the name
     */
    private @Nonnull String nameToPath(final @Nonnull String name) {
        return "/" + name.replace('.', '/');
    }
        
    /**
     * Makes a resource reference relative to the class being tested.
     * 
     * The convention adopted is that the class-relative name is something
     * like "foo.pem", and that this is expanded to "/a/b/c/Bar-foo.pem".
     * 
     * @param which class-relative resource name
     * @return absolute resource name
     */
    protected @Nonnull String classRelativeResource(final @Nonnull String which) {
        return baseClassPath + "-" + which;
    }
    
    protected @Nonnull String simpleClassRelativeName(final String which) {
        return testingClass.getSimpleName() + "-" + which;
    }
        
    /**
     * Makes a resource reference relative to the package of the class being tested.
     * 
     * The convention adopted is that the package-relative name is something
     * like "foo.pem", and that this is expanded to "/a/b/c/foo.pem".
     * 
     * @param which package-relative resource name
     * @return absolute resource name
     */
    protected @Nonnull String packageRelativeResource(final String which) {
        return basePackagePath + which;
    }
    
    /**
     * Helper method to acquire a ClassPathResource based on the given resource path.
     * 
     * Uses class-relative resource names if there is a known class under test.
     * 
     * @param resourcePath classpath path to the resource
     * @return the data file as a resource
     */
    public @Nonnull Resource getClasspathResource(final String resourcePath) {
        return new ClassPathResource(simpleClassRelativeName(resourcePath), testingClass);
    }

    /**
     * Read a classpath resource as an array of <code>byte</code>s.
     *
     * @param resourcePath classpath path to the resource
     * 
     * @return the resource as an array of <code>byte</code>s
     *
     * @throws IOException on error
     */
    public byte[] readBytes(@Nonnull final String resourcePath) throws IOException {
        final var res = getClasspathResource(resourcePath);
        try (var stream = res.getInputStream()) {
            return stream.readAllBytes();
        }
    }

}
