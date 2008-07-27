package com.sun.enterprise.module.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.CompilationFailureException;

import java.io.File;
import java.util.List;

/**
 * @goal hk2-test-compile
 * @phase test-compile
 * @requiresDependencyResolution test
 *
 * @author Jerome Dochez
 */
public class HK2TestCompileMojo extends HK2CompileMojo {
    /**
     * Set this to 'true' to bypass unit tests entirely.
     * Its use is NOT RECOMMENDED, but quite convenient on occasion.
     *
     * @parameter expression="${maven.test.skip}"
     */
    private boolean skip;

    /**
     * The source directories containing the test-source to be compiled.
     *
     * @parameter expression="${project.testCompileSourceRoots}"
     * @required
     * @readonly
     */
    private List compileSourceRoots;

    /**
     * Project test classpath.
     *
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    private List classpathElements;

    /**
     * The directory where compiled test classes go.
     *
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;

    public void execute()
        throws MojoExecutionException, CompilationFailureException
    {
        if ( skip )
        {
            getLog().info( "Not compiling test sources" );
        }
        else
        {
            super.execute();
        }
    }

    protected List getCompileSourceRoots()
    {
        return compileSourceRoots;
    }

    protected List getClasspathElements()
    {
        return classpathElements;
    }

    protected File getOutputDirectory()
    {
        return outputDirectory;
    }
    
}
