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
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/


package com.sun.xml.registry.common.util;

import java.util.*;
import java.io.*;
import javax.naming.*;
import java.net.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import com.sun.xml.registry.uddi.*;

import com.sun.xml.registry.common.*;
import com.sun.xml.registry.uddi.infomodel.*;
import javax.swing.*;


import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Class Declaration.
 * @see
 * @author Farrukh S. Najmi
 * @author Kathy Walsh
 * @version   1.2, 05/02/00
 */
public class Utility {

    public static final String LOGGING_DOMAIN = "javax.enterprise.resource.webservices.registry";
    
    Logger logger = Logger.getLogger(com.sun.xml.registry.common.util.Utility.LOGGING_DOMAIN + ".common");

    private static Utility  instance = null;
    private String jaxrHome=null;
    
    /**
     * Class Constructor.
     *
     *
     * @see
     */
    protected Utility() {}
    
    public void setJAXRHome(String jaxrHome) {
        this.jaxrHome = jaxrHome;
    }
    
    public String getJAXRHome() {
	logger.finest("getJAXRHome() called");
        if (jaxrHome == null) {
            try {
                // can throw exception or return null
                jaxrHome = System.getProperty("JAXR_HOME");
                if (jaxrHome == null) { // still
                    throw new NullPointerException();
                }
            } catch (NullPointerException npe) {
                throw new RuntimeException(ResourceBundle.getBundle("com/sun/xml/registry/common/LocalStrings").getString("Utility:JAXR_HOME_must_be_set"), npe);
            }
        }
        
        return jaxrHome;
    }
    
    /**
     *
     * @return The root content for JAXR client or service
     * @see
     */
    public String getContextRoot() /* throws MessengerException */ {
        //??
        String  contextRoot = "c:/jaxr";        // default
        
        try {
            Context context = new InitialContext();
            
            contextRoot =
		(String) context.lookup("java:comp/env/jaxr-service/contextRoot");
        }
        catch (NamingException e) {
            System.getProperty("JAXR_HOME", "c:/jaxr");
        }
        return contextRoot;
    }
    
    /**
     * Method Declaration.
     *
     *
     * @return
     *
     * @see
     */
    public String getContextRootURLString() {
        String  contextRoot = getContextRoot();
        
        if ((!(contextRoot.startsWith("http", 0)))
	    && (!(contextRoot.startsWith("file", 0)))) {
            contextRoot = "file:///" + contextRoot;
        }
        return contextRoot;
    }
    
    /**
     * Method Declaration.
     *
     *
     * @param urlSuffix
     *
     * @return
     *
     * @exception MalformedURLException
     *
     * @see
     */
    public URL getURL(String urlSuffix) throws MalformedURLException {
        URL     url = null;
        String  contextRootURLString = getContextRootURLString() + urlSuffix;
        
        url = new URL(contextRootURLString);
        return url;
    }
    
    /**
     * Method Declaration.
     *
     *
     * @return
     *
     * @see
     */
    public static Utility getInstance() {
        if (instance == null) {
            synchronized (Utility.class) {
                if (instance == null) {
                    instance = new Utility();
                }
            }
        }
        return instance;
    }
    
    
    
    public static String generateUUID() {
        String uuid=null;
        
        try  {
            uuid = InetAddress.getLocalHost() + (new java.rmi.server.UID()).toString();
        }
        catch (UnknownHostException e)  {
            e.printStackTrace();
            //??
        }
        
        return uuid;
    }
    
}
