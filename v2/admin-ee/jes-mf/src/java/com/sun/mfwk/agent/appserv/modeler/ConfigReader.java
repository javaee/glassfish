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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.modeler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.InputStream;
import java.io.FileInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.IOException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility class to parse the xml file that describes the mappings.
 */
class ConfigReader {

    /**
     * Returns the available mbeans described in the xml.
     *
     * @param  url  url to the xml descriptor file
     * @param  dLocation  default location
     *
     * @return available mbeans
     *
     * @throws IOException  if an i/o error
     */
    static NodeList getMBeans(String url, String dLocation) throws IOException {

        Document doc = getDocument(url, dLocation);

        NodeList root = doc.getElementsByTagName("mbeans-descriptors");
        Element e = (Element) root.item(0);
        NodeList mbeans = e.getElementsByTagName("mbean");

        return mbeans;
    }

    /**
     * Returns the available relations described in the xml.
     *
     * @param  url  url to the xml descriptor file
     * @param  dLocation  default location
     *
     * @return available mbeans
     *
     * @throws IOException  if an i/o error
     */
    static NodeList getRelations(String url, String dLocation) 
            throws IOException {

        Document doc = getDocument(url, dLocation);

        NodeList root = doc.getElementsByTagName("relation-descriptors");
        Element e = (Element) root.item(0);
        NodeList relations = e.getElementsByTagName("relation");

        return relations;
    }

    /**
     * Returns the parsed document object. 
     *
     * @param  url  url to the xml descriptor file
     * @param  defaultLocation  default location
     * @throws IOException  if an i/o error
     *
     * @return  document object
     */
    static Document getDocument(String url, String defaultLocation) 
            throws IOException {

        Document doc = null;
        InputStream in = null;

        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();

            try {
                ClassLoader cl = (ConfigReader.class).getClassLoader();
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
     * Returns the attributes of an mbean element.
     *
     * @param  e  mbean element
     */
    static NodeList getMBeanAttrMappings(Element e) {

        NodeList attr = null;
        if (e != null) {
            attr = e.getElementsByTagName("attribute");
        }
        return attr;
    }

    /**
     * Returns the ObjectName value of an mbean element.
     *
     * @param  mbean  mbean element
     * @param  isProxy  if true, the method will return the proxy object name
     *
     * @return  object name
     */
    static String getMBeanObjectName(Element mbean, boolean isProxy) {

        // descriptor elements
        NodeList descriptors = mbean.getElementsByTagName("descriptor");
        Element d = (Element) descriptors.item(0);

        // field elements
        NodeList fields = d.getElementsByTagName("field");

        Element f = null;
        
        if (!isProxy) {
            // first field is ObjectName
            f = (Element) fields.item(0);
        } else {
            // second field is ProxyObjectName
            f = (Element) fields.item(1);
        }

        // object name
        String objectName = f.getAttribute("value").trim();

        return objectName;
    }
}
