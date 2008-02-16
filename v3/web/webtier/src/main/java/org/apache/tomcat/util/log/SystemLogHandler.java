

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.tomcat.util.log;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Stack;
import java.util.EmptyStackException;

/**
 * This helper class may be used to do sophisticated redirection of 
 * System.out and System.err on a per Thread basis.
 * 
 * A stack is implemented per Thread so that nested startCapture
 * and stopCapture can be used.
 *
 * @author Remy Maucherat
 * @author Glenn L. Nielsen
 */
public class SystemLogHandler extends PrintStream {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct the handler to capture the output of the given steam.
     */
    public SystemLogHandler(PrintStream wrapped) {
        super(wrapped);
        out = wrapped;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Wrapped PrintStream.
     */
    protected PrintStream out = null;


    /**
     * Thread <-> CaptureLog associations.
     */
    protected static final ThreadLocal logs = new ThreadLocal();


    /**
     * Spare CaptureLog ready for reuse.
     */
    protected static final Stack reuse = new Stack();


    // --------------------------------------------------------- Public Methods


    /**
     * Start capturing thread's output.
     */
    public static void startCapture() {
        CaptureLog log = null;
        if (!reuse.isEmpty()) {
            try {
                log = (CaptureLog)reuse.pop();
            } catch (EmptyStackException e) {
                log = new CaptureLog();
            }
        } else {
            log = new CaptureLog();
        }
        Stack stack = (Stack)logs.get();
        if (stack == null) {
            stack = new Stack();
            logs.set(stack);
        }
        stack.push(log);
    }


    /**
     * Stop capturing thread's output and return captured data as a String.
     */
    public static String stopCapture() {
        Stack stack = (Stack)logs.get();
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        CaptureLog log = (CaptureLog)stack.pop();
        if (log == null) {
            return null;
        }
        String capture = log.getCapture();
        log.reset();
        reuse.push(log);
        return capture;
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Find PrintStream to which the output must be written to.
     */
    protected PrintStream findStream() {
        Stack stack = (Stack)logs.get();
        if (stack != null && !stack.isEmpty()) {
            CaptureLog log = (CaptureLog)stack.peek();
            if (log != null) {
                PrintStream ps = log.getStream();
                if (ps != null) {
                    return ps;
                }
            }
        }
        return out;
    }


    // ---------------------------------------------------- PrintStream Methods


    public void flush() {
        findStream().flush();
    }

    public void close() {
        findStream().close();
    }

    public boolean checkError() {
        return findStream().checkError();
    }

    protected void setError() {
        //findStream().setError();
    }

    public void write(int b) {
        findStream().write(b);
    }

    public void write(byte[] b)
        throws IOException {
        findStream().write(b);
    }

    public void write(byte[] buf, int off, int len) {
        findStream().write(buf, off, len);
    }

    public void print(boolean b) {
        findStream().print(b);
    }

    public void print(char c) {
        findStream().print(c);
    }

    public void print(int i) {
        findStream().print(i);
    }

    public void print(long l) {
        findStream().print(l);
    }

    public void print(float f) {
        findStream().print(f);
    }

    public void print(double d) {
        findStream().print(d);
    }

    public void print(char[] s) {
        findStream().print(s);
    }

    public void print(String s) {
        findStream().print(s);
    }

    public void print(Object obj) {
        findStream().print(obj);
    }

    public void println() {
        findStream().println();
    }

    public void println(boolean x) {
        findStream().println(x);
    }

    public void println(char x) {
        findStream().println(x);
    }

    public void println(int x) {
        findStream().println(x);
    }

    public void println(long x) {
        findStream().println(x);
    }

    public void println(float x) {
        findStream().println(x);
    }

    public void println(double x) {
        findStream().println(x);
    }

    public void println(char[] x) {
        findStream().println(x);
    }

    public void println(String x) {
        findStream().println(x);
    }

    public void println(Object x) {
        findStream().println(x);
    }

}
