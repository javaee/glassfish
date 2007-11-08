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

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.util;

/**
 * Helper class providing string utility methods.
 */
public class StringHelper {

    /**
     * Checks if a string is null or empty.
     * @param aString the string to be checked.
     * @return <code>true</code> if the string is null or empty after trim,
     *         <code>false</code> otherwise.
     */
    public static boolean isEmpty(String aString) {
        return ((aString == null) || (aString.trim().length() == 0));
    }

    /**
     * Returns the package portion of the specified class
     * @param className the name of the class from which to extract the package
     * @return package portion of the specified class
     */
    public static String getPackageName(final String className) {
        if (className != null) {
            final int index = className.lastIndexOf('.');

            return ((index != -1) ? className.substring(0, index) : ""); // NOI18N
        }

        return null;
    }

    /**
     * Returns the name of a class without the package name.  For example: if
     * input = "java.lang.Object" , then output = "Object".
     * @param fully qualified classname
     * @return the unqualified classname 
     */
    public static String getShortClassName(final String className) {
        if (className != null) {
            final int index = className.lastIndexOf('.');

            return className.substring(index + 1);
        }
        return null;
    }
}
