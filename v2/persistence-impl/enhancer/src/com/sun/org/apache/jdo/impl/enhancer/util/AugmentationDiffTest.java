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

import com.sun.org.apache.jdo.enhancer.classfile.ClassInfo;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfoFactory;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfoFactoryFactory;
import com.sun.org.apache.jdo.enhancer.classfile.MethodInfo;


/**
 * Utility class for testing two class files for equal augmentation.
 *
 * @author Martin Zaun
 */
public class AugmentationDiffTest
{
    // return values of main()
    static public final int OK = 0;
    static public final int USAGE_ERROR = -1;
    static public final int INTERNAL_ERROR = -3;

    // return values of internal test methods
    static public final int AFFIRMATIVE = 1;
    static public final int NEGATIVE = 0;
    static public final int ERROR = -1;

    // output streams
    static private boolean debug = false;
    static private final PrintWriter out = new PrintWriter(System.out, true);
    static private final PrintWriter err = new PrintWriter(System.err, true);

    static final void affirm(boolean cond)
    {
        if (debug && !cond)
            //^olsen: throw AssertionException ?
            throw new RuntimeException("Assertion failed.");
    }

    static final void affirm(Object obj)
    {
        if (debug && obj == null)
            //^olsen: throw AssertionException ?
            throw new RuntimeException("Assertion failed: obj = null");
    }

    static private InputStream openFileInputStream(String fileName)
        throws FileNotFoundException
    {
     	return new BufferedInputStream(new FileInputStream(fileName));
    }

    static private void closeInputStream(InputStream in)
    {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ex) {
                err.println("Exception caught: " + ex);
            }
        }
    }

    // ----------------------------------------------------------------------

    private boolean verbose;
    private String[] classFileNames;
    private String[] classNames;
    private String[] userClassNames;
    private ClassInfo[] classInfos;

    public AugmentationDiffTest()
    {}

    private int diffAugmentation(PrintWriter out)
    {
        affirm(ERROR < NEGATIVE && NEGATIVE < AFFIRMATIVE);
        affirm(classInfos.length == 2);
        affirm(classNames.length == 2);

        int res = NEGATIVE;

        final Map[] classMethods = { new HashMap(), new HashMap() };
        for (int i = 0; i < 2; i++) {
            for (MethodInfo method : classInfos[i].methods()) {
                final String methodSig = method.getDescriptor();
                final String methodArgs = method.getArgumentsAsJavaString();
                final String methodName = method.getName();
                if (methodName.startsWith("jdo")) {
                //if (methodName.equals("jdoReplaceField")) {
                    final Object obj
                        = classMethods[i].put(methodName + methodArgs, method);
                    affirm(obj == null);
                }
            }
        }
        
        final Set keySet = new HashSet();
        keySet.addAll(classMethods[0].keySet());
        keySet.addAll(classMethods[1].keySet());
        for (Iterator i = keySet.iterator(); i.hasNext();) {
            final Object key = i.next();

            final MethodInfo method0
                = (MethodInfo)classMethods[0].remove(key);
            final MethodInfo method1
                = (MethodInfo)classMethods[1].remove(key);
            affirm(method0 != method1);
            affirm(method0 != null || method1 != null);
            
            if (method0 == null || method1 == null) {
                out.println("    !!! ERROR: missing method: " + key);
                out.println("        [1]<<< method: " + method0);
                out.println("        [2]>>> method: " + method1);
                res = ERROR;
                continue;
            }

            final Stack<String> msg = new Stack<String>();
            if (method0.isEqual(msg, method1)) {
                if (verbose) {
                    out.println("    +++ equal augmentation: " + key);
                }
            } else {
                out.println("    !!! not equal augmentation: " + key);
                msg.push("method = " + method1);
                msg.push("method = " + method0);
                final StringWriter s0 = new StringWriter();
                final StringWriter s1 = new StringWriter();
                final PrintWriter p0 = new PrintWriter(s0);
                final PrintWriter p1 = new PrintWriter(s1);
                int j = 0;
                while (!msg.empty()) {
                    p0.println("    [1]<<< " + pad(j) + msg.pop());
                    p1.println("    [2]>>> " + pad(j) + msg.pop());
                    j += 4;
                }
                out.println(s0.toString());
                out.println(s1.toString());

                if (verbose) {
                    ByteArrayOutputStream b0 = new ByteArrayOutputStream();
                    ByteArrayOutputStream b1 = new ByteArrayOutputStream();
                    method0.print(new PrintStream(b0), 4);
                    method1.print(new PrintStream(b1), 4);
                    out.println(b0.toString());
                    out.println(b1.toString());
                    if (res == NEGATIVE) {
                        res = AFFIRMATIVE;
                    }
                }
                break;
            }
        }

        return res;
    }

    static private String pad(int n) 
    {
        final StringBuffer s = new StringBuffer();
        for (int i = 0; i < n; i++) {
            s.append(' ');
        }
        return s.toString();
    }

    private int parseClass(PrintWriter out,
                           int i)
    {
        affirm(0 <= i && i <= 1);
        affirm(classFileNames.length == 2);
        affirm(classInfos.length == 2);
        affirm(classNames.length == 2);
        affirm(userClassNames.length == 2);
        final String fileName = classFileNames[i];
        
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

        DataInputStream dis = null;
        try {
            // create class file
            dis = new DataInputStream(openFileInputStream(fileName));
            classInfos[i] = factory.createClassInfo(dis);

            // get real class name
            classNames[i] = classInfos[i].toJavaName();
            userClassNames[i] = classNames[i].replace('/', '.');
            out.println("    +++ parsed classfile");
        } catch (ClassFormatError ex) {
            out.println("    !!! ERROR: format error when parsing classfile: "
                        + fileName);
            out.println("        error: " + err);
            return ERROR;
        } catch (IOException ex) {
            out.println("    !!! ERROR: exception while reading classfile: "
                        + fileName);
            out.println("        exception: " + ex);
            return ERROR;
        } finally {
            closeInputStream(dis);
        }

        return AFFIRMATIVE;
    }

    private int test(PrintWriter out,
                     String[] classFileNames)
    {
        affirm(classFileNames.length == 2);
        this.classFileNames = classFileNames;

        if (verbose) {
            out.println("-------------------------------------------------------------------------------");
            out.println();
            out.println("Test classfiles for equal augmentation: ...");
        }
        
        // check parsing class
        classInfos = new ClassInfo[2];
        classNames = new String[2];
        userClassNames = new String[2];
        for (int i = 0; i < 2; i++) {
            final StringWriter s = new StringWriter();
            if (parseClass(new PrintWriter(s), i) <= NEGATIVE) {
                out.println();
                out.println("!!! ERROR: failed parsing classfile: "
                            + classFileNames[i]);
                out.println(s.toString());
                return ERROR;
            }

            if (verbose) {
                out.println();
                out.println("+++ parsed classfile: " + classFileNames[i]);
                out.println(s.toString());
            }
        }
        
        // check class names
        {
            final StringWriter s = new StringWriter();
            if (!classNames[0].equals(classNames[1])) {
                out.println();
                out.println("!!! ERROR: different class names:");
                out.println("<<< class name = " + userClassNames[0]);
                out.println(">>> class name = " + userClassNames[1]);
                out.println(s.toString());
                return ERROR;
            }
        }
        
        // check for augmentation differences
        final StringWriter s = new StringWriter();
        final int r = diffAugmentation(new PrintWriter(s));
        if (r < NEGATIVE) {
            out.println();
            out.println("!!! ERROR: incorrect augmentation: "
                        + userClassNames[0]);
            out.println(s.toString());
            return ERROR;
        }
        
        if (r == NEGATIVE) {
            out.println();
            out.println("+++ equal augmentation:"
                        + userClassNames[0]);
        } else {
            out.println();
            out.println("!!! not equal augmentation:"
                        + userClassNames[0]);
        }
        if (verbose) {
            out.println(s.toString());
        }

        return r;
    }

    public int test(PrintWriter out,
                    boolean verbose,
                    List classFileNames)
    {
        affirm(classFileNames.size() % 2 == 0);
        this.verbose = verbose;

        out.println();
        out.println("AugmentationDiffTest: Testing Classes for JDO Persistence-Capability Enhancement");

        final int all = classFileNames.size() / 2;
        int nofFailed = 0;
        for (int i = 0; i < all; i++) {
            String name0 = (String)classFileNames.get(i);
            String name1 = (String)classFileNames.get(all + i);
            String[] pair = { name0, name1 };
            if (test(out, pair) != NEGATIVE) {
                nofFailed++;
            }
        }
        final int nofPassed = all - nofFailed;

        out.println();
        out.println("AugmentationDiffTest: Summary:  TESTED: " + all
                    + "  PASSED: " + nofPassed
                    + "  FAILED: " + nofFailed);
        return nofFailed;
    }
    
    // ----------------------------------------------------------------------

    /**
     * Prints usage message.
     */
    static private void usage()
    {
        err.println();
        err.println("Usage: AugmentationDiffTest <options> <classfile1> ... <classfile2> ...");
        err.println();
        err.println("This class pairwise tests if two classes have structurally the same code");
        err.println("enhancement for persistence-capability (\"augmentation\").");
        err.println();
        err.println("Options include:");
        err.println("    -h, --help               print usage");
        err.println("    -v, --verbose            enable verbose output");
        err.println();
        err.println("Return value:");
        err.println("= 0   equally augmented classfiles");
        err.println("> 0   not equally augmented classfiles");
        err.println("< 0   severe errors preventing the test to complete");
        err.println();
    }

    static public void main(String[] argv)
    {
        // parse args
        boolean verbose = false;
        List classFileNames = new ArrayList();
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            if (arg.equals("-h") || arg.equals("--help")) {
                usage();
                System.exit(OK);
            }
            if (arg.equals("-v") || arg.equals("--verbose")) {
                verbose = true;
                continue;
            }
            if (arg.equals("-d") ||
                arg.equals("--debug")) {
                debug = true;
                continue;
            }
            if (arg.startsWith("-")) {
                err.println();
                err.println("Unrecognized option: " + arg);
                usage();
                System.exit(USAGE_ERROR);
            }
            classFileNames.add(arg);
        }

        // check arguments
        if (classFileNames.size() % 2 != 0) {
            err.println();
            err.println("Odd number of classfiles arguments.");
            usage();
            System.exit(USAGE_ERROR);
            return;
        }

        if (debug) {
            out.println("AugmentationDiffTest: options:");
            out.println("    verbose = " + verbose);
            out.print("    classFileNames =");
            for (int i = 0; i < classFileNames.size(); i++)
                out.print(" " + classFileNames.get(i));
            out.println();
        }

        try {
            final AugmentationDiffTest test = new AugmentationDiffTest();
            final int res = test.test(out, verbose, classFileNames);
            System.exit(res);
        } catch (RuntimeException ex) {
            err.println("Internal error;");
            err.println("Exception caught:" + ex);
            ex.printStackTrace(err);
            System.exit(INTERNAL_ERROR);
        }
    }
}
