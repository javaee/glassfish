package com.sun.enterprise.module.maven;

import com.sun.enterprise.tools.apt.MetainfServiceGenerator;
import org.codehaus.plexus.compiler.CompilerConfiguration;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.compiler.javac.JavacCompiler;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 * {@link Compiler} for APT.
 *
 * <p>
 * In Maven, {@link Compiler} handles the actual compiler invocation.
 *
 * @author Kohsuke Kawaguchi
 */
public class AptCompiler extends JavacCompiler {

    public List compile( CompilerConfiguration config ) throws CompilerException {
        // force 1.5
        config.setTargetVersion("1.5");
        config.setSourceVersion("1.5");


        File destinationDir = new File( config.getOutputLocation() );

        if ( !destinationDir.exists() )
        {
            destinationDir.mkdirs();
        }

        String[] sourceFiles = getSourceFiles( config );

        if ( sourceFiles.length == 0 )
        {
            return Collections.EMPTY_LIST;
        }

        getLogger().info( "Compiling " + sourceFiles.length + " " +
                          "source file" + ( sourceFiles.length == 1 ? "" : "s" ) +
                          " to " + destinationDir.getAbsolutePath() );

        // this is where the META-INF/services get generated.
        config.addCompilerCustomArgument("-s",new File(config.getOutputLocation()).getAbsolutePath());
        String[] args = buildCompilerArguments( config, sourceFiles );

        return compileInProcess( args );
    }

    /**
     * Compile the java sources in the current JVM, without calling an external executable,
     * using <code>com.sun.tools.javac.Main</code> class
     *
     * @param args arguments for the compiler as they would be used in the command line javac
     * @return List of CompilerError objects with the errors encountered.
     * @throws CompilerException
     */
    protected List compileInProcess( String[] args ) throws CompilerException {
        com.sun.tools.apt.Main aptTool = new com.sun.tools.apt.Main();
        int r = aptTool.process(new MetainfServiceGenerator(),new PrintWriter(System.out,true),args);
        if(r!=0)
            throw new CompilerException("APT failed: "+r);

        // TODO: should I try to parse the output?
        return Collections.emptyList();
    }
}
