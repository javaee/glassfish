/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.enhancer;

import java.io.PrintWriter;

import com.sun.org.apache.jdo.impl.enhancer.util.Support;



/**
 * Provides some basic utilities for main classes.
 *
 * @author Martin Zaun
 */
class LogSupport
    extends Support
{
    /**
     * The stream to write messages to.
     */
    protected final PrintWriter out;

    /**
     * The stream to write error messages to.
     */
    protected final PrintWriter err;
    
    /**
     * Creates an instance.
     */
    public LogSupport(PrintWriter out,
                      PrintWriter err) 
    {
        affirm(out != null);
        affirm(err != null);
        this.out = out;
        this.err = err;
    }

    /**
     * Prints out an error message.
     */
    protected void printlnErr(String msg,
                              Throwable ex,
                              boolean verbose)
    {
        out.flush();
        if (msg != null) {
            err.println(msg);
        }
        if (ex != null) {
            if (verbose) {
                ex.printStackTrace(err);
            }
            else {
                err.println(ex.toString());
            }
        }
    }

    /**
     * Prints out an error message.
     */
    protected void printlnErr(String msg,
                              Throwable ex)
    {
        out.flush();
        err.println(msg + ": " + ex.getMessage());
        ex.printStackTrace(err);
    }

    /**
     * Prints out an error message.
     */
    protected void printlnErr(String msg)
    {
        out.flush();
        err.println(msg);
    }

    /**
     * Prints out an error message.
     */
    protected void printlnErr()
    {
        out.flush();
        err.println();
    }

    /**
     * Prints out a message.
     */
    protected void print(String msg)
    {
        out.print(msg);
    }

    /**
     * Prints out a message.
     */
    protected void println(String msg)
    {
        out.println(msg);
    }

    /**
     * Prints out a message.
     */
    protected void println()
    {
        out.println();
    }

    /**
     * Flushes streams.
     */
    protected void flush()
    {
        out.flush();
        err.flush();
    }

    // ----------------------------------------------------------------------

//^olsen: support for I18N

    /**
     *  Prints out a warning message.
     *
     *  @param msg the message
     */
/*
    public void printWarning(String msg)
    {
        out.println(getI18N("enhancer.warning", msg));
    }
*/
    /**
     *  Prints out a verbose message.
     *
     *  @param msg the message
     */
/*
    public void printMessage(String msg)
    {
        if (options.verbose.value) {
            out.println(getI18N("enhancer.message", msg));
        }
    }
*/
}
