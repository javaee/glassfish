/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

package com.sun.enterprise.config.serverbeans.validation.tests;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import com.sun.enterprise.config.serverbeans.validation.Result;
import com.sun.enterprise.config.ConfigContext;

import javax.xml.parsers.*;
import org.xml.sax.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import java.lang.StringBuffer;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import java.net.UnknownHostException;
import java.net.InetAddress;





/* Utility functions
 * Author : srini@sun.com
 **/ 

public class StaticTest {
    
    /* Adding various constants to this file. This should remove the hard coded values that
     * we are using in various tests.
     */
    
    // Represents the ADD configbean request
    public static final String ADD = "ADD";
    // Represents the DELETE configbean request
    public static final String DELETE = "DELETE";
    // Represents the UPDATE configbean request
    public static final String UPDATE = "UPDATE";
    // Represents the SET configbean request
    public static final String SET = "SET";
    // Represents the VALIDATE configbean request
    public static final String VALIDATE = "VALIDATE";
    
    // flag to check for file existence
    public static boolean fileCheck = false;
    // flag to check for classpath existence
    public static boolean classPathCheck = false;
    // flag to check for javaHome existence
    public static boolean javaHomeCheck = false;
    
    public StaticTest() {
    }
    

        /**
         * Check the IP address syntax of the given address. If its
         * invalid throw UnknownHostException. Ignore the address if
         * its a token
         */
    public static void checkIPAddress(final String addr) {
        if (valueContainsTokenExpression (addr) || validSymbolicAddress(addr)) {
            return;
        /* Several problems because of DNS lookup. It's generally not a good idea
           to validate this. Hence, I am commenting the else clause out, km@dev.java.net, 07/25/2007
        /*} else {
            InetAddress.getByName(addr);            
        */}
        if (!allAscii(addr)) {
            throw new IllegalArgumentException("dummy message");
        }
    }

    private static boolean allAscii(String s) {
        //s is already checked for nullness.
       // leveraging the JDK 1.6 java.net.IDN.isASCII(String)
       boolean isASCII = true;
       for (int i = 0; i < s.length(); i++) {
           int c = s.charAt(i);
           if (c > 0x7F) {
               isASCII = false;
               break;
           }
       }
       return isASCII;
    }
        /**
           Indicate if the given address is a symbolic
           address. i.e. is "any", "inaddr_any" or "localhost", in any case.
         */
    private static boolean validSymbolicAddress(final String address){
        return address.equalsIgnoreCase("ANY") ||
        address.equalsIgnoreCase("INADDR_ANY") || 
        address.equalsIgnoreCase("localhost");
    }
    
        
    // Method to get the reference to config 
    public static Config getConfig(ConfigContext context) {
        Config mConfig=null;
        try {
            mConfig = ServerBeansFactory.getConfigBean(context);
        } catch(Exception e) {
        }
        return mConfig;
    }
    
    public static boolean isOptionsValid(String options) {
        boolean flag = true;
        StringTokenizer token = new StringTokenizer(options," ");
        while(token.hasMoreTokens())  {
              if(!token.nextToken().startsWith("-")) {
                    flag = false;
                    break;
              }
        }
        return flag;
    }
    
    public static boolean isClassPathValid(String path) {
        boolean flag = true;
        StringTokenizer token = new StringTokenizer(path, File.pathSeparator);
        while(token.hasMoreTokens()) {
            File f = new File(token.nextToken());
            // User requires to validate the file path check for existence, else blank check
            if(classPathCheck) {
                if(!f.exists()) {
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }
    
    /**
     * Verifies that the given path is a valid java-home path.
     *
     * @param path The java-home path to be validated.
     * @return boolean true if valid false if invalid
     */
    public static boolean isJavaHomeValid(String path) {
        boolean flag = true;
        if(javaHomeCheck) {
            String jdkPath = path + File.separator + "bin" + File.separatorChar;
            if(System.getProperty("os.name").startsWith("Win")) {
                jdkPath = jdkPath + "java.exe";
            } else {
                jdkPath = jdkPath + "java";
            }
            StringTokenizer token = new StringTokenizer(path, File.pathSeparator);
            while(token.hasMoreTokens()) {
                File f = new File(token.nextToken());
                // User requires to validate the file path check for existence, else blank check
                if(!f.exists()) {
                    flag = false;
                    break;
                }
            }        
        }
        return flag;
    }
    

    public static Vector tokens(String value) {
        StringTokenizer token = new StringTokenizer(value,",");
        Vector test = new Vector();
        while(token.hasMoreTokens()) 
                  test.add(token.nextToken().trim());
        return test;
    }

    public static boolean isIdInList(String list, String poolId)
    {
        return null != poolId && tokens(list+"").contains(poolId);
    }
    

    
    static void setJavaHomeCheck(boolean check) {
        javaHomeCheck = true;
    }
    
    public static boolean valueContainsTokenExpression(final Object value){
        return null != value && value instanceof String && token_pattern.matcher((String) value).lookingAt();
    }
    
    private static Pattern token_pattern = Pattern.compile("\\$\\{[^}]*}");

        /**
           Name of config reserved for DAS server
        */
    public static final String DAS_CONFIG_NAME = "server-config";

        /**
           Name of config used as a template
         */
    public static final String CONFIG_TEMPLATE_NAME = "default-config";
    

}
