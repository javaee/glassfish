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
 * Set of options used by the JDO enhancer and its test programs.
 *
 * @author Martin Zaun
 */
public class EnhancerOptions
    extends JdoMetaOptions
{
    /**
     * The quiet option.
     */
    public final FlagOption quiet
        = createFlagOption("quiet", "q",
                             "           : suppress warnings");
    /**
     * The force write option.
     */
    public final FlagOption forceWrite
        = createFlagOption("forcewrite", "f",
                           "           : overwrite output files");

    /**
     * The no write option.
     */
    public final FlagOption noWrite
        = createFlagOption("nowrite", "n",
                           "           : never write output files");

    /**
     * The destination directory option.
     */
    public final StringOption destDir
        = createStringOption("destdir", "d",
                             "<path>     : directory for any output files");

    /**
     * The dump class option.
     */
    public final FlagOption dumpClass
        = createFlagOption("dumpclass", null,
                           "           : dump out disassembled byte-code");

    /**
     * The suppress augmentation option.
     */
    public final FlagOption noAugment
        = createFlagOption("noaugment", null,
                           "           : do not enhance for persistence-capability");

    /**
     * The suppress annotation option.
     */
    public final FlagOption noAnnotate
        = createFlagOption("noannotate", null,
                           "           : do not enhance for persistence-awareness");

    /**
     * Creates an instance.
     */
    public EnhancerOptions(PrintWriter out,
                          PrintWriter err) 
    {
        super(out, err);
    }

    // ----------------------------------------------------------------------

    /**
     * Print a usage message to System.err.
     */
    public void printUsageHeader()
    {
        printlnErr("Usage: <options>.. <arguments>..");
        printlnErr(indent
                   + "-j <path> -s <path> -d <dir>   <classname>..");
        printlnErr(indent
                   + "-j <path> -d <dir>             <classfile>..");
        //^olsen: consider allowing omission of destination directory for
        // class file arguments
        //printlnErr(indent
        //           + "-j <path> [-d <dir>]           <classfile>..");
        //^olsen: re-enable support for archive files
        //printlnErr(indent
        //           + "[-j <path>] [-d <dir>]         <archivefile>..");
    }

    /**
     * Check options and arguments.
     */
    public int check()
    {
        int res;
        if ((res = super.check()) != OK) {
            return res;
        }
        
        // check destination directory option
        if (destDir.value == null && !classNames.isEmpty()) {
            printUsageError("No destination directory specified for enhanced classes");
            return USAGE_ERROR;
        }

        //^olsen: consider allowing omission of destination directory for
        // class file arguments
        if (destDir.value == null && !classFileNames.isEmpty()) {
            printUsageError("No destination directory specified for enhanced classes");
            return USAGE_ERROR;
        }

        return OK;
    }

    // ----------------------------------------------------------------------

    /**
     * Tests the class.
     */
    static public void main(String[] args)
    {
        final PrintWriter out = new PrintWriter(System.out, true);
        out.println("--> EnhancerOptions.main()");
        final EnhancerOptions options = new EnhancerOptions(out, out);
        out.println("    options.process() ...");
        int res = options.process(args);
        out.println("    return value: " + res);
        out.println("<-- EnhancerOptions.main()");
    }
}
