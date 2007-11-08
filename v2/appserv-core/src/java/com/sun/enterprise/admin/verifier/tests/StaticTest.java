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

package com.sun.enterprise.admin.verifier.tests;

// <addition> srini@sun.com Bug : 4698687
// JMX Imports
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

// 8.0 XML Verifier
//import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.admin.verifier.Result;
import com.sun.enterprise.config.ConfigContext;

import javax.xml.parsers.*;
import org.xml.sax.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import com.sun.enterprise.config.serverbeans.*;


/* Test to check the Port validity
 * Author : srini@sun.com
 **/

public class StaticTest {
    
     // Logging
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);

    
    //<addition author="irfan@sun.com" [bug/rfe]-id="4704985" >
    /* Adding various constants to this file. This should remove the hard coded values that
     * we are using in various tests.
     */
    
    /** Represents the ADD configbean request*/
    public static final String ADD = "ADD";
    /** Represents the DELETE configbean request*/
    public static final String DELETE = "DELETE";
    /** Represents the UPDATE configbean request*/
    public static final String UPDATE = "UPDATE";
    /** Represents the SET configbean request*/
    public static final String SET = "SET";
    
    public static final String IAS_NAME = "com.sun.appserv:name=";
    public static final String XML_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <xml>";
    public static final String XML_2 = "</xml>";
    //</addition>
    
    public StaticTest() {
    }
    
    // check if port is within the range of 2^16
    public static boolean isPortValid(int i){
        if(i > 0 && i <= 65535) 
            return true;
        else
            return false;
    }
    
    // Function added to check address if it needs to be resolved
    // Bug : 4697248
    public static boolean checkAddress(String address) {
        if(address.equalsIgnoreCase("ANY") || address.equalsIgnoreCase("INADDR_ANY") || 
                                address.equalsIgnoreCase("localhost")) 
            return false;
        else 
            return true;
    }
    
    // Utility Function added Bug No. : 4698687
    /**
     * should just take in the id. construct the name inside this method
     */
    public static boolean checkObjectName(String id) throws MalformedObjectNameException {
        String name = IAS_NAME + id;
        new ObjectName(name);  // verify that it's good
        return true;
    }
    
    /**
     * method to be called by verifier
     */
    public static boolean checkObjectName(String id, Result result) {
        try {
            return checkObjectName(id);
        } catch(MalformedObjectNameException ex) {
            _logger.log(Level.FINE, "serverxmlverifier.exception", ex);
            result.failed(ex.getMessage());
            return false;
        }
    }
    
    /**
     *external method to be called by instller, etc for checking if the string
     * is a valid xml string
     * This is a very expensive test. Be prudent in using it.
     */
    public static boolean checkXMLName(String name) throws SAXParseException, SAXException,
							IOException,
							ParserConfigurationException {
            //Construct a valid xml string
            String xml = XML_1 + name + XML_2;
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
            InputSource is = new InputSource(bais);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.parse(is);
            return true;
    }
    
    /**
     * method called by verifier for checking for a valid xml string
     * This is a very expensive test. Be prudent in using it.
     */
    public static boolean checkXMLName(String name, Result result) throws SAXParseException {
        try {
            return checkXMLName(name);
        } catch (Exception s) {
            _logger.log(Level.FINE, "serverxmlverifier.exception", s);
            result.failed(s.getMessage());
            return false;
        }
    }
    
    /* Method to get the reference to config */
    // 8.0 XML Verifier
    public static Config getConfig(ConfigContext context) {
        
        Config mConfig=null;
        try {
            mConfig = ServerBeansFactory.getConfigBean(context);
        } catch(Exception e) {
        }
        return mConfig;
    }
}
