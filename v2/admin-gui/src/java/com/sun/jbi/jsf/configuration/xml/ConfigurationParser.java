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
 * ConfigurationParser
 */
package com.sun.jbi.jsf.configuration.xml;

import com.sun.jbi.jsf.util.JBILogger;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.jbi.jsf.configuration.xml.model.Configuration;
import com.sun.jbi.jsf.configuration.xml.model.DisplayInformation;

/**
 * @author Sun Microsystems
 *
 */
public class ConfigurationParser extends DefaultHandler implements Serializable {

    // Private members needed to parse the XML document
    private boolean parsingInProgress; // keep track of parsing
    private Stack<String> qNameStack = new Stack<String>(); // keep track of QName
    private Configuration configuration = new Configuration(); // keep track of element2
    private String[] displayKeys;
    private DisplayInformation[] displayInfo = null;

    // XML TAGS
    private static final String CONFIGURATION_KEY = "Configuration";
    private static final String DISPLAYNAME_KEY = "displayName";
    private static final String DISPLAYDESCRIPTION_KEY = "displayDescription";
    private static final String ISPASSWORDFIELD_KEY = "isPasswordField";
    private static final String NAME_KEY = "name";
    
     //Get Logger to log fine mesages for debugging
    private static Logger sLog = JBILogger.getInstance();
    
    /**
     * 
     */
    public ConfigurationParser(String[] keys) {
        displayKeys = new String[keys.length];
        displayInfo = new DisplayInformation[keys.length];
        for(int index = 0; index < keys.length; index++) {
            displayKeys[index] = keys[index];
            displayInfo[index] = new DisplayInformation();
        }
    }
    
    public Configuration getComponentConfiguration() {
        return this.configuration;
    }

    /**
     * Start of document processing.
     * @throws org.xml.sax.SAXException is any SAX exception, 
     * possibly wrapping another exception.
    */
    public void startDocument() 
            throws SAXException {
        parsingInProgress = true;
        qNameStack.removeAllElements();
    }
    
    /**
     * End of document processing.
     * @throws org.xml.sax.SAXException is any SAX exception, 
     * possibly wrapping another exception.
     */
    public void endDocument() 
            throws SAXException {
        parsingInProgress = false;
        // We have encountered the end of the document. Do any processing that is desired, 
        // for example dump all collected element2 values.

    }    
    
    /**
     * Process the new element.
     * @param uri is the Namespace URI, or the empty string if the element 
     * has no Namespace URI or if Namespace processing is not being performed.
     * @param localName is the The local name (without prefix), or the empty 
     * string if Namespace processing is not being performed.
     * @param qName is the qualified name (with prefix), or the empty string 
     * if qualified names are not available.
     * @param attributes is the attributes attached to the element. If there 
     * are no attributes, it shall be an empty Attributes object.
     * @throws org.xml.sax.SAXException is any SAX exception, 
     * possibly wrapping another exception.
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) 
            throws SAXException {
        String displayName = null;
        String displayDescription = null;
        String password = null;
        boolean isPasswordField = false;
        if (qName.endsWith(CONFIGURATION_KEY)) {
            // ELEMENT1 has an attribute, get it by name
            String name = attributes.getValue(NAME_KEY);
            // Do something with the attribute
            this.configuration.setName(name);
        } else {
            for(int index = 0; index < displayKeys.length; index++) {
                if((displayKeys[index] != null) && 
                  (qName.endsWith(displayKeys[index]) == true)) {
                    // Keep track of the value of element2
                    isPasswordField = false;
                    displayName = attributes.getValue(DISPLAYNAME_KEY);
                    displayDescription = attributes.getValue(DISPLAYDESCRIPTION_KEY);
                    password = attributes.getValue(ISPASSWORDFIELD_KEY);
                    if((displayName != null) &&
                       (displayDescription != null) &&
                       (password != null)) {
                        isPasswordField = Boolean.parseBoolean(password);
                        sLog.fine("displayName:"+ displayName+
                                           " displayDescription:"+displayDescription+
                                           " password:"+password+
                                           " isPasswordField:"+isPasswordField);
                        displayInfo[index] = new DisplayInformation(displayKeys[index],
                                displayName,displayDescription,isPasswordField);
                    } else {
                        sLog.fine("displayName:"+ displayName+
                                           " displayDescription:"+displayDescription+
                                           " password:"+password);
                    }
                }
            }
        }
        // Keep track of QNames
        qNameStack.push(qName);
    }
    
    /**
     * Process the character data for current tag.
     * @param ch are the element's characters.
     * @param start is the start position in the character array.
     * @param length is the number of characters to use from the 
     * character array.
     * @throws org.xml.sax.SAXException is any SAX exception, 
     * possibly wrapping another exception.
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        String qName;
        String chars = new String(ch, start, length);
        // Get current QName
        qName = (String) qNameStack.peek();
        if (qName.endsWith(CONFIGURATION_KEY)) {
            // Nothing to process
        } else {
            for(int index = 0; index < displayKeys.length; index++) {
                if((displayKeys[index] != null) && 
                  (qName.endsWith(displayKeys[index]) == true)) {
                    // Keep track of the value of element2
                    if(chars != null) {
                        displayInfo[index].setDefaultValue(chars);
                    } else {
                        displayInfo[index].setDefaultValue("");
                    }
                } else {
                }
            }
        }
    }    
    
    /**
     * Process the end element tag.
     * @param uri is the Namespace URI, or the empty string if the element 
     * has no Namespace URI or if Namespace processing is not being performed.
     * @param localName is the The local name (without prefix), or the empty 
     * string if Namespace processing is not being performed.
     * @param qName is the qualified name (with prefix), or the empty 
     * string if qualified names are not available.
     * @throws org.xml.sax.SAXException is any SAX exception, 
     * possibly wrapping another exception.
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // Pop QName, since we are done with it
        qNameStack.pop();
        if (qName.endsWith(CONFIGURATION_KEY)) {
            // We have encountered the end of ELEMENT1
            // ...
            for(int index = 0; index < displayKeys.length; index++) {
                if((displayKeys[index] != null) && 
                   (displayInfo[index] != null)) {
                    this.configuration.addDisplayDetail(displayKeys[index], displayInfo[index]);
                } else {
                    sLog.fine("Index "+index+" displayKeys or displayInfo is null.");
                    if(displayKeys[index] != null) {
                        sLog.fine("displayKeys["+index+"] is: "+displayKeys[index]);
                    }
                    if(displayInfo[index] != null) {
                        sLog.fine("displayInfo["+index+"] is: ");
                        displayInfo[index].dump();
                    }
                }
            }
        } else {
            // We have encountered the end of an ELEMENT2
            // ...
        }        
    }    
    
    /**
     * 
     * @param uriString
     * @param keys
     * @return
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws IOException
     */
    public static ConfigurationParser parse(String uriString, String[] keys) throws MalformedURLException, ParserConfigurationException, SAXException, URISyntaxException, IOException {
        
            // Get an instance of the SAX parser factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
    
            // Get an instance of the SAX parser
            SAXParser saxParser = factory.newSAXParser();
            
            // Initialize the URI and XML Document InputStream
            URI uri = new URI(uriString);
            InputStream inputStream = uri.toURL().openStream(); 
    
            // Create an InputSource from the InputStream
            InputSource inputSource = new InputSource(inputStream);
    
            // Parse the input XML document stream, using my event handler
            ConfigurationParser parser = new ConfigurationParser(keys);
            saxParser.parse(inputSource, parser);
            
            return parser;
    }
    
    /**
     * 
     * @param xmlData
     * @param keys
     * @return
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws URISyntaxException
     * @throws IOException
     */
    public static ConfigurationParser parseFromString(String xmlData, String[] keys) throws MalformedURLException, ParserConfigurationException, SAXException, URISyntaxException, IOException {
        
            // Get an instance of the SAX parser factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
    
            // Get an instance of the SAX parser
            SAXParser saxParser = factory.newSAXParser();
            
            // Initialize the XML Document InputStream
            InputStream inputStream = new ByteArrayInputStream(xmlData.getBytes("UTF-8")); 
    
            // Create an InputSource from the InputStream
            InputSource inputSource = new InputSource(inputStream);
    
            // Parse the input XML document stream, using my event handler
            ConfigurationParser parser = new ConfigurationParser(keys);
            saxParser.parse(inputSource, parser);
            
            return parser;
    }
    
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        String uri = "file:///C:/Alaska/jbicomps/cachese/jbiadapter/componentconfiguration.xml";
        String[] keys = null;
        String propertiesFile = "C:/Alaska/jbicomps/cachese/jbiadapter/config.properties";
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
        }
        keys = new String[properties.size()];
        Set set = properties.entrySet();
        Iterator iterator = set.iterator();
        for(int index = 0; iterator.hasNext() == true; index++) {
            Entry entry = (Entry)iterator.next();
            keys[index] = (String) entry.getKey(); 
        }
        try {
            ConfigurationParser parser = ConfigurationParser.parse(uri, keys);
            parser.getComponentConfiguration().dump();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
        
    }

}
