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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.net.URL;
import java.io.File;
import java.io.IOException;

//xml support
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import com.sun.enterprise.util.LocalStringManagerImpl;

import com.sun.enterprise.config.ConfigContext;

// config imports
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigBean;

//JMX
import javax.management.Attribute;
import javax.management.AttributeList;

/**
 *  This class represents name list for given config context  
 *
 *  $Id: NameList.java,v 1.4 2005/12/25 03:44:23 tcfujii Exp $
 *  @author alexkrav
 *
 *  $Log: NameList.java,v $
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

public class NameList {
    
    // Logging 
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    LocalStringManagerImpl _localStrings = StringManagerHelper.getLocalStringsManager();

    ConfigContext _ctx;

    ArrayList _srcXPathes;
    ArrayList _refXPathes;
    
    Hashtable _srcLists = null;
    Hashtable _refLists = null;
    
    boolean _bKeepList = false;
    String  _name;
    String  _fullName;
    String  _scope;
    int     _scope_depth;
    
    
    public NameList(String name, String fullName, String scope, ArrayList srcXPathes, ArrayList refXPathes, ConfigContext ctx, boolean bPreCreateAndKeepList) {
        _name = name;
        _fullName = fullName;
        _srcXPathes = srcXPathes;
        _refXPathes = refXPathes;
        if(scope!=null && !scope.equals("/"))
        {
            _scope = scope.trim();
            _scope_depth = XPathHelper.getNumberOfElemTokens(_scope);
//System.out.println("######################### _scope = " +_scope + " _scope_depth="+_scope_depth);
        }
        _ctx = ctx;
        _bKeepList = bPreCreateAndKeepList;
        _srcLists = new Hashtable();
        _refLists = new Hashtable();
        if(bPreCreateAndKeepList)
            buildLists(null);
    }

    public String toString()
    {
        String str = "domain name: " + _name;
        
        if(_srcLists!=null)
        {
            Iterator lists = _srcLists.keySet().iterator();
            while(lists.hasNext())
            {
                String key = (String)lists.next();
                str = str + "\n    " + "list name: " + key;
                Hashtable list = (Hashtable)_srcLists.get(key);
                if(list!=null)
                {
                    Object[] keys = (Object[])list.keySet().toArray();
                    for(int i=0; i<keys.length; i++)
                        str = str + "\n      " + keys[i]; // + "   xpath=" + list.get(keys[i]);
                }
            }
        }
        if(_refLists!=null)
        {
            Iterator lists = _refLists.keySet().iterator();
            while(lists.hasNext())
            {
                String key = (String)lists.next();
                str = str + "\n    " + "Referencees list name: " + key;
                Hashtable list = (Hashtable)_refLists.get(key);
                if(list!=null)
                {
                    Object[] keys = (Object[])list.keySet().toArray();
                    for(int i=0; i<keys.length; i++)
                        str = str + "\n      " + keys[i]; // + "   xpath=" + list.get(keys[i]);
                }
            }
        }
        return str;
    }

    public String getDomainValueSourceXPath(Object value, String xpath, boolean bRef)
    {
        String listName;
        if(xpath==null || value==null || (listName = getListNameForXpath(xpath))==null)
            return null;
        Hashtable list = getNamedList(listName, bRef, false);
        if(list!=null)
            return (String)list.get(value);
        return null;
    }
    
    public boolean isValueInNameDomain(Object value, String xpath, boolean bRef)
    {
        return (getDomainValueSourceXPath(value, xpath, bRef)!=null);
    }

    protected void buildLists(String onlyPrefix)
    {
        //FIXME: should clear only poroper sublist
        _srcLists.clear();
        _refLists.clear();
        
        //source lists
        buildList(_srcXPathes, onlyPrefix, false);
        //referencees lists
        buildList(_refXPathes, onlyPrefix, true);
    }
    
    private void buildList(ArrayList xpathes, String onlyPrefix, boolean bRef)
    {
        //build list
        AttributeList arr = XPathHelper.resolve(_ctx, xpathes, onlyPrefix);
        for(int i=0; i<arr.size(); i++)
        {
            Attribute attr = (Attribute)arr.get(i);
            String[] values = ((String)attr.getValue()).split(",");
            for(int j=0; j<values.length; j++)
            {
                addValueToProperList(attr.getName(), values[j], bRef);
            }
        }
    }
    
    private String addValueToProperList(String xpath, Object value, boolean bRef)
    {
        String listName;
        if(xpath==null || value==null || (listName = getListNameForXpath(xpath))==null)
            return null;
//System.out.println("+++addValueToProperList() domain-name=" + _name + " sublistName=" + listName + "  value=" + value);
        addValueToNamedList(listName, xpath,  value, bRef);
        return listName;
    }
    
    private void addValueToNamedList(String listName, String sourceXPath, Object value, boolean bRef)
    {
        if(listName!=null && value!=null)
        {
            Hashtable list = getNamedList(listName, bRef, true);
            if(list.get(value)==null)
                list.put(value, sourceXPath);
        }
    }
    
    private Hashtable getNamedList(String listName, boolean bRef, boolean bCreateIfNotFound)
    {
        Hashtable lists = bRef?_refLists:_srcLists;
        Hashtable list = (Hashtable)lists.get(listName);
        if(list==null && bCreateIfNotFound)
        {
            list = new Hashtable();
            lists.put(listName, list);
        }
        return list;
    }
    
    private String getListNameForXpath(String xpath)
    {
        return XPathHelper.getXPathPrefix(xpath, _scope_depth);
    }
    
}
 