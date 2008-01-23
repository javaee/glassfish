package com.sun.enterprise.module.maven;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.common_impl.DefaultModuleDefinition;
import com.sun.enterprise.module.common_impl.DefaultModuleDefinition;
import com.sun.enterprise.module.common_impl.Tokenizer;
import com.sun.enterprise.module.common_impl.DefaultModuleDefinition;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.jar.Attributes;

/**
 * {@link ModuleDefinition} that loads {@value ManifestConstants#CLASS_PATH}
 * from the local maven repository.
 *
 * @author Kohsuke Kawaguchi
 */
final class MavenModuleDefinition extends DefaultModuleDefinition {
    MavenModuleDefinition(MavenProjectRepository repository, File location) throws IOException {
        super(location);

        try {
            String classpath = mainAttributes.getValue(ManifestConstants.CLASS_PATH_ID);
            for( String id : new Tokenizer(classpath," ")) {
                File jar = repository.resolveArtifact(id);
                classPath.add(jar.toURI());
            }
        } catch (IOException e) {
            throw new IOException2("Failed to process "+ManifestConstants.CLASS_PATH_ID+" for "+location,e);
        }
    }

    void addClasspath(File location) {
        // insert at the front so that this takes precedence.
        classPath.add(0,location.toURI());
    }

    /**
     * Instead of looking at {@value ManifestConstants#CLASS_PATH}, look at
     * {@link ManifestConstants#CLASS_PATH_ID} and pick up artifacts from
     * the repository.
     */
    @Override
    protected void parseClassPath(Attributes attr, URI baseURI) {
        // noop
    }
}
