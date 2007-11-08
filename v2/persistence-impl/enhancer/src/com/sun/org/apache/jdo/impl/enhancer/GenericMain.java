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


/**
 * Base class for JDO command line enhancer and tests.
 *
 * @author Martin Zaun
 */
public class GenericMain
    extends LogSupport
{
    // return values for process() method
    static public final int OK = 0;
    static public final int USAGE_ERROR = -1;
    static public final int USER_EXCEPTION = -2;
    static public final int INTERNAL_ERROR = -3;

    /**
     *  The options and arguments.
     */
    protected GenericOptions options;

    /**
     * Creates an instance.
     */
    public GenericMain(PrintWriter out,
                       PrintWriter err)
    {
        this(out, err, new GenericOptions(out, err));
    }

    /**
     * Creates an instance.
     */
    public GenericMain(PrintWriter out,
                       PrintWriter err,
                       GenericOptions options)
    {
        super(out, err);
        this.options = options;
    }

    // ----------------------------------------------------------------------

    /**
     * Initializes all components.
     */
    protected void init()
        throws EnhancerFatalError, EnhancerUserException
    {}
    
    /**
     * Do processing (to be overloaded by subclasses).
     */
    protected int process()
    {
        return OK;
    }
    
    /**
     * Process command line arguments, run initialization and do processing.
     */
    public int run(String[] args)
    {
        try {
            // process passed command-line arguments
            if (options.process(args) != options.OK) {
                return USAGE_ERROR;
            }

            // run initialization and do processing
            init();
            return process();
        } catch (RuntimeException ex) {
            printlnErr("exception caught", ex);
            return INTERNAL_ERROR;
        } catch (Exception ex) {
            printlnErr("exception caught", ex);
            return USER_EXCEPTION;
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Runs this class
     */
    static public void main(String[] args)
    {
        final PrintWriter out = new PrintWriter(System.out, true);
        out.println("--> GenericMain.main()");
        final GenericMain main = new GenericMain(out, out);
        int res = main.run(args);
        out.println("<-- GenericMain.main(): exit = " + res);
        System.exit(res);
    }
}
