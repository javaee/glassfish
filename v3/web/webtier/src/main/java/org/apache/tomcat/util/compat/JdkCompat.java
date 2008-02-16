

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package org.apache.tomcat.util.compat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

/**
 *  General-purpose utility to provide backward-compatibility and JDK
 *  independence. This allow use of JDK1.3 ( or higher ) facilities if
 *  available, while maintaining the code compatible with older VMs.
 *
 *  The goal is to make backward-compatiblity reasonably easy.
 *
 *  The base class supports JDK1.3 behavior.
 *
 *  @author Tim Funk
 */
public class JdkCompat {

    // ------------------------------------------------------- Static Variables

    /**
     * class providing java2 support
     */
    static final String JAVA14_SUPPORT =
        "org.apache.tomcat.util.compat.Jdk14Compat";

    /** Return java version as a string
     */
    public static String getJavaVersion() {
        return javaVersion;
    }

    public static boolean isJava2() {
        return java2;
    } 
   
    public static boolean isJava14() {
        return java14;
    }

    // -------------------- Implementation --------------------
    
    // from ant
    public static final String JAVA_1_0 = "1.0";
    public static final String JAVA_1_1 = "1.1";
    public static final String JAVA_1_2 = "1.2";
    public static final String JAVA_1_3 = "1.3";
    public static final String JAVA_1_4 = "1.4";

    static String javaVersion;
    static boolean java2=false;
    static boolean java14=false;
    static JdkCompat jdkCompat;
    
    static {
        init();
    }

    private static void init() {
        try {
            javaVersion = JAVA_1_0;
            Class.forName("java.lang.Void");
            javaVersion = JAVA_1_1;
            Class.forName("java.lang.ThreadLocal");
            java2=true;
            javaVersion = JAVA_1_2;
            Class.forName("java.lang.StrictMath");
            javaVersion = JAVA_1_3;
            Class.forName("java.lang.CharSequence");
            javaVersion = JAVA_1_4;
            java14=true;
        } catch (ClassNotFoundException cnfe) {
            // swallow as we've hit the max class version that we have
        }
        if( java14 ) {
            try {
                Class c=Class.forName(JAVA14_SUPPORT);
                jdkCompat=(JdkCompat)c.newInstance();
            } catch( Exception ex ) {
                jdkCompat=new JdkCompat();
            }
        } else {
            jdkCompat=new JdkCompat();
            // Install jar handler if none installed
        }
    }

    // ----------------------------------------------------------- Constructors
    /**
     *  Default no-arg constructor
     */
    protected JdkCompat() {
    }


    // --------------------------------------------------------- Public Methods
    /**
     * Get a compatibiliy helper class.
     */
    public static JdkCompat getJdkCompat() {
        return jdkCompat;
    }

    /**
     *  Return the URI for the given file. Originally created for
     *  o.a.c.loader.WebappClassLoader
     *
     *  @param File to wrap into URI
     *  @return A URI as a URL
     */
    public URL getURI(File file)
        throws MalformedURLException {

        File realFile = file;
        try {
            realFile = realFile.getCanonicalFile();
        } catch (IOException e) {
            // Ignore
        }

        return realFile.toURL();
    }


    /**
     *  Return the maximum amount of memory the JVM will attempt to use.
     */
    public long getMaxMemory() {
        return (-1L);
    }


    /**
     * Print out a partial servlet stack trace (truncating at the last 
     * occurrence of javax.servlet.).
     */
    public String getPartialServletStackTrace(Throwable t) {
        StringWriter stackTrace = new StringWriter();
        t.printStackTrace(new PrintWriter(stackTrace));
        String st = stackTrace.toString();
        int i = st.lastIndexOf
            ("org.apache.catalina.core.ApplicationFilterChain.internalDoFilter");
        if (i > -1) {
            return st.substring(0, i - 4);
        } else {
            return st;
        }
    }

    /**
     * Splits a string into it's components.
     * @param path String to split
     * @param pat Pattern to split at
     * @return the components of the path
     */
    public  String [] split(String path, String pat) {
        Vector comps = new Vector();
        int pos = path.indexOf(pat);
        int start = 0;
        while( pos >= 0 ) {
            if(pos > start ) {
                String comp = path.substring(start,pos);
                comps.add(comp);
            }
            start = pos + pat.length();
            pos = path.indexOf(pat,start);
        }
        if( start < path.length()) {
            comps.add(path.substring(start));
        }
        String [] result = new String[comps.size()];
        for(int i=0; i < comps.size(); i++) {
            result[i] = (String)comps.elementAt(i);
        }
        return result;
    }

 }
