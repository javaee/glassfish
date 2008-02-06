package com.sun.enterprise.module.maven;

import org.codehaus.plexus.compiler.CompilerConfiguration;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.compiler.javac.JavacCompiler;

import java.io.File;
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

        /*
            Source path pitfall.

            When APT goes multiple rounds, the files compiled in the previous round
            needs to be made available to the next round. APT does this internally
            by adding '-sourcepath' option, but since Maven only passes in the list
            of individual files to be compiled, this simply doesn't work.

            So here I'm manually constructing -sourcepath option and pass that on to APT.
         */
        String sourcePath = "";
        for (Object o : config.getSourceLocations()) {
            sourcePath += o+File.pathSeparator;
        }
        config.addCompilerCustomArgument("-sourcepath",sourcePath);

        // keep the generated source files on the side so that they can be bundled into sources-jar.
        File sourceDest = new File(config.getBuildDirectory(), "apt-generated-sources");
        sourceDest.mkdirs();
        config.addCompilerCustomArgument("-s", sourceDest.getAbsolutePath());
        String[] args = buildCompilerArguments( config, sourceFiles );

        return AptInvoker.get().compileInProcess(args);
    }
}
