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

/*
 * JDORIVersion.java
 *
 * Created on December 1, 2000
 */

package com.sun.org.apache.jdo.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Hashtable;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Helper class to handle properties object with version number and vendor name.
 *
 * @author Marina Vatkina
 */ 
public class JDORIVersion {
    private static Properties _properties	= null;
    private final static String default_bundle  = "com.sun.org.apache.jdo.util.Bundle"; // NOI18N
    
    private final static String  vendor_name_msg     	= "MSG_VendorName"; // NOI18N
    private final static String  version_number_msg  	= "MSG_VersionNumber"; // NOI18N
    
    private final static String  vendor_name     	= "VendorName"; // NOI18N
    private final static String  version_number  	= "VersionNumber"; // NOI18N
    
    private final static I18NHelper msg = I18NHelper.getInstance(default_bundle);
    
    private final static String vendor = msg.msg(vendor_name_msg);
    private final static String version = msg.msg(version_number_msg);
    
    public static void main(String[] args) {
    	if (args == null || args.length == 0 ||
            (args.length == 1 && args[0].equals("-version")) ) { // NOI18N
            System.out.println( msg.msg("MSG_DisplayVersion", version)); // NOI18N
        }
        System.exit(0);
    }
    
    /**
     * Constructor without parameters
     */
    public JDORIVersion() { 
        loadProperties();
    }
    
    /**
     * Constructor without parameters
     */
    public JDORIVersion(String fileName) {
        loadProperties(fileName);
    }
    
    /**
     * Load default properties 
     */
    private static void loadProperties() {
        _properties = new Properties();
        _properties.setProperty(vendor_name, vendor);
        _properties.setProperty(version_number, version);
    }
    
    /**
     * Load specific properties file
     */
    private static void loadProperties(String fileName) {
        Properties temp_properties = new Properties();
        try {
            InputStream in = JDORIVersion.class.getResourceAsStream(fileName);
            if (in == null)
                throw new java.io.FileNotFoundException(fileName);
    
            temp_properties.load(in);
            in.close();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e.toString());
        }
    
        _properties = new Properties();
        _properties.setProperty(vendor_name, temp_properties.getProperty(vendor_name));
        _properties.setProperty(version_number, temp_properties.getProperty(version_number));
    }
    
    /** 
     * Return Vendor properties for a given file name
     */
    public static Properties getVendorProperties(String fileName) {
        loadProperties(fileName);
        return getVendorProperties();
    }
    
    /** 
     * Return Vendor properties
     */
    public synchronized static Properties getVendorProperties() {
        if (_properties == null) {
            loadProperties();
        }
        return _properties;
    }
    
}
