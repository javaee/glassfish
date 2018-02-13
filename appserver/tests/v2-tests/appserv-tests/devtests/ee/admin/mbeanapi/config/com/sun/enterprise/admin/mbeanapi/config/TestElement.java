/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.admin.mbeanapi.config;

import java.lang.Integer;
import java.util.Map;
import java.util.HashMap;

import javax.management.ObjectName;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.ConfigConfig;


/**
 * This is the class for element representing object.
 * It contains element name and attributes for testing element
 * This object is using in cofig related generic tests (create/delete/update/list...)
 * @author alexkrav
 * @version $Revision: 1.8 $
 */
public class TestElement {
    private String name;
    private HashMap attributes;
    RegEntry   entry;
    
    TestElement(String line)
    {
        int current = 0, next = 0;
        attributes = new HashMap();
        //not fast but easy
        //+no errors handling
        line = line.trim();
        if(line.startsWith("<"))
           line = line.substring(1).trim();
        if(line.endsWith(">"))
           line = line.substring(0, line.length()-1).trim();
        //element name
        next = line.indexOf(' ');
        if (next<0)
           name = line;
        else
           name = line.substring(0, next);
//System.out.println("new element's name=\""+name+ "\"");
//System.out.println("new element's name(camelized)=\""+camelize(name)+ "\"");
        next++;
        while (next>0 && next<line.length())
        {
            line = line.substring(next).trim();
            if(line.length()<=0)
                break;
            //attrName
            next = line.indexOf('=');
            String attrName = line.substring(0, next);
            //attrValue
            line = line.substring(next+1).trim();
            String attrValue;
            if(line.charAt(0)=='"')
            {
                next = line.indexOf('"', 1);
                attrValue = line.substring(1, next);
                next++;
            }
            else
            {
                next = line.indexOf(' ');
                if(next<=0)
                    attrValue = line;
                else
                    attrValue = line.substring(0, next);
            }
            attributes.put(attrName, attrValue);
            
            entry = TestElemRegistry.getRegEntry(name);
//System.out.println("attributes.put(\""+attrName+"\", \"" + attrValue+ "\")");
//System.out.println("attributes.put(\""+camelize(attrName)+"\"(camelized), \"" + attrValue+ "\")");
        }
    }
    public String getElementName()
    {
        return name;
    }
    public Object getAttributeValue(String attrName)
    {
        return attributes.get(attrName);
    }
    
    public Map getAttributesMapCopy()
    {
        return new HashMap(attributes);
    }
    
    public String getDtdName()
    {
        return entry.dtdName;
    }

    public String getElementKeyName()
    {
        String[] req = entry.getReqAttrs();
        return req[0];
    }

    public String getElementKey()
    {
        return (String)getAttributeValue(getElementKeyName());
    }

    public Object[] getCreationParams()
    {
        String[] req = entry.getReqAttrs();
        Class[]  classes = entry.getReqAttrClasses();
        Object[] params;
        Map optional = getAttributesMapCopy(); //all in the begining
        if(req==null)
            params = new Object[1];
        else
            params = new Object[req.length+1];
        for(int i=0; i<req.length; i++)
        {
            params[i] = attributes.get(req[i]);
            if(classes[i].equals(Integer.TYPE))
            {
                try {
                    params[i] = new Integer((String)(params[i]));
                } catch (Exception e) {}
            }
            optional.remove(req[i]); 
        }
        
        params[params.length-1] = optional;
        return params;
    }
    
    public Class[] getCreationClasses() throws Exception
    {
        Class[] classes = entry.getReqAttrClasses();
        Class[] cls = new Class[classes.length+1];
        for(int i=0; i<classes.length; i++)
            cls[i] = classes[i];
        cls[cls.length-1] = Class.forName("java.util.Map");
        return cls;
    }
	
    public boolean isConfigSubordinatedElem()
    {
        return !("domain".equals(entry.getMasterNodeName()));
    }
    
    //can be overriden
    public ObjectName getElemMBeanObjectName()  throws Exception
	{
        if(isConfigSubordinatedElem())
            return new ObjectName("com.sun.appserv:category=config,config="+TestElemRegistry.mConfigName+
                         ",type="+getDtdName()+ ","+getElementKeyName()+"="+getElementKey());
        else
            return new ObjectName("com.sun.appserv:category=config,type="+getDtdName()+
                              ","+getElementKeyName()+"="+getElementKey());
	}
    //can be overriden
    public ObjectName getElemMBeanObjectNamePattern()  throws Exception
	{
        if(isConfigSubordinatedElem())
            return new ObjectName("com.sun.appserv:category=config,config="+TestElemRegistry.mConfigName+
                         ",type="+getDtdName()+",*");
        else
            return new ObjectName("com.sun.appserv:category=config,type="+getDtdName()+",*");
                                  
	}
    
    public AMXConfig getMasterAMXConfigForElement(DomainRoot domainRoot) throws Exception
    {
        String masterNodeName = entry.getMasterNodeName();
        if("domain".equals(masterNodeName))
            return domainRoot.getDomainConfig();
        if("http-service".equals(masterNodeName))
            return ((ConfigConfig)domainRoot.getDomainConfig().getConfigConfigMap().get("server-config")).getHTTPServiceConfig();
        if("iiop-service".equals(masterNodeName))
            return ((ConfigConfig)domainRoot.getDomainConfig().getConfigConfigMap().get("server-config")).getIIOPServiceConfig();
        throw new Exception("Testing for Master Node "+masterNodeName+" is not implemented yet");
        
    }
}   
