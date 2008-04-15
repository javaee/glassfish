/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
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
