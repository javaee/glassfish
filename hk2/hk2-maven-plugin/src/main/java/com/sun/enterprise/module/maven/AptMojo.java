package com.sun.enterprise.module.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.CompilationFailureException;
import org.apache.maven.plugin.AbstractCompilerMojo;

import java.lang.reflect.Field;

/**
 * @goal hk2-compile
 * @phase compile
 * @author Kohsuke Kawaguchi
 */
public class AptMojo extends CompilerMojo {

    public void execute() throws MojoExecutionException, CompilationFailureException {
        // overwrite the compilerId value. This seems to be the only way to
        //do so without touching the copied files.
        setField("compilerId", "hk2-apt");

        super.execute();
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
