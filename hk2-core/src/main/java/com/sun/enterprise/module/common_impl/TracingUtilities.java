/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2015 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.module.common_impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.sun.enterprise.module.ModulesRegistry;

/**
 * hk2 and modules usage tracing utilities.
 * @author Jerome Dochez
 * 
 */
public class TracingUtilities {

    private final static boolean enabled = Boolean.getBoolean("hk2.module.tracestate");
    
    
    public interface Loader {
        Class loadClass(String type) throws ClassNotFoundException;
    }

    public static boolean isEnabled() {
        return enabled;
    }
    
    public static File getLocation() {
        String location = System.getProperty("hk2.module.loglocation");
        if (location==null) {
            location = System.getProperty("user.dir");
        }
        File f = new File(location);
        if (f.isAbsolute()) {
            return f;
        } else {
            return new File(System.getProperty("user.dir"), location);
        }
    }

    public static void traceResolution(ModulesRegistry registry, long bundleId, String bundleName, Loader loader) {
        traceState(registry, "resolved", bundleId, bundleName, loader);
    }

    public static void traceStarted(ModulesRegistry registry, long bundleId, String bundleName, Loader loader) {
        traceState(registry, "started", bundleId, bundleName, loader);
    }

    public static void traceState(ModulesRegistry registry, String state, long bundleId, String bundleName, Loader loader) {
        File out = new File(getLocation(), state + "-" + bundleId+".log");
        Writer w = null;
        try {
            w = new FileWriter(out);
            w.append("\n");
            w.append("Module ["+ bundleId + "] " + state + " " + bundleName+"\n");
            String prefix="-";
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();

            w.append("\n");
            w.append("-----------------------------------\n");
            w.append("Inhabitants / stack combination\n");
            w.append("-----------------------------------\n");

            String currentBundleName = bundleName;
            
            for (int i=0;i<stack.length;i++) {
                 {
                    // now let's find out the first non hk2 class asking for this...
                    int j=i+1;
                    for (;j<stack.length;j++) {
                        StackTraceElement caller = stack[j];
                        if (!caller.getClassName().contains("hk2")) {
                            break;
                        }
                    }


                }

            }
            w.append("\n");

            w.append("---------------------------\n");
            w.append("Complete thread stack Trace\n");
            w.append("---------------------------\n");

            for (StackTraceElement element : stack) {
                w.append(element.toString()+"\n");
            }


        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (w!=null) {
                try {
                    w.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

    }    
}
