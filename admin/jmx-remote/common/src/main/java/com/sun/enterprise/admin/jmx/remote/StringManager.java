/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.admin.jmx.remote;

import com.sun.enterprise.admin.jmx.remote.IStringManager;

import java.lang.reflect.Method;
//import java.lang.reflect.Constructor;
import java.util.logging.Logger;

/**
 * This class acts as an adapter to use the appserver internal StringManager. 
 * It implements the IStringManager interface that is defined for an 
 * jmx-connector string-manager. At the same time it composes a delegatee
 * com.sun.enterprise.util.i18n.StringManager object.
 * The method invocation on this delegatee is through reflection to avoid any 
 * compile time dependencies
 */
public class StringManager implements IStringManager {
    
    private static final Logger logger = Logger.getLogger(
        DefaultConfiguration.JMXCONNECTOR_LOGGER);/*, 
        DefaultConfiguration.LOGGER_RESOURCE_BUNDLE_NAME );*/
    
    private static Class asStringManagerClass = null;
    private static Method getStr = null;
    private static Method getStrDef = null;
    private static Method getStrGeneric = null;
    private static Method getManager = null;
    
    //this is the composed AS9.0 internal default StringManager
    private Object asStringManager = null;
    
    public StringManager(String packageName) {
        try {
            if (asStringManagerClass == null) {
                asStringManagerClass = Class.forName("com.sun.enterprise.util.i18n.StringManager");
                getStr = asStringManagerClass.getMethod("getString", new Class[] { String.class});
                getStrDef = asStringManagerClass.getMethod("getString", new Class[] { String.class, Object.class });
                getStrGeneric = asStringManagerClass.getMethod("getString", new Class[] { String.class, Object[].class });
                getManager = asStringManagerClass.getMethod("getManager", new Class[] { String.class });
            }
            
            asStringManager = getManager.invoke(asStringManagerClass, new Object[] { packageName });
        } catch (Throwable e) {
            e.printStackTrace();
            StackTraceElement[] ste = e.getStackTrace();
            for (int i =0; i<ste.length; i++) logger.severe(ste[i].toString());
            logger.severe("StringManager could not be configured");
        }
    }

    public String getString(String key) {
        try {
            return (String) getStr.invoke(asStringManager, new Object[] { key });
        } catch(Exception ex) {
            logger.severe("Method invocation failed on com.sun.enterprise.util.i18n.StringManager");
            return null;
        }
    }
    
    public String getString(String key, Object arg) {
        try {
            return (String) getStrDef.invoke(asStringManager, new Object[] { key, arg });
        } catch(Exception ex) {
            logger.severe("Method invocation failed on com.sun.enterprise.util.i18n.StringManager");
            return null;
        }
    }
    
    public String getString(String key, Object[] args) {
        try {
            return (String) getStrGeneric.invoke(asStringManager, new Object[] { key, args });
        } catch(Exception ex) {
            logger.severe("Method invocation failed on com.sun.enterprise.util.i18n.StringManager");
            return null;
        }
    }    
}
