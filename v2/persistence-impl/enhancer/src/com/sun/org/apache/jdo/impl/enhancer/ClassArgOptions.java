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

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;


/**
 * Set of options used by the JDO enhancer and its test programs.
 *
 * @author Martin Zaun
 */
public class ClassArgOptions
    extends GenericOptions
{
    /**
     * Tests if a filename has suffix <code>".class"</code> (ignoring case).
     *
     * @param  filename  the name of the file
     * @return true if filename has a class file suffix
     */
    static private boolean isClassFileName(String filename)
    {
        return filename.toLowerCase().endsWith(".class");
    }

    /**
     * Tests if a filename has suffix <code>".jar"</code> or
     * <code>".zip"</code> (ignoring case).
     *
     * @param  filename  the name of the file
     * @return true if filename has an archive file suffix
     */
    static private boolean isArchiveFileName(String filename)
    {
        final String s = filename.toLowerCase();
        return (s.endsWith(".jar") || s.endsWith(".zip"));
    }

    // ----------------------------------------------------------------------

    /**
     * The source path option.
     */
    public final StringOption sourcePath
        = createStringOption("sourcepath", "s",
                             "<path>  : path for lookup of class files");

    /**
     * The list of class name arguments.
     */
    public final List classNames = new ArrayList();        

    /**
     * The list of class file name arguments.
     */
    public final List classFileNames = new ArrayList();        

    /**
     * The list of archive file name arguments.
     */
    public final List archiveFileNames = new ArrayList();        

    /**
     * Creates an instance.
     */
    public ClassArgOptions(PrintWriter out,
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
                   + "-s <path>   <classname>..");
        printlnErr(indent
                   + "            <classfile>..");
        //^olsen: re-enable support for archive files
        //printlnErr(indent
        //           + "            <archivefile>..");
    }

    /**
     * Print a usage message to System.err.
     */
    public void printArgumentUsage()
    {
        printlnErr(indent
                   + "<classname>       the fully qualified name of a Java class");
        printlnErr(indent
                   + "<classfile>       the name of a .class file");
        printlnErr(indent
                   + "<archivefile>     the name of a .zip or .jar file");
    }

    /**
     * Print arguments.
     */
    public void printArguments()
    {
        println();
        println(argumentsHeader);
        printListArgument("classNames", classNames);
        printListArgument("classFileNames", classFileNames);
        printListArgument("archiveFileNames", archiveFileNames);
    }
    
    /**
     * Print argument of list type.
     */
    public void printListArgument(String name, List list)
    {
        print(indent);
        final StringBuffer s = new StringBuffer();
        for (Iterator i = list.iterator(); i.hasNext();) {
            s.append(" " + i.next());
        }
        println(name + " = {" + s.toString() + " }");
        println();
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

        // group input file arguments
        for (Iterator names = arguments.iterator(); names.hasNext();) {
            final String name = (String)names.next();
            if (isClassFileName(name)) {
                classFileNames.add(name);
            } else if (isArchiveFileName(name)) {
                archiveFileNames.add(name);
            } else {
                classNames.add(name);
            }
        }

        if (verbose.value) {
            printAll();
        }
        
        // check class arguments
        final int argTypes = ((classNames.isEmpty() ? 0 : 1)
                              + (classFileNames.isEmpty() ? 0 : 1)
                              + (archiveFileNames.isEmpty() ? 0 : 1));
        if (argTypes == 0) {
            printUsageError("No class arguments: specify classes either by class name, class file, or archive file");
            return USAGE_ERROR;
        }
        if (argTypes > 1) {
            printUsageError("Mixed class arguments: specify classes by either class name, class file, or archive file");
            return USAGE_ERROR;
        }
        
        // check sourcepath option
        if (sourcePath.value == null && !classNames.isEmpty()) {
            printUsageError("No source-path specified for lookup of classes");
            return USAGE_ERROR;
        }
        if (sourcePath.value != null && classNames.isEmpty()) {
            printUsageError("No source-path can be specified with class or archive files");
            return USAGE_ERROR;
        }

        //^olsen: re-enable support for archive files
        if (!archiveFileNames.isEmpty()) {
            printUsageError("Sorry, support for archive files currently disabled");
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
        out.println("--> ClassArgOptions.main()");
        final ClassArgOptions options = new ClassArgOptions(out, out);
        out.println("    options.process() ...");
        int res = options.process(args);
        out.println("    return value: " + res);
        out.println("<-- ClassArgOptions.main()");
    }
}
