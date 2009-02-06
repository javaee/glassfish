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
import com.sun.enterprise.admin.jmx.remote.StringManager;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;
import java.util.Hashtable;
import java.util.Map;

public class StringManagerFactory {

    /** cache for all the local string managers (per pkg) */
    private static Hashtable managers = new Hashtable();
    
    private static final Logger logger = Logger.getLogger(
        DefaultConfiguration.JMXCONNECTOR_LOGGER);/*, 
        DefaultConfiguration.LOGGER_RESOURCE_BUNDLE_NAME );
    
    /**
     * packageName stands for the input fully qualified class name against which 
     * a string manager is stored
     */
    public static IStringManager getServerStringManager(String packageName) {
        String stringMgrClassName = System.getProperty(DefaultConfiguration.STRING_MANAGER_CLASS_NAME);
        return getManager(packageName, stringMgrClassName);
    }

    public static IStringManager getClientStringManager(String packageName, Map env) {
        String stringMgrClassName = null;
        if (env != null) 
            stringMgrClassName = (String)env.get(DefaultConfiguration.STRING_MANAGER_CLASS_NAME);
        return getManager(packageName, stringMgrClassName);
    }

    /**
     * inputClass is the Class against whose package a string manager is stored
     */
    public static IStringManager getServerStringManager(Class inputClass) {
        String packageName = inputClass.getPackage().getName();
        String stringMgrClassName = System.getProperty(DefaultConfiguration.STRING_MANAGER_CLASS_NAME);
        return getManager(packageName, stringMgrClassName);
    }

    public static IStringManager getClientStringManager(Class inputClass, Map env) {
        String packageName = inputClass.getPackage().getName();
        String stringMgrClassName = null;
        if (env != null) 
            stringMgrClassName = (String)env.get(DefaultConfiguration.STRING_MANAGER_CLASS_NAME);
        return getManager(packageName, stringMgrClassName);
    }

    public static IStringManager getManager(String packageName, String stringMgrClassName) {
        
        IStringManager mgr = (IStringManager) managers.get(packageName);
        
        if (mgr != null) return mgr;
        
        if (stringMgrClassName == null) mgr = new StringManager(packageName);
        else {
            try {
                Class customClass = Class.forName(stringMgrClassName);
                Constructor constructor = 
                    customClass.getConstructor(new Class[] { String.class });
                mgr = (IStringManager) constructor.newInstance(new Object[] {packageName});
            } catch (Exception e) {
                logger.severe("StringManager could not be configured");
            }
        }
        
        if (mgr != null) managers.put(packageName, mgr);
        else logger.severe("Custom StringManager Class could not be instantiated");
        
        return mgr;
    }
}