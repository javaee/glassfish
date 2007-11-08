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

package com.sun.enterprise.corba.ee.internal.util;

import java.util.logging.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

//QQ. remove unnecessary import
//import com.sun.corba.ee.internal.orbutil.ORBConstants;

import java.util.MissingResourceException;

public class LogWrap {
    
    public static final Logger logger;
    
    private static final String LOGDOMAINS_CLASS_NAME = 
            "com.sun.logging.LogDomains";
    private static final String GET_LOGGER_METHOD_NAME = "getLogger";

    private static final String ORB_LOGGER_FIELD_NAME = "CORBA_LOGGER";
    private static final String ORB_LOGGER_NAME = 
            "javax.enterprise.resource.corba";
    private static final String ORB_LOGGER_RESOURCE_BUNDLE =
            "com.sun.logging.enterprise.resource.corba.LogStrings";
    
    static {
        // use a temp variable so that 'logger' may be 'final'
        Logger tempLogger=null;
        
        try {
            tempLogger = getLoggerUsingLogDomainsAPI();
            if (tempLogger == null) {
                System.out.println("Could not initialize LogDomains logger");
            }
        } catch (Exception ex) {
            System.out.println("Could not initialize LogDomains logger");
        }                

        if (tempLogger == null) {
            try {
                tempLogger = getLoggerUsingJavaLoggingAPI();
                if (tempLogger == null) {
                    System.out.println("Could not initialize JDK logger");
                }
            } catch (Exception ex) {
                System.out.println("Could not initialize JDK logger");
                ex.printStackTrace();
            }
        }
        logger = tempLogger;
    }
    
    private static Logger getLoggerUsingLogDomainsAPI() throws Exception {
        Class logDomainsClass = Class.forName(LOGDOMAINS_CLASS_NAME);

        Class [] parameterClasses = new Class[1];
        parameterClasses[0] = String.class;
        Method getLoggerMethod = logDomainsClass.getDeclaredMethod(
                GET_LOGGER_METHOD_NAME, parameterClasses);

        Field orbLoggerNameField = logDomainsClass.getDeclaredField(
                ORB_LOGGER_FIELD_NAME);
        String orbLoggerName = (String)orbLoggerNameField.get(null);

        Object [] parameters = new Object[1];
        parameters[0] = orbLoggerName;
        Logger lgr = (Logger)getLoggerMethod.invoke(null, parameters);

        return lgr;
    }
    
    private static Logger getLoggerUsingJavaLoggingAPI() throws Exception {
        Logger lgr = Logger.getLogger(ORB_LOGGER_NAME, ORB_LOGGER_RESOURCE_BUNDLE);
        
        return lgr;
    }
    
}
