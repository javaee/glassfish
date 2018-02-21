/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import lib.LibClass;

import java.net.URLClassLoader;
import java.net.URL;



/**
 *
 * @author tjquinn
 */
public class Client {
    
    private Vector<String> otherArgs = new Vector<String>();
    
    private Map<String,String> optionValues = new HashMap<String,String>();
    
    /** Creates a new instance of ShowArgsClient */
    public Client() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int status = 1;
        try {
            new Client().run(args);
            status = 0;
        } catch (Throwable thr) {
            status = 1;
            throw new RuntimeException("Client detected the following error", thr);
        }
        
    }
    
    private void run(String[] args) throws FileNotFoundException, IOException {
        System.err.println("In Client");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        while (cl != null) {
            System.err.println("CL " + cl.toString());
            if (cl instanceof URLClassLoader) {
                System.err.println("  URLs for this loader:");
                for (URL url : ((URLClassLoader)cl).getURLs()) {
                    System.err.println("  " + url.toString());
                }
            }
            cl = cl.getParent();
        }
        final LibClass lc = new LibClass();
        System.out.println("Command line arguments:");
        for (String arg : args) {
            System.out.println(arg);
        }
        System.out.println();
        prepareArguments(args);
        
        System.out.println("Command line arguments:");
        for (String arg : args) {
            System.out.println(arg);
        }
        
        System.out.flush();
    }

    
    private void prepareArguments(String[] args) throws IllegalArgumentException, FileNotFoundException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                optionValues.put(args[i].substring(1), args[++i]);
            } else {
                otherArgs.add(args[i]);
            }
        }

        System.out.println("Other arguments: " + otherArgs);
        
    }
}
