/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.jvnet.hk2.generator;

import java.io.File;
import java.io.IOException;

import org.jvnet.hk2.generator.internal.GeneratorRunner;

/**
 * This is a command line (or embedded) utility
 * that will generate habitat files based on
 * &#64;Service annotations.
 * 
 * @author jwells
 *
 */
public class HabitatGenerator {
    private final String directoryOrFileToGenerateFor;
    private final String locatorName;
    private final boolean verbose;
    
    private HabitatGenerator(String directoryOrFileToGenerateFor, String locatorName, boolean verbose) {
        this.directoryOrFileToGenerateFor = directoryOrFileToGenerateFor;
        this.locatorName = locatorName;
        this.verbose = verbose;
    }
    
    private void printThrowable(Throwable th) {
        int lcv = 0;
        while (th != null) {
            System.out.println("Exception level " + lcv++ + " message is \"" +
                th.getMessage() + "\"");
            
            th.printStackTrace();
            
            th = th.getCause();
        }
    }
    
    private int go() {
        GeneratorRunner runner = new GeneratorRunner(directoryOrFileToGenerateFor, locatorName, verbose);
        
        try {
            runner.go();
        }
        catch (AssertionError ae) {
            if (verbose) {
                printThrowable(ae);
            }
            else {
                System.out.println(ae.getMessage());
            }
            return 1;
        }
        catch (IOException io) {
            if (verbose) {
                printThrowable(io);
            }
            else {
                System.out.println(io.getMessage());
            }
            
            return 2;
        }
        
        return 0;
    }
    
    private static void usage() {
        System.out.println("java org.jvnet.hk2.generator.HabitatGenerator [--file jarFileOrDirectory] [--locator locatorName] [--verbose]");
    }
    
    private final static String FILE_ARG = "--file";
    private final static String LOCATOR_ARG = "--locator";
    private final static String VERBOSE_ARG = "--verbose";
    private final static String CLASS_PATH_PROP = "java.class.path";
    private final static String LOCATOR_DEFAULT = "default";
    
    /**
     * A utility to generate inhabitants files.  By default the first element of the classpath will be analyzed and
     * an inhabitants file will be put into the JAR or directory.  The arguments are as follows:
     * <p>
     * HabitatGenerator [--file jarFileOrDirectory] [--locator locatorName] [--verbose]
     * </p>
     * 
     * @param argv The set of command line arguments
     * @return 0 on success, non-zero on failure
     */
    public static int embeddedMain(String argv[]) {
        String defaultFileToHandle = null;
        String defaultLocatorName = LOCATOR_DEFAULT;
        boolean defaultVerbose = false;
        
        for (int lcv = 0; lcv < argv.length; lcv++) {
            if (VERBOSE_ARG.equals(argv[lcv])) {
                defaultVerbose = true;
            }
            else if (FILE_ARG.equals(argv[lcv])) {
                lcv++;
                if (lcv >= argv.length) {
                    usage();
                    return 3;
                }
                
                defaultFileToHandle = argv[lcv];
            }
            else if (LOCATOR_ARG.equals(argv[lcv])) {
                lcv++;
                if (lcv >= argv.length) {
                    usage();
                    return 4;
                }
                
                defaultLocatorName = argv[lcv];
            }
            else {
                System.err.println("Uknown argument: " + argv[lcv]);
            }
        }
        
        if (defaultFileToHandle == null) {
            String cp = System.getProperty(CLASS_PATH_PROP);
            
            int pathSep = cp.indexOf(File.pathSeparator);
            
            String firstInLine;
            if (pathSep < 0) {
                firstInLine = cp;
            }
            else {
                firstInLine = cp.substring(0, pathSep);
            }
            
            defaultFileToHandle = firstInLine;
        }
        
        HabitatGenerator hg = new HabitatGenerator(defaultFileToHandle, defaultLocatorName, defaultVerbose);
        
        return hg.go();
    }
    
    /**
     * This method will call System.exit() with a 0 on success and non-zero on failure
     * 
     * @param argv The arguments to the command (see embeddedMain)
     */
    public static void main(String argv[]) {
        try {
            System.exit(embeddedMain(argv));
        }
        catch (Throwable th) {
            System.exit(-1);
        }
    }

}
