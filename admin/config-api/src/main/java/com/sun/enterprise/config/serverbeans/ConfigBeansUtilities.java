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

package com.sun.enterprise.config.serverbeans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.List;

/**
 * Bunch of utility methods for the new serverbeans config api based on jaxb
 */
public final class ConfigBeansUtilities {

    
    // static methods only
    private ConfigBeansUtilities() {
    }

    public static <T> List<T> getModules(Class<T> type, Applications apps) {
        List<T> modules = new ArrayList<T>();
        for (Object module : apps.getModules()) {
            if (module.getClass().getName().equals(type.getClass().getName())) {
                modules.add((T) module);
            }
        }
        return modules;
    }

    public static <T> T getModule(Class<T> type, Applications apps, String moduleID) {

        if (moduleID == null) {
            return null;
        }

        for (Object module : apps.getModules()) {
            if (module.getClass().getName().equals(type.getClass().getName())) {
                Method m;
                try {
                    m = type.getMethod("getName");
                } catch (SecurityException ex) {
                    return null;
                } catch (NoSuchMethodException ex) {
                    return null;
                }
                if (m != null) {
                    try {
                        if (moduleID.equals(m.invoke(module))) {
                            return (T) module;
                        }
                    } catch (IllegalArgumentException ex) {
                        return null;
                    } catch (IllegalAccessException ex) {
                        return null;
                    } catch (InvocationTargetException ex) {
                        return null;
                    }
                }
            }
        }
        return null;

    }

    /**
     * Get the default value of Format from dtd
     */
    public static String getDefaultFormat() {
        return "%client.name% %auth-user-name% %datetime% %request% %status% %response.length%".trim();
    }

    /**
     * Get the default value of RotationPolicy from dtd
     */
    public static String getDefaultRotationPolicy() {
        return "time".trim();
    }

    /**
     * Get the default value of RotationEnabled from dtd
     */
    public static String getDefaultRotationEnabled() {
        return "true".trim();
    }

    /**
     * Get the default value of RotationIntervalInMinutes from dtd
     */
    public static String getDefaultRotationIntervalInMinutes() {
        return "1440".trim();
    }

    /**
     * Get the default value of QueueSizeInBytes from dtd
     */
    public static String getDefaultQueueSizeInBytes() {
        return "4096".trim();
    }

    /**
     * This method is used to convert a string value to boolean.
     *
     * @return true if the value is one of true, on, yes, 1. Note
     *         that the values are case sensitive. If it is not one of these
     *         values, then, it returns false. A finest message is printed if
     *         the value is null or a info message if the values are
     *         wrong cases for valid true values.
     */
    public static boolean toBoolean(final String value) {
        final String v = (null != value ? value.trim() : value);
        return null != v && (v.equals("true")
                || v.equals("yes")
                || v.equals("on")
                || v.equals("1"));
    }
    
    /** Returns the list of system-applications that are referenced from the given server.
     *  A server references an application, if the server has an element named
     *  &lt;application-ref> in it that points to given application. The given server
     *  is a &lt;server> element inside domain.
     *  
     * @param sn the string denoting name of the server
     * @return List of system-applications for that server, an empty list in case there is none
     */
    public static List<Application> getSystemApplicationsReferencedFrom(Domain d, String sn) {
        if (d == null || sn == null)
            throw new IllegalArgumentException("Null argument");
        List<Application> allApps = getAllDefinedSystemApplications(d);
        if (allApps.size() == 0)
            return (allApps); //if there are no sys-apps, none can reference one :)
        //allApps now contains ALL the system applications
        Server s = getServerNamed(sn, d);
        List<Application> referencedApps = new ArrayList<Application>();
        List<ApplicationRef> appsReferenced = s.getApplicationRef();
        for (ApplicationRef ref : appsReferenced) {
            for (Application app : allApps) {
                if (ref.getRef().equals(app.getName())) {
                    referencedApps.add(app);
                }
            }
        }
        return ( referencedApps );
    }
    
    public static Application getSystemApplicationReferencedFrom(Domain d, String sn, String appName) {
        //returns null in case there is none
        List<Application> allApps = getSystemApplicationsReferencedFrom(d, sn);
        for (Application app : allApps) {
            if (app.getName().equals(appName)) {
                return ( app );
            }
        }
        return ( null );
    }
    public static boolean isNamedSystemApplicationReferencedFrom(Domain d, String appName, String serverName) {
        List <Application> referencedApps = getSystemApplicationsReferencedFrom(d, serverName);
        for (Application app : referencedApps) {
            if (app.getName().equals(appName))
                return ( true );
        }
        return ( false );
    }
    
    public static Server getServerNamed(String name, Domain d) {
        if (d == null || d.getServers() == null || name == null)
            throw new IllegalArgumentException ("Either domain is null or no <servers> element");
        List<Server> servers = d.getServers().getServer();
        for (Server s : servers) {
            if (name.equals(s.getName().trim())) {
                return ( s );
            }
        }
        return ( null );
    }
    
    public static List<Application> getAllDefinedSystemApplications(Domain d) {
        List<Application> allSysApps = new ArrayList<Application>();
        SystemApplications sa = d.getSystemApplications();
        if (sa != null) {
            for (Module m : sa.getModules()) {
                if (m instanceof Application)
                    allSysApps.add((Application)m);
            }
        }
        return ( allSysApps );
    }
    
    public static ApplicationRef getApplicationRefInServer(Domain d, String sn, String name) {
        Servers ss = d.getServers();
        List<Server> list = ss.getServer();
        Server theServer = null;
        for (Server s : list) {
            if (s.getName().equals(sn)) {
                theServer = s;
                break;
            }
        }
        ApplicationRef aref = null;
        if (theServer != null) {
            List <ApplicationRef> arefs = theServer.getApplicationRef();
            for (ApplicationRef ar : arefs) {
                if (ar.getRef().equals(name)) {
                    aref = ar;
                    break;
                }
            }
        }
        return ( aref );
    }
}


