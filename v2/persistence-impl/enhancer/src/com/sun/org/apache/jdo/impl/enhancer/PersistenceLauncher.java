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

import java.util.Properties;

import java.io.PrintWriter;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import com.sun.org.apache.jdo.impl.enhancer.util.Support;



/**
 * Application launcher for persistence-capable classes.
 *
 * @author Martin Zaun
 */
public class PersistenceLauncher {

    // chose whether to separate or join out and err channels
    //static private final PrintWriter err = new PrintWriter(System.err, true);
    static private final PrintWriter err = new PrintWriter(System.out, true);
    static private final PrintWriter out = new PrintWriter(System.out, true);
    static private final String prefix = "PersistenceLauncher.main() : ";

    /**
     * Creates new PersistenceLauncher.
     */
    private PersistenceLauncher() {
    }

    /**
     * Prints usage message.
     */
    static void usage()
    {
        out.flush();
        err.println("PersistenceLauncher:");
        err.println("    usage: <options> ... <target class name> <args> ...");
        err.println("    options:");
        err.println("           -h | --help");
        err.println("           -n | --noEnhancement");
        err.println("           -q | --quiet");
        err.println("           -w | --warn");
        err.println("           -d | --debug");
        err.println("           -t | --timing");
        err.println("    class names have to be fully qualified");
        err.println("done.");
        err.println();
        err.flush();
    }

    /**
     * Creates a class loader and launches a target class.
     * @param args the command line arguments
     */
    public static void main(String[] args)
        throws ClassNotFoundException,
        NoSuchMethodException,
        SecurityException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException {
/*
        message("property PersistenceExecutor.TAG_REPOSITORY = "
                + System.getProperty("PersistenceExecutor.TAG_REPOSITORY"));
        message("property PersistenceExecutor.TAG_CLASSPATH = "
                + System.getProperty("PersistenceExecutor.TAG_CLASSPATH"));
        message("property PersistenceExecutor.TAG_LIBRARY = "
                + System.getProperty("PersistenceExecutor.TAG_LIBRARY"));
        message("property PersistenceExecutor.TAG_CLASSNAME = "
                + System.getProperty("PersistenceExecutor.TAG_CLASSNAME"));
*/

        // get launcher options
        final String classpath = System.getProperty("java.class.path");
        boolean noEnhancement = false;
        boolean debug = false;
        boolean timing = false;
        Properties enhancerSettings = new Properties();
        String targetClassname = null;
        String[] targetClassArgs = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-h")
                || arg.equals("--help")) {
                usage();

                // exit gently
                return;
            }
            if (arg.equals("-n")
                || arg.equals("--noEnhancement")) {
                noEnhancement = false;
                continue;
            }
            if (arg.equals("-t")
                || arg.equals("--timing")) {
                timing = true;
                enhancerSettings.setProperty(EnhancerClassLoader.DO_TIMING_STATISTICS,
                                             "true");
                continue;
            }
            if (arg.equals("-d")
                || arg.equals("--debug")) {
                debug = true;
                enhancerSettings.setProperty(EnhancerClassLoader.VERBOSE_LEVEL,
                                             EnhancerClassLoader.VERBOSE_LEVEL_DEBUG);
                continue;
            }
            if (arg.equals("-w")
                || arg.equals("--warn")) {
                debug = false;
                enhancerSettings.setProperty(EnhancerClassLoader.VERBOSE_LEVEL,
                                             EnhancerClassLoader.VERBOSE_LEVEL_WARN);
                continue;
            }
            if (arg.equals("-q")
                || arg.equals("--quiet")) {
                debug = false;
                enhancerSettings.setProperty(EnhancerClassLoader.VERBOSE_LEVEL,
                                             EnhancerClassLoader.VERBOSE_LEVEL_QUIET);
                continue;
            }

            // get target class name
            targetClassname = arg;

            // copy remaining arguments and leave loop
            i++;
            final int length = args.length - i;
            targetClassArgs = new String[length];
            System.arraycopy(args, i, targetClassArgs, 0, length);
            break;
        }

        // debugging oputput
        if (debug) {
            out.println(prefix + "...");
            out.println("settings and arguments:");
            out.println("    classpath = " + classpath);
            out.println("    noEnhancement = " + noEnhancement);
            out.println("    debug = " + debug);
            out.println("    enhancerSettings = {");
            enhancerSettings.list(out);
            out.println("    }");
            out.println("    targetClassname = " + targetClassname);
            out.print("    targetClassArgs = { ");
            for (int i = 0; i < targetClassArgs.length; i++) {
                out.print(targetClassArgs[i] + " ");
            }
            out.println("}");
        }

        // check options
        if (targetClassname == null) {
            usage();
            throw new IllegalArgumentException("targetClassname == null");
        }

        // get class loader
        final ClassLoader loader;
        if (noEnhancement) {
            if (debug) {
                out.println(prefix + "using system class loader");
            }
            //out.println("using system class loader");
            loader = PersistenceLauncher.class.getClassLoader();
        } else {
            if (debug) {
                out.println(prefix + "creating enhancer class loader");
            }
            final Properties settings = enhancerSettings;
            final PrintWriter out = PersistenceLauncher.out;
            loader = new EnhancerClassLoader(classpath, settings, out);
        }

        // get target class' main method
        Class clazz;
        Method main;
        try {
            final String mname = "main";
            final Class[] mparams = new Class[]{ String[].class };
            final boolean init = true;
            if (debug) {
                out.println(prefix + "getting method "
                            + targetClassname + "." + mname + "(String[])");
            }
            clazz = Class.forName(targetClassname, init, loader);
            main = clazz.getDeclaredMethod(mname, mparams);
        } catch (ClassNotFoundException e) {
            // log exception only
            if (debug) {
                out.flush();
                err.println("PersistenceLauncher: EXCEPTION SEEN: " + e);
                e.printStackTrace(err);
                err.flush();
            }
            throw e;
        } catch (NoSuchMethodException e) {
            // log exception only
            if (debug) {
                out.flush();
                err.println("PersistenceLauncher: EXCEPTION SEEN: " + e);
                e.printStackTrace(err);
                err.flush();
            }
            throw e;
        } catch (SecurityException e) {
            // log exception only
            if (debug) {
                out.flush();
                err.println("PersistenceLauncher: EXCEPTION SEEN: " + e);
                e.printStackTrace(err);
                err.flush();
            }
            throw e;
        }

        // invoke target class' main method
        try {
            final Object[] margs = new Object[]{ targetClassArgs };
            if (debug) {
                out.println("invoking method " + clazz.getName()
                            + "." + main.getName() + "(String[])");
            }
            main.invoke(null, margs);
        } catch (IllegalAccessException e) {
            // log exception only
            if (debug) {
                out.flush();
                err.println("PersistenceLauncher: EXCEPTION SEEN: " + e);
                e.printStackTrace(err);
                err.flush();
            }
            throw e;
        } catch (IllegalArgumentException e) {
            // log exception only
            if (debug) {
                out.flush();
                err.println("PersistenceLauncher: EXCEPTION SEEN: " + e);
                e.printStackTrace(err);
                err.flush();
            }
            throw e;
        } catch (InvocationTargetException e) {
            // log exception only
            if (debug) {
                out.flush();
                err.println("PersistenceLauncher: EXCEPTION SEEN: " + e);
                e.printStackTrace(err);
                err.flush();
            }
            throw e;
        } finally {
            if (timing) {
                Support.timer.print();
            }
        }

        if (debug) {
            out.println(prefix + "done.");
            out.flush();
            err.flush();
        }
    }
}
