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
	PROPRIETARY/CONFIDENTIAL. Use of this product is subject
	to license terms. Copyright (c) 2002 Sun Microsystems, Inc.
        All rights reserved.
	
	$Id: ConfigMBeansNaming.java,v 1.5 2006/11/03 19:10:33 llc Exp $
 */

package com.sun.enterprise.admin.server.core.mbean.config.naming;

//import com.sun.enterprise.admin.util.Debug;
import com.sun.enterprise.admin.common.Name;

import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.common.constant.AdminConstants;


import com.sun.enterprise.instance.InstanceEnvironment;

//Config imports
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigBeansFactory;

//JMX imports
import javax.management.ObjectName;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.appserv.management.util.jmx.JMXUtil;

/**
    Provides naming support for ConfigMbeans
*/
public class ConfigMBeansNaming extends MBeansDescriptions
{
    static MBeanNamingDescriptor[] m_mbeanDescr = initConfigMBeanNaming();
    public static final Logger sLogger = Logger.getLogger(AdminConstants.kLoggerName);
    private final static String MSG_FINDNAMINDESCRIPTOR_FAILED = "mbean.config.findnamingdescriptor_failed";
    private final static String MSG_MALFORMED_DOTTED_NAME = "mbean.config.malformed_dotted_name";
    private final static String MSG_EXCEPTION_DURING_LIST_NAMES = "mbean.config.list_names_failed";
    //**************************************************************************
    static MBeanNamingDescriptor findNamingDescriptorByType(String type)
    {
        try
        {
            for(int i=0; i<m_mbeanDescr.length; i++)
            {
                if(type.equals(m_mbeanDescr[i].getType()))
                    return m_mbeanDescr[i];
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.FINE, MSG_FINDNAMINDESCRIPTOR_FAILED, e);
        }
        return null;
    }
    //**************************************************************************
    static MBeanNamingDescriptor findNamingDescriptor(String dottedName)
    {
        try
        {
            Name name = new Name(dottedName);
            for(int i=0; i<m_mbeanDescr.length; i++)
            {
                if(m_mbeanDescr[i].isDottedPatternMatch(name))
                    return m_mbeanDescr[i];
            }
        }
        catch (Exception e)
        {
            sLogger.log(Level.FINE, MSG_FINDNAMINDESCRIPTOR_FAILED, e);
        }
        return null;
    }
    //**************************************************************************
    static MBeanNamingDescriptor findNamingDescriptor(ObjectName objectName)
    {
        try
        {
           Hashtable ht = objectName.getKeyPropertyList();
           for(int i=0; i<m_mbeanDescr.length; i++)
           {
               if(m_mbeanDescr[i].isObjectNamePatternMatch(ht))
                   return m_mbeanDescr[i];
           }
        }
        catch (Exception e)
        {
            sLogger.log(Level.FINE, MSG_FINDNAMINDESCRIPTOR_FAILED, e);
        }
        return null;
    }

    //**************************************************************************
    public static String[] findNameContinuation(String instanceName, String dottedName)
    {
        HashSet hs = new HashSet();
        int wildDescrIndex = -1;
        Name name = null;
        int  nNameTokens = 0;

        //if there is no such instance - next statement will throw runtime exception
        InstanceEnvironment instanceEnvironment= new InstanceEnvironment(instanceName);
        
        //First: add "static" continuations
        try
        {
            name = new Name(dottedName);
            nNameTokens = name.getNumParts();
        }
        catch (Exception e)
        {
            sLogger.log(Level.FINE, MSG_MALFORMED_DOTTED_NAME, e);
            return new String[0];
        }

        for(int i=0; i<m_mbeanDescr.length; i++) //enumerate all descriptors
        {
            Object[][] tokens = m_mbeanDescr[i].getDottedTokens();
            if(tokens!=null)
            {
                for(int j=0; j<tokens.length; j++) //enum different dotted patterns presentations
                {
                    if(MBeanNamingDescriptor.isDottedPatternMatch(name, tokens[j], false) && tokens[j].length>nNameTokens)
                    {
                        //dotted pattern beginning matches to sample
                        if(!(tokens[j][nNameTokens] instanceof String)) //wildcard?
                        {
                            if(tokens[j].length==nNameTokens+1) //only if wildcard at the end; otherwise - ignore
                                wildDescrIndex = i;
                        }
                        else
                        {
                            hs.add(dottedName+"."+tokens[j][nNameTokens]);
                        }
                    }
                }
            }
        }
        //Now try to add childrens names
        String xpath = null;
        if(wildDescrIndex>=0)
        {
            try
            {
                ConfigMBeanNamingInfo info = new ConfigMBeanNamingInfo(dottedName + ".fake");
                xpath = info.getXPath();
            }
            catch (Exception e)
            {
               sLogger.log(Level.FINE, MSG_EXCEPTION_DURING_LIST_NAMES, e);
            }
        }
        if(xpath!=null)
        {
            String attributeName = null;
            String elementName= null;
            // seek for elemname[@attrname=]
            xpath = xpath.trim();
            if(xpath.length()>0 && xpath.endsWith("]"))
            {
                int i = xpath.lastIndexOf('@') + 1;
                int j = xpath.indexOf('=',i) ;
                if(i>0 && j>i)
                {
                    attributeName = xpath.substring(i,j).trim();
                    j = xpath.lastIndexOf('[');
                    if(j>0 && j<i)
                    {
                        xpath = xpath.substring(0,j);
                        j = xpath.lastIndexOf('/');
                        if(j>0 && j<xpath.length()-2)
                        {
                            elementName = xpath.substring(j+1).trim();
                            xpath = xpath.substring(0,j);
                        }
                    }
                }
                
            }
            
            if(attributeName!=null && elementName!=null) //is parsed successfully
            {
                //here we are to call ConfiBeans methods
                ConfigContext configContext;
                try
                {
                    String fileUrl  = instanceEnvironment.getConfigFilePath();
                    configContext   = ConfigFactory.createConfigContext(fileUrl);
                    ConfigBean bean = ConfigBeansFactory.getConfigBeanByXPath(configContext, xpath);
                    ConfigBean[] childs =  bean.getChildBeansByName(elementName);
                    for(int i=0; i<childs.length; i++)
                    {
                        String next = childs[i].getAttributeValue(attributeName);
                        if(next!=null)
                            hs.add(dottedName+"."+next);
                    }
                    
                }
                catch (ConfigException ce)
                {
                   sLogger.log(Level.FINE, MSG_EXCEPTION_DURING_LIST_NAMES, ce);
                }
                catch (NullPointerException npe) //ConfigBean returns this exception by many reasons
                {
                   sLogger.log(Level.FINE, MSG_EXCEPTION_DURING_LIST_NAMES, npe);
                }
            }
        }
        return (String[])hs.toArray(new String[hs.size()]);
    }
    
    synchronized static private MBeanNamingDescriptor[]  initConfigMBeanNaming() // throws MBeanConfigException
    {
       MBeanNamingDescriptor[] descrs = null;
       try
       {
               descrs = new MBeanNamingDescriptor[mbean_descriptions.length];
               for(int i=0; i<mbean_descriptions.length; i++)
               {
                   descrs[i] = new MBeanNamingDescriptor(mbean_descriptions[i]);
               }
       }
       catch (MBeanConfigException e)
       {
       }
       return descrs;
    }
    
/*    
    static private ConfigMBeanNamingInfo getMBeanNamingDescriptor(String type)
    {
        int idx = getTypeIndex(type);
        if(idx<0)
           return null;
        return m_mbeanDescr[idx];
    }
    
    static private int getTypeIndex(String type)
    {
       Integer i = (Integer)m_typeIndex.get(type);
       if(i==null)
           return -1;
       return i.intValue();
    }
*/
        
    public static void main(String args[])
    {
       testDottedName("myInstance.http-listener.myListener");
       testDottedName("myInstance.http-listener");
       testDottedName("myInstance.http-server");
       testDottedName("myInstance.http-server.mymy");
       testDottedName("myInstance.http-listeners.myListener");
       testDottedName("myInstance.http-serve");
       testDottedName("myInstance.http-server.http-listener.myListener");

        try
        {
           final String dmn = "com.sun.appserv";
            
           testObjectName( JMXUtil.newObjectName(dmn, "type=http-listener,instance-name=myServer,name=myListener"));
           testObjectName( JMXUtil.newObjectName(dmn, "type=http-listener,instance-name=myServer,name=myListener,chtoto=to"));
           testObjectName( JMXUtil.newObjectName(dmn, "type=http-service,instance-name=myServer,name=jhgv"));
           testObjectName( JMXUtil.newObjectName(dmn, "type=http-service,server-instances=myServer"));
           testObjectName( JMXUtil.newObjectName(dmn, "type=http-service,instance-name=myServer"));
           testObjectName( JMXUtil.newObjectName(dmn, "type=http-listener,name=myListener,instance-name=myServer"));
        }
        catch (Throwable e)
        {
            print(">>>>>>EXCEPTION: " + e);
            e.printStackTrace();
        }
    }

    private static void print(String str)
    {
        System.out.println(str);
    }
    
    private static void testDottedName(String dottedName)
    {
        try
        {
            print("\n\n\n>>>>>>test for dotted name: " + dottedName);
            ConfigMBeanNamingInfo mbi = new ConfigMBeanNamingInfo(dottedName);
            print("       ConfigMBeanNamingInfo =" + mbi);
            print("       ObjectName =" + mbi.getObjectName());
            print("       XPath      =" + mbi.getXPath());
        }
        catch (Throwable e)
        {
            print(">>>>>>EXCEPTION: " + e);
            //e.printStackTrace();
        }
            
    }
    private static void testObjectName(ObjectName name)
    {
        try
        {
            print("\n\n\n>>>>>>test for object name: " + name);
            ConfigMBeanNamingInfo mbi = new ConfigMBeanNamingInfo(name);
            print("       ConfigMBeanNamingInfo =" + mbi);
            print("       ObjectName =" + mbi.getObjectName());
            print("       XPath      =" + mbi.getXPath());
        }
        catch (Throwable e)
        {
            print(">>>>>>EXCEPTION: " + e);
            e.printStackTrace();
        }
            
    }
    
}
