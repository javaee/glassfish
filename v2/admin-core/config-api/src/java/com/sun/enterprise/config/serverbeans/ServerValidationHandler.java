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

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

//The RelativePathResolver is used to translate relative paths containing 
//embedded system properties (e.g. ${com.sun.aas.instanceRoot}/applications) 
//into absolute paths
import com.sun.enterprise.util.RelativePathResolver;

public class ServerValidationHandler extends DefaultHandler {

    //private static final Logger _logger = LogDomains.getLogger(LogDomains.CONFIG_LOGGER);
    
    public static final String SERVER_DTD_PUBLIC_ID = 
        "-//Sun Microsystems Inc.//DTD Sun ONE Application Server 9.1//EN";
    
    private static final String SERVER_DTD_PUBLIC_ID_PATH = 
        "/sun-domain_1_3.dtd";
    
    public static final String SERVER_DTD_SYSTEM_ID = 
        "http://www.sun.com/software/appserver/dtds/sun-domain_1_3.dtd";
    
    //===========================================================
    // SAX ErrorHandler methods
    //===========================================================

    // treat validation errors as fatal
    public void error(SAXParseException e)
    throws SAXParseException
    {
        /*
    _logger.log(Level.SEVERE,"config.errorhandler_msg",new Object[]{"" + e.getLineNumber(),
                                                                        "" + e.getColumnNumber(),
                                                                        "" + e.getSystemId(), 
                                                                        "" + e.getPublicId()});
    _logger.log(Level.SEVERE,"   " + e.getMessage());
         */
        throw e;
    }

    // dump warnings too
    public void warning(SAXParseException e)
    throws SAXParseException
    {
        /*
    _logger.log(Level.WARNING,"config.errorhandler_msg",new Object[]{"" + e.getLineNumber(),
                                                                        "" + e.getColumnNumber(),
                                                                        "" + e.getSystemId(), 
                                                                        "" + e.getPublicId()});
    _logger.log(Level.WARNING,"   " + e.getMessage());
         */
        //FIXME
    }

    public void fatalError(SAXParseException e) 
    throws SAXParseException {
        /*
    _logger.log(Level.SEVERE,"config.errorhandler_msg",new Object[]{"" + e.getLineNumber(),
                                                                        "" + e.getColumnNumber(),
                                                                        "" + e.getSystemId(), 
                                                                        "" + e.getPublicId()});
    _logger.log(Level.SEVERE, e.toString());
         */
        throw e;
    }    

    //===========================================================
    // Resolver methods
    //===========================================================

    public InputSource resolveEntity(String publicID, String systemID) throws SAXException {
        InputSource is = null;
        try {
            InputStream i = this.getClass().getResourceAsStream( SERVER_DTD_PUBLIC_ID_PATH );
            if( i!= null ) {
                is = new InputSource(i);
                return is;
            }

            
            is =  new InputSource(
               new FileInputStream(
               new File(new java.net.URI(
                   RelativePathResolver.resolvePath(systemID)))));
        } catch(Exception e) {
            throw new SAXException("cannot resolve dtd", e);
        }
        return is;
    }
}
