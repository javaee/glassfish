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

/**
 * An interface to abstract out the String Manager used by the JMX connector 
 * client and JMX connector server. 
 */
public interface IStringManager {
    
    /**
     * Returns a localized string.
     * @param    key    the key to the local format string
     * @return   the localized string
     */
    public String getString(String key);
    
   /**
     * Returns a local string for the caller and format the arguments
     * accordingly.
     * @param   key     the key to the local format string
     * @param   arg1    the one argument to be provided to the formatter
     * @return  a formatted localized string
     */
    public String getString(String key, Object arg);

    /**
     * Returns a local string for the caller and format the arguments
     * accordingly.
     *
     * @param   key     the key to the local format string
     * @param   args    the array of arguments to be provided to the formatter
     *
     * @return  a formatted localized string
     */
    public String getString(String key, Object[] args);
    
}

