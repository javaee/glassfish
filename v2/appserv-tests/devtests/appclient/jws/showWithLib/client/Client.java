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
