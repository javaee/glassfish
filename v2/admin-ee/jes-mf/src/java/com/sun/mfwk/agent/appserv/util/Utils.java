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

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.IOException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.ParserConfigurationException;

import com.sun.mfwk.agent.appserv.logging.LogDomains;
import com.sun.mfwk.agent.appserv.ASEntityResolver;
        

/**
 * Utility helper class.
 */
public class Utils {
    
   /**
    * Returns the parsed document object. 
    *
    * @param  url  url to the xml descriptor file
    * @param  defaultLocation  default location
    * @throws IOException  if an i/o error
    *
    * @return  document object
    */
    public static Document getDocument(String url, String defaultLocation)
            throws IOException {
        
        Document doc = null;
        InputStream in = null;
        
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new ASEntityResolver());           
            
            try {
                ClassLoader cl = (Utils.class).getClassLoader();
                in = cl.getResourceAsStream(url);
            } catch (Exception e) {
                // ignore
            }
            
            if (in == null) {
                in = new FileInputStream(defaultLocation+url);
            }
            
            doc = db.parse(in);
            
        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        } catch (SAXParseException e) {
            throw new IOException(e.getMessage());
        } catch (SAXException e) {
            throw new IOException(e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) { }
            }
        }
        
        return doc;
    }


   /**
    * Gets an input stream for the given file.
    *
    * @param relativeFilePath the relative path name of the file.
    *
    * @return InputStream the input stream for the given file; returns null
    * in case of failure.
    */
    public static InputStream getInputStream(String relativeFilePath) {

        if (relativeFilePath == null){
            throw new IllegalArgumentException();
        }

        ClassLoader cl = (Utils.class).getClassLoader();
        return cl.getResourceAsStream(relativeFilePath);
    }
    
    /**
     * Returns only first occurence, from beginsWith to endsWith or till end
     */
    public static String getStringRegion(String source, String beginsWith, 
            String endsWith) {

        String region = null;

        int beginIndex = source.indexOf(beginsWith);
        if (beginIndex >= 0) {
            int endIndex = 
                source.indexOf(endsWith, beginIndex + beginsWith.length());

            if (endIndex < 0) {
                endIndex = source.length();
            }
            region = source.substring(beginIndex+beginsWith.length(), endIndex);
        }
        return region;
    }
    
    public boolean isDAS(String serverName) {
        return Constants.ADMIN_SERVER_NAME.equals(serverName);
    }

    public static void log(Level level, String message, Exception e) {
        Logger logger = LogDomains.getLogger();
        if (logger != null) {
            logger.log(level, message, e);
        }
    }

    public static void log(Level level, String message) {
        Logger logger = LogDomains.getLogger();
        if (logger != null) {
            logger.log(level, message);
        }
    }
 
   /**
    * Gets a sum of values of an attribute from the given set of mbeans.
    *
    * @param connection the given mbean server connection.
    * @param objectNames the given set of mbeans on the given server connection.
    * @param attribute the given attribute, value of which is fetched from each
    *        of the mbean in the given set.
    *
    * @return long the sum of attribute values fetched from each of the mbean in
    *         the given set.
    */
    public static long getAttributeSum(MBeanServerConnection connection,
        Set objectNames, String attribute) {
        long attributeSum = 0; 
        Iterator iter = objectNames.iterator();
        ObjectName objectName = null; 
        Long attributeValue;
        while (iter.hasNext()) {
            objectName = (ObjectName) iter.next();
            try {
                attributeValue = (Long) connection.getAttribute(objectName, attribute); 
            } catch (Exception exception) {
                Utils.log(Level.WARNING, "Error getting attribute, " +
                    attribute + " of" + objectName, exception);
                continue;
            }
            attributeSum = attributeSum + attributeValue.longValue();
        }
        return attributeSum;
    }

}
