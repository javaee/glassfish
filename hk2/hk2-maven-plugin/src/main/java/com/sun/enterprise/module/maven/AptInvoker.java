package com.sun.enterprise.module.maven;

import org.codehaus.plexus.compiler.CompilerError;
import org.codehaus.plexus.compiler.CompilerException;
import org.apache.maven.plugin.Mojo;

import java.util.List;

/**
 * Meat of {@link AptCompiler}.
 *
 * <p>
 * We'd like {@link Mojo} and {@link AptCompiler} to be able to talk each other to share some state,
 * but Maven doesn't let me do this, so I resort on thread local.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class AptInvoker {
    /**
     * Compile the java sources in the current JVM, without calling an external executable,
     * using <code>com.sun.tools.javac.Main</code> class
     *
     * @param args
     *      arguments for the compiler as they would be used in the command line javac
     * @return
     *      List of {@link CompilerError} objects with the errors encountered.
     * @throws CompilerException
     */
    abstract List<CompilerError> compileInProcess( String[] args ) throws CompilerException;

    private static final ThreadLocal<AptInvoker> INVOKER = new ThreadLocal<AptInvoker>();

    static AptInvoker replace(AptInvoker i) {
        AptInvoker r = INVOKER.get();
        INVOKER.set(i);
        return r;
    }

    public static AptInvoker get() {
        return INVOKER.get();
    }
}
