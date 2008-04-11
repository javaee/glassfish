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
        return "%client.name% %auth-user-name% %datetime% %request% %status% %response.length%";
    }

    /**
     * Get the default value of RotationPolicy from dtd
     */
    public static String getDefaultRotationPolicy() {
        return "time";
    }

    /**
     * Get the default value of RotationEnabled from dtd
     */
    public static String getDefaultRotationEnabled() {
        return "true";
    }

    /**
     * Get the default value of RotationIntervalInMinutes from dtd
     */
    public static String getDefaultRotationIntervalInMinutes() {
        return "1440";
    }

    /**
     * Get the default value of QueueSizeInBytes from dtd
     */
    public static String getDefaultQueueSizeInBytes() {
        return "4096";
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

}


