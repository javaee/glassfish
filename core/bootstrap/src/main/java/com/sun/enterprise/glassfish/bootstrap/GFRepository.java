package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
final class GFRepository extends DirectoryBasedRepository {
    public GFRepository(String name, File repository) throws FileNotFoundException {
        super(name, repository);
    }

    @Override
    protected ModuleDefinition loadJar(File jar) throws IOException {
        return new GFModuleDefinition(jar);
    }
}
