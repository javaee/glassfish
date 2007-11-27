package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.impl.DefaultModuleDefinition;

import java.io.File;
import java.io.IOException;

/**
 * {@link ModuleDefinition} that loads {@value ManifestConstants#CLASS_PATH}
 * from a lib/jars subdirectory.
 *
 * @author Kohsuke Kawaguchi
 */
final class GFModuleDefinition extends DefaultModuleDefinition {
    GFModuleDefinition(File location) throws IOException {
        super(location);
    }

//    /**
//     * Load from "jars".
//     */
//    @Override
//    protected String decorateClassPath(String classpathElement) {
//        return "jars/"+classpathElement;
//    }
    // no such convention defined so far
}
