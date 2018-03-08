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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;

import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.AMXConfig;


/**
 * generic named config element tester 
 * @author alexkrav
 *         Date: Aug 23, 2004
 * @version $Revision: 1.11 $
 */
public class ElemTester {
    private final AMXConfig           mMasterConfig;
    private final TestElement           mElement;
    private final MBeanServerConnection mConnection;
    static final String REPORT_PREFIX           = "                 ";
    static final String CHECK_OK_PREFIX         = "        check-ok:";
    static final String CHECK_FAILURE_PREFIX    = "CHECK-FAILED:";

    public ElemTester(MBeanServerConnection connection, DomainRoot domainRoot, TestElement element) throws Exception
    {
        mConnection      = connection;
        mElement         = element;
        mMasterConfig    = mElement.getMasterAMXConfigForElement(domainRoot);
        String msg = "\n\n------- tester created for  "+mElement.getElementName();
        printObj(msg, mElement.getAttributesMapCopy()); 
    }

    public ElemTester(final MBeanServerConnection connection, final DomainRoot domainRoot, final TestElement element,
                      final AMXConfig masterConfig ) throws Exception
    {
        mConnection      = connection;
        mElement         = element;
        mMasterConfig    = masterConfig;
        String msg = "\n\n------- tester created for  "+mElement.getElementName();
        printObj(msg, mElement.getAttributesMapCopy());
    }

    void println(String str)
    {
        System.out.println(str);
    }
    
    void printObj(String str, Object obj)
    {
        ConfigTestHelper.printObj(str, obj);
    }
    
    public Object createElement()  throws Exception
    {
        Object ret = null;
        
        final boolean	isNamedElement	= false;
        
        if( isNamedElement ){
            ret = ConfigTestHelper.invokeInMgr(mMasterConfig, "create",
                     mElement.getCreationParams(),
                     mElement.getCreationClasses());
            println(REPORT_PREFIX +"create(...)");
        }
        else {
            ret = ConfigTestHelper.invokeInMgr(mMasterConfig, "create"+
                    ConfigTestHelper.camelize(mElement.getElementName())+"Config",
                     mElement.getCreationParams(),
                     mElement.getCreationClasses());
            println(REPORT_PREFIX +"createXXXConfig(...)");
        }

        return ret;
    }
    public void deleteElement()  throws Exception
    {
        String key = mElement.getElementKey();
        
        final boolean	isNamedElement	= key != null;
        
        if( isNamedElement ){
            ConfigTestHelper.invokeInMgr(mMasterConfig, "remove",
                     new Object[] {key});
            println(REPORT_PREFIX +"remove()");
        }
        else{
            ConfigTestHelper.invokeInMgr(mMasterConfig, "remove"+
                    ConfigTestHelper.camelize(mElement.getElementName())+"Config",
                     null);
            println(REPORT_PREFIX +"removeXXXConfig()");
        }

    }
   
	public ObjectName getElemMBeanObjectName()  throws Exception
    {
        return mElement.getElemMBeanObjectName();
    }


    
    public Map list() 
    {
        try{
            println(REPORT_PREFIX +"list()");
            return  (Map)ConfigTestHelper.invokeInMgr(mMasterConfig, "get"+
                        ConfigTestHelper.camelize(mElement.getElementName())+"ConfigMap");
        } catch(Exception e)
        {
            println("Exception during list() operation");
            return null;
        }
    }
    
    public void cleanExceptionIfMatched(String parseStr)
    {
        if(ConfigTestHelper._lastException!=null &&
            (parseStr==null ||   
            ((String)(""+ConfigTestHelper.getLastExceptionShortMsg())).indexOf(parseStr)>=0))
        {
           ConfigTestHelper._lastException = null;
        }
    }
    
    public boolean checkNoException(String description)
    {
        if(ConfigTestHelper._lastException!=null)
        {
           println(CHECK_FAILURE_PREFIX +  "("+description+") " + ConfigTestHelper.getLastExceptionShortMsg());
           return false;
        }
        return true;
    }
    
    
    public boolean checkList(String description) throws Exception
    {
        Map map = list();
        if(!checkNoException(description+ "[during list operation]"))
           return false;
        boolean bOk = true;
        String keyName = mElement.getElementKeyName();
        Set objectNamesSet  = mConnection.queryNames(mElement.getElemMBeanObjectNamePattern(), null);
        Iterator iter = objectNamesSet.iterator();
        while(iter.hasNext())
        {
            String keyValue = ((ObjectName)iter.next()).getKeyProperty(keyName);
            if(map.get(keyValue)==null)
            {
                println(CHECK_FAILURE_PREFIX + "("+description+") element "+  mElement.getElementName() +"."+keyValue +
                      " not found by list command");
                bOk = false;
            }
            else
            {
                map.remove(keyValue);
                println(REPORT_PREFIX +"check list element \"" + keyValue + "\" OK");
            }

        }
        iter = map.keySet().iterator();
        while(iter.hasNext())
        {
                println(CHECK_FAILURE_PREFIX + "("+description+") element "+  mElement.getElementName() +
                        "."+ iter.next() + "reported by list command but not found by queryNames()");
                bOk = false;
        }
        if(bOk)
        {
            println(CHECK_OK_PREFIX + "("+description+") element "+  mElement.getElementName() + " list() is checked");
        }
        return bOk;
    }
    
    private ArrayList getAttributesNamesFromMBeanInfo() throws Exception
    {
        ArrayList list = new ArrayList();
        MBeanInfo mbInfo = mConnection.getMBeanInfo(getElemMBeanObjectName());
        MBeanAttributeInfo[] attrInfos = mbInfo.getAttributes();
        for(int i=0; i<attrInfos.length; i++)
        {
            list.add(attrInfos[i].getName());
        }
        return list;
    }
    
    public boolean checkAttributes(String description, Object amxBean) throws Exception
    {
//        if(!checkNoException(description))
//           return false;
        ArrayList attrNames = getAttributesNamesFromMBeanInfo();
        AttributeList attrsInConfig  = mConnection.getAttributes(getElemMBeanObjectName(), new String[]{""});  
        HashMap elemAttrsFromConfig = new HashMap();
        for(int i=0; i<attrsInConfig.size(); i++)
        {
            Attribute   attr = (Attribute)attrsInConfig.get(i);
            elemAttrsFromConfig.put(attr.getName(), attr.getValue());
        }

        Map elemAttrsFromAMXAttrMap = mElement.getAttributesMapCopy();
        boolean bOk = true;
        for(int i=0; i<attrNames.size(); i++)
        {
            String      attrName = (String)attrNames.get(i);        
            Object      valueInConfig       = elemAttrsFromConfig.get(attrName);
            Object      valueInElem         = elemAttrsFromAMXAttrMap.get(attrName);
            if( (valueInConfig==null && valueInElem!=null) ||
                (valueInElem!=null && valueInConfig!=null && !valueInElem.equals(valueInConfig) ) )
            { //FIRST - check EXPECTED with RECEIVED_FROM MBeanServer.GetAttributes()
                println(CHECK_FAILURE_PREFIX + "("+description+") element "+  mElement.getElementName() +" attribute: "+attrName+
                        ": value \"" + valueInElem + "\"(expected) differs from \""+
                        valueInConfig + "\"(in config MBean)");
                bOk = false;
            }
            else
            {   //THEN - check EXPECTED with the result of AMX...getXXX()
                if(amxBean!=null)
                {
                    valueInConfig = ConfigTestHelper.getBeanAttribute(amxBean, attrName);
                    if(checkNoException("get \"" + attrName + "\""))
                    {
                        if( (valueInConfig==null && valueInElem!=null) ||
                            (valueInElem!=null && valueInConfig!=null && !valueInElem.equals(valueInConfig) ) )
                        {
                            println(CHECK_FAILURE_PREFIX + "("+description+") element "+  mElement.getElementName() +" attribute: "+attrName+
                                    ": value \"" + valueInElem + "\"(expected) differs from \""+
                                    valueInConfig + "\"(in AMX MBean)");
                            bOk = false;
                        }
                        else
                        {
                            println(REPORT_PREFIX +"check attribute \"" + attrName + "="+valueInConfig+"\" OK");
                        }
                    }
                }
                else
                {
                    println(REPORT_PREFIX +"check attribute \"" + attrName + "="+valueInConfig+"\" OK");
                }
            }
        }
        if(bOk)
        {
            println(CHECK_OK_PREFIX + "("+description+") element "+  mElement.getElementName() + " attributes checked");
        }
        return bOk;
    }
    
    public boolean checkExist(String description) 
    {
        if(!checkNoException(description))
           return false;
        String key = mElement.getElementKey();
        Map list = list();
        if(list!=null && (list.get(key))!=null)
        {
            println(CHECK_OK_PREFIX + "("+description+")"+ mElement.getElementName() + "." + key  + " exists");
            return true;
        }
        println(CHECK_FAILURE_PREFIX + "("+description+")"+ mElement.getElementName() + "." + key  + " is not exist");
        return false;
    }

    public boolean checkNotExist(String description)
    {
        if(!checkNoException(description))
           return false;
        String key = mElement.getElementKey();
        Map list = list();
        if(list!=null && (list.get(key))!=null)
        {
            println(CHECK_FAILURE_PREFIX + "("+description+")"+ mElement.getElementName() + "." + key  + " exists");
            return true;
        }
        println(CHECK_OK_PREFIX + "("+description+")"+ mElement.getElementName() + "." + key  + " is not exist");
        return false;
    }

    public void runGenericTest()
    {
        try{
            println("--- generic test");
            deleteElement();
//TEMPORARY            
    //if(ConfigTestHelper._lastException!=null)
    //    ConfigTestHelper._lastException.printStackTrace();
//checkNoException("testting phase");
//cleanExceptionIfMatched(null);
            cleanExceptionIfMatched("is not found");
            checkNotExist("after pre-deletion");

            Object created  = createElement();
            if(checkExist("after creation"))
                checkAttributes("attributes-check", created);

            println("WHAT TO DO WITH NON-EXISTENT \"mElemMgr instanceof AMXConfigMgr\"????");
			/*
            if(mElemMgr instanceof AMXConfigMgr)
                checkList("list operation");
            */

            deleteElement();
//if(ConfigTestHelper._lastException!=null)
//    ConfigTestHelper._lastException.printStackTrace();
            checkNotExist("after final-deletion");
            
        } catch (Exception e)
        {
           //e.printStackTrace();
           ConfigTestHelper.printStackTrace(e);
        }
    }
}

