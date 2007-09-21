package com.sun.enterprise.module.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.CompilationFailureException;
import org.apache.maven.plugin.AbstractCompilerMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import java.lang.reflect.Field;
import java.io.File;

/**
 * @goal hk2-compile
 * @phase compile
 * @author Kohsuke Kawaguchi
 */
public class AptMojo extends CompilerMojo {
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    public void execute() throws MojoExecutionException, CompilationFailureException {
        // overwrite the compilerId value. This seems to be the only way to
        //do so without touching the copied files.
        setField("compilerId", "hk2-apt");

        super.execute();

        // TODO: ideally we should do this in the AptCompiler class, but I don't know
        // how to get MavenProject injected there
        project.getCompileSourceRoots().add(new File(project.getBasedir(),"target/apt-generated-sources").getAbsolutePath());
    }

    private void setField(String name, String value) {
        try {
            Field field = AbstractCompilerMojo.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e); // impossible
        } catch (IllegalAccessException e) {
            throw new AssertionError(e); // impossible
        }
    }
}
