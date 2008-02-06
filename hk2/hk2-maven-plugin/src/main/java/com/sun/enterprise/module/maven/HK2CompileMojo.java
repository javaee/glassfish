package com.sun.enterprise.module.maven;

import com.sun.enterprise.tools.apt.MetainfServiceGenerator;
import org.apache.maven.plugin.AbstractCompilerMojo;
import org.apache.maven.plugin.CompilationFailureException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.CompilerError;
import org.codehaus.plexus.compiler.CompilerException;
import org.jvnet.hk2.config.generator.AnnotationProcessorFactoryImpl;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * @goal hk2-compile
 * @phase compile
 * @author Kohsuke Kawaguchi
 */
public class HK2CompileMojo extends CompilerMojo {
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

        // invoke APT with the known set of annotation processors that we care about.
        AptInvoker old = AptInvoker.replace(new AptInvoker() {
            List<CompilerError> compileInProcess(String[] args) throws CompilerException {
                com.sun.tools.apt.Main aptTool = new com.sun.tools.apt.Main();
                int r = aptTool.process(
                    new CompositeAnnotationProcessorFactory(
                        new MetainfServiceGenerator(),
                        new AnnotationProcessorFactoryImpl()
                    ), new PrintWriter(System.out,true),args);
                if(r!=0)
                    throw new CompilerException("APT failed: "+r);

                // TODO: should I try to parse the output?
                return Collections.emptyList();
            }
        });
        try {
            super.execute();
        } finally {
            AptInvoker.replace(old);
        }

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
