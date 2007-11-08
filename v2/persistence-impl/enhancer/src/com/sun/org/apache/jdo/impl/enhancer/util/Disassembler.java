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

package com.sun.org.apache.jdo.impl.enhancer.util;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.sun.org.apache.jdo.impl.enhancer.ClassArgMain;
import com.sun.org.apache.jdo.impl.enhancer.EnhancerFatalError;

import com.sun.org.apache.jdo.enhancer.classfile.ClassInfo;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfoFactory;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfoFactoryFactory;

/**
 * Utility class for testing two class files for equal augmentation.
 *
 * @author Martin Zaun
 */
public class Disassembler
    extends ClassArgMain
{
    // return values of internal test methods
    static public final int AFFIRMATIVE = 1;
    static public final int NEGATIVE = 0;
    static public final int ERROR = -1;

    // ----------------------------------------------------------------------

    private boolean verbose;

    public Disassembler(PrintWriter out,
                        PrintWriter err)
    {
        super(out, err);
    }

    private int disassemble(PrintWriter out,
                            String className,
                            String classFileName)
    {
        affirm(className == null ^ classFileName == null);
        final String name = (className != null ? className : classFileName);

        DataInputStream dis = null;
        try {
            if (className != null) {
                dis = new DataInputStream(openClassInputStream(className));
            } else {
                dis = new DataInputStream(openFileInputStream(classFileName));
            }
			
			ClassInfoFactory factory = null;
			try {
				factory = ClassInfoFactoryFactory.getClassInfoFactory();
			} catch (InstantiationException instEx) {
	            out.println("    !!! ERROR: Couldn't get ClassInfoFactory");
				out.println("        error: " + instEx);
				return ERROR;
			} catch (ClassNotFoundException cnfEx) {
	            out.println("    !!! ERROR: Couldn't get ClassInfoFactory");
				out.println("        error: " + cnfEx);
				return ERROR;
			} catch (IllegalAccessException illEx) {
	            out.println("    !!! ERROR: Couldn't get ClassInfoFactory");
				out.println("        error: " + illEx);
				return ERROR;
			}
			
            final ClassInfo classInfo = factory.createClassInfo(dis);
            out.println("    +++ parsed class");

            final ByteArrayOutputStream b = new ByteArrayOutputStream();
            if (verbose) {
				classInfo.print(new PrintWriter(b), 0);
                out.println(b.toString());
            }
            out.println("Statistics:");
			classInfo.summarize(new PrintWriter(b), 4);
            out.println(b.toString());
        } catch (ClassFormatError ex) {
            out.println("    !!! ERROR: format error when parsing class: "
                        + name);
            out.println("        error: " + err);
            return ERROR;
        } catch (IOException ex) {
            out.println("    !!! ERROR: exception while reading class: "
                        + name);
            out.println("        exception: " + ex);
            return ERROR;
        } finally {
            closeInputStream(dis);
        }

        return AFFIRMATIVE;
    }

    protected int disassemble(PrintWriter out,
                              boolean verbose,
                              List classNames,
                              List classFileNames)
    {
        affirm(classNames);
        affirm(classFileNames);
        this.verbose = verbose;

        out.println();
        out.println("Disassembler: Dumps out the java byte-code for classes.");

        int nofFailed = 0;
        final int all = classNames.size() + classFileNames.size();
        for (int i = 0; i < classNames.size(); i++) {
            out.println("-------------------------------------------------------------------------------");
            out.println();
        
            // parse class
            final String className = (String)classNames.get(i);
            final StringWriter s = new StringWriter();
            if (disassemble(new PrintWriter(s), className, null) < NEGATIVE) {
                out.println();
                out.println("!!! ERROR: failed disassembling class: "
                            + className);
                out.println(s.toString());
                nofFailed++;
            }

            out.println("+++ disassembled class: " + className);
            out.println();
            out.println(s.toString());
        }
        for (int i = 0; i < classFileNames.size(); i++) {
            out.println("-------------------------------------------------------------------------------");
            out.println();
        
            // parse class
            final String classFileName = (String)classFileNames.get(i);
            final StringWriter s = new StringWriter();
            if (disassemble(new PrintWriter(s), null, classFileName) < NEGATIVE) {
                out.println();
                out.println("!!! ERROR: failed disassembling class: "
                            + classFileName);
                out.println(s.toString());
                nofFailed++;
            }

            out.println("+++ disassembled class: " + classFileName);
            out.println();
            out.println(s.toString());
        }
        final int nofPassed = all - nofFailed;

        out.println();
        out.println("Disassembler: Summary:  PROCESSED: " + all
                    + "  PASSED: " + nofPassed
                    + "  FAILED: " + nofFailed);
        return nofFailed;
    }
    
    // ----------------------------------------------------------------------

    /**
     * Run the disassembler.
     */
    protected int process()
    {
        //^olsen: to be extended for zip/jar arguments
        return disassemble(out, options.verbose.value,
                           options.classNames, options.classFileNames);
    }

    static public void main(String[] args)
    {
        final PrintWriter out = new PrintWriter(System.out, true);
        out.println("--> Disassembler.main()");
        final Disassembler main = new Disassembler(out, out);
        int res = main.run(args);
        out.println("<-- Disassembler.main(): exit = " + res);
        System.exit(res);
    }
}
