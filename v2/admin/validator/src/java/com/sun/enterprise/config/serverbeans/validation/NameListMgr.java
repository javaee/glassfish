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

package com.sun.enterprise.config.serverbeans.validation;

//jdk imports
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.File;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import java.io.IOException;
import java.net.URL;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;

import java.lang.Class;
import java.lang.reflect.Constructor;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigContextEventListener;

// config imports
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.*;

/**
 *  This class provides access to NameLists defined for validations 
 *  Its constructor parses nameLists.xml file to read lists descriptors.
 *
 *  $Id: NameListMgr.java,v 1.4 2005/12/25 03:44:23 tcfujii Exp $
 *  @author alexkrav
 *
 *  $Log: NameListMgr.java,v $
 *  Revision 1.4  2005/12/25 03:44:23  tcfujii
 *  Updated copyright text and year.
 *
 *  Revision 1.3  2005/08/18 17:10:08  kravtch
 *  M3: Admin Config Validator changes:
 *      1. referencees support in name domains;
 *      2. "virtual servers" name domain is added;
 *      3. required elements existance validation (for ADD/DELETE element);
 *      4. Clean up of custom validators (removed duplication testy, covered by generic validation;
 *      5. Validation messages are prefixed by message code (for SQE negative tests)
 *  Submitted by: kravtch
 *  Reviewed by: Shreedhar
 *  Affected modules admin/validator; admin-core/config-api
 *  Tests passed: SQE, QLT/EE + devtests
 *
 *  Revision 1.2  2005/06/27 21:18:16  tcfujii
 *  Issue number: CDDL header updates.
 *
 *  Revision 1.1  2005/06/17 17:09:58  kravtch
 *  MS3: Config Validator's infrastructure changes:
 *     - new "VALIDATE" ConfigContextEvent type introduced for "static" validation;
 *     - host "static" validation as ConfigContext listener for "VALIDATE" events;
 *     - new namespace "http://www.sun.com/ias/validation" support for RelaxNG schemas extension:
 *           - "name-domains" metadata support for test uniquiness/references;
 *           - new "belongs-to"/"references-to"/"type"/"**-than" schema attributes;
 *     - most rng element definitions and xslt scripts changed;
 *     - new base validation classes to perform generic validation;
 *     - custom validation test classes cleaned from performing generic validation cases;
 *     - ConfigBeans and domain dtd re-generated;
 *     - Schematron(scripts and validation) and Relax NG (validator only) not used for validation any more;
 *  Submitted by: kravtch
 *  Tests passed QLT/EE + devtests
 *  Modules affected: appserver-commons; admin/validator; admin-core/config-api
 *
*/

public class NameListMgr {
    
    // Logging 
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    LocalStringManagerImpl _localStrings = StringManagerHelper.getLocalStringsManager();

    Hashtable _lists;
    boolean _precreateAndKeepLists;
    ConfigContext _ctx;
    
    
    public NameListMgr(ConfigContext ctx, boolean precreateAndKeepLists) {
        _lists = new Hashtable();
        _precreateAndKeepLists = precreateAndKeepLists;
        _ctx = ctx;
        loadDescriptors();
//System.out.println(this.toString());    
    }

    public String toString()
    {
        String str = "";
        
        if(_lists!=null)
        {
            Enumeration keys = _lists.keys();
            while(keys.hasMoreElements())
            {
                String key = (String)keys.nextElement();
                str = str + "\n" + ((NameList)_lists.get(key)).toString();
            }
        }
        return str;
    }
    
    private NameList getNameList(String nameDomainName)
    {
        NameList list = null;
        if(_lists!=null)
            list=(NameList)_lists.get(nameDomainName);
        if(list!=null)
        {
            //FIXME: 1. should no recreate all sublists  
            //FIXME: 2. should recreate only if ADD/DELETE happened
            if(!_precreateAndKeepLists)
            {
                //XPathHelper.getXPathPrefix(xpath
                list.buildLists(null);
            }
        }
        return list;
    }
            
    public String getDomainValueSourceXPath(String nameDomainName, Object value, String xpath)
    {
        //returns true even if value not in doamin at all
        NameList list = getNameList(nameDomainName);
        if(list!=null)
        {
            return list.getDomainValueSourceXPath(value, xpath, false);
        }
        return null;
    }
    public String getDomainValueReferenceeXPath(String nameDomainName, Object value, String xpath)
    {
        //returns true even if value not in doamin at all
        NameList list = getNameList(nameDomainName);
        if(list!=null)
        {
            return list.getDomainValueSourceXPath(value, xpath, true);
        }
        return null;
    }
    
    public boolean isUniqueValueInNameDomain(String nameDomainName, Object value, String xpath)
    {
        //returns true even if value not in doamin at all
        NameList list = getNameList(nameDomainName);
        if(list!=null && list.isValueInNameDomain(value, xpath, false))
        {
            String sourceXPath = list.getDomainValueSourceXPath(value, xpath, false);
            return (xpath.equals(sourceXPath));
        }
        return true;
    }

    public boolean isValueInNameDomain(String nameDomainName, Object value, String xpath)
    {
        //returns true only if value is in correspondent name domain
        NameList list = getNameList(nameDomainName);
        if(list!=null)
        {
            return list.isValueInNameDomain(value, xpath, false);
        }
        return false;
    }

    public boolean isValueInNameDomainReferenced(String nameDomainName, Object value, String xpath)
    {
        //returns true only if value is in correspondent name domain
        NameList list = getNameList(nameDomainName);
        if(list!=null)
        {
            return list.isValueInNameDomain(value, xpath, true);
        }
        return false;
    }
    
    public String getDescriptionForNameDomain(String nameDomainName)
    {
        //returns true only if value is in correspondent name domain
        NameList list = getNameList(nameDomainName);
        if(list!=null && list._fullName!=null)
        {
            return list._fullName;
        }
        return nameDomainName;
    }
    
    // Get the path of namelist descriptors xml file from jar
    private String getDescriptorsFilePath() throws Exception {
        URL propertiesFile = NameListMgr.class.getClassLoader().getResource(
             "com/sun/enterprise/config/serverbeans/validation/config/name-domains.xml");
//TODO: for test only
// URL propertiesFile = (new File("/ias/admin/validator/NameLists.xml")).toURL();
        return propertiesFile.toString();
    }

    // Loads all namelist descriptors and instantiate correspondent name lists
    public boolean loadDescriptors() {
        boolean allIsWell = true;
       
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource(getDescriptorsFilePath());
            Document doc = db.parse(is);
            NodeList list = doc.getElementsByTagName("name-list");
            for(int i=0; i<list.getLength(); i++) 
            {
                Element e = (Element) list.item(i);
                String name     =  e.getAttribute("name");
                String fullName =  e.getAttribute("full-name");
                String scope     =  e.getAttribute("scope");
                //sources list
                NodeList nl = e.getElementsByTagName("forms-from");
                ArrayList xpath_arr = new ArrayList();
                for(int j=0; j<nl.getLength(); j++) 
                {
                    String xpath = ((Element)nl.item(j)).getAttribute("xpath");
                    if(xpath!=null && xpath.length()>0)
                        xpath_arr.add(xpath);
                }    
                //referencees list
                nl = e.getElementsByTagName("referenced-by");
                ArrayList ref_xpath_arr = new ArrayList();
                for(int j=0; j<nl.getLength(); j++) 
                {
                    String xpath = ((Element)nl.item(j)).getAttribute("xpath");
                    if(xpath!=null && xpath.length()>0)
                        ref_xpath_arr.add(xpath);
                }    
/*
System.out.println("name="+name);
System.out.println("   fullname="+fullName);
System.out.println("   scope="+scope);
for(int j=0; j<xpath_arr.size(); j++)
    System.out.println("         xpath="+xpath_arr.get(j));
*/
                
                _lists.put(name, new NameList(name, fullName, scope, xpath_arr, ref_xpath_arr, _ctx, _precreateAndKeepLists));
            }
        } catch (ParserConfigurationException e) {
            _logger.log(Level.WARNING, "parser_error", e);
            allIsWell = false;
        } catch (SAXException e) {
            _logger.log(Level.WARNING, "sax_error", e);
            allIsWell = false;     
        } catch (IOException e) {
            _logger.log(Level.WARNING, "error_loading_xmlfile", e);
            allIsWell = false;
        } catch(Exception e) {
            _logger.log(Level.WARNING, "error", e);
            allIsWell = false;
        }
        return allIsWell;
    }

    
}
