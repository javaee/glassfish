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

package com.sun.enterprise.admin;

import javax.management.*;

import com.sun.enterprise.admin.meta.*;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;

//junit imports
import junit.framework.*;
import junit.textui.TestRunner;

public class AdminTester
{
   static int COMPARESAMPLES_MODE   = 0;
   static int CREATESAMPLES_MODE    = 1;
   static int PRINT_MODE            = 2;
   
   static int _mode = COMPARESAMPLES_MODE;
   static String SAMPLES_DIR = "samples";
   
    TestCase _caller;
    MBeanRegistry _registry;
    ConfigContext _configContext;
    static PrintWriter      _printStream; //for CREATESAMPLES_MODE only
    static LineNumberReader _compareStream; //for COMPARE_MODE only
 
    public AdminTester(TestCase caller, int mode, MBeanRegistry registry, ConfigContext configContext, String testpath)
    {
        _caller = caller;
        _mode=mode;
        _registry=registry;
        _configContext=configContext;
        SAMPLES_DIR = testpath+"/samples";
    }
    public static void println(String str) throws Exception
    {
        int idx = 0;
        while((idx=str.indexOf('\n'))>=0)
        {
            printSplitedLine(str.substring(0,idx));
            str = str.substring(idx+1);
        }
        printSplitedLine(str);
    }
    public static void printSplitedLine(String str) throws Exception
    {
       if(_mode==CREATESAMPLES_MODE)
           _printStream.println(str);
       else if(_mode==PRINT_MODE)
            System.out.println(str);
       if(_mode==COMPARESAMPLES_MODE)    
       {
if(_compareStream==null)
{
//    System.out.println("--- _compareStream = "+_compareStream);
//    System.out.println("---"+ str );
    return;
}

           String compStr = _compareStream.readLine();
           String str0 = str;
           String compStr0 = compStr;
           if(!str.equals(compStr))
              throw new AdminTestException( _compareStream.getLineNumber(), str, compStr);
       }
    }
    public static void title(String str) throws Exception
    {
        println("\n\n************* " + str + " *******************");
    }
    
    public static void printObj(String title, String pref, Object obj) throws Exception
    {
            println(title);
            printObj(pref, obj);
    }
    
    public static void printObj(String pref, Object obj) throws Exception
    {
        if(pref==null)
            pref="";
        if(obj==null)
            println(pref+"null");
        if(obj instanceof Object[])
        {
            Object[] objs = (Object[])obj;
            if(objs.length==0)
                println(pref+"array.length = 0");
            for(int i=0; i<objs.length; i++)
            {
                printObj(pref+" ["+i+"] -> ", objs[i]);
            }
        }
        else if(obj instanceof ArrayList)
        {
            ArrayList objs = (ArrayList)obj;
            if(objs.size()==0)
                println(pref+"list.size() = 0");
            for(int i=0; i<objs.size(); i++)
            {
                printObj(pref/*+" ["+i+"] -> "*/, objs.get(i));
            }
        }
        else if(obj instanceof Attribute)
        {
            println(pref+ ((Attribute)obj).getName() + "=" + ((Attribute)obj).getValue());
        }
        else if(obj instanceof BaseAdminMBean)
        {
            String str = obj.toString();
            //cut the address
            int idx = str.lastIndexOf('@');
            if(idx>=0)
                str = str.substring(0, idx);
            println(pref+ str);
        }
        else
        {
            println(pref  + obj);
        }
    }

    public static void printAllAttributes(String title, String pref, DynamicMBean mbean) throws Exception
    {
            printObj(title, pref, mbean.getAttributes(new String[]{""}));
    }
    
    public static void printAllProperties(String title, String pref, DynamicMBean mbean) throws Exception
    {
        Object ret = mbean.invoke("getProperties", null, null);    
//            retObject = mbean.invoke("deployApplication", new Object[]{"testName", "testLocation", null, null, null}, new String[]{"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String"});
        printObj(title, pref, ret);
    }
    public static Object getConfigMbeanProperty(String name, DynamicMBean mbean) throws Exception
    {
        return mbean.invoke("getPropertyValue", new Object[]{name}, new String[]{"java.lang.String"});    
    }
    public static void setConfigMbeanProperty(String name, Object value, DynamicMBean mbean) throws Exception
    {
        Object ret = mbean.invoke("setProperty", new Object[]{new Attribute(name, value)}, new String[]{"javax.management.Attribute"});    
    }

    public String runTestCase(String testName, String sampleName) throws Exception
    {
        return runTestCase(new String[]{testName}, sampleName);
    }
    public String runTestCase(String[] testNames, String sampleName) throws Exception
    {
        if(_mode==CREATESAMPLES_MODE)
        {
            if(_printStream!=null)
            {
                _printStream.close();
                _printStream=null;
            }
            _printStream = new PrintWriter(new FileOutputStream(SAMPLES_DIR+"/"+sampleName+".smp"));
        }
        else if(_mode==COMPARESAMPLES_MODE)
        {
            if(_compareStream!=null)
            {
                _compareStream.close();
                _compareStream=null;
            }
            if(sampleName!=null)
                _compareStream = new LineNumberReader(new FileReader(SAMPLES_DIR+"/"+sampleName+".smp"));
        }
        for(int i=0; i<testNames.length; i++)
        {
            Class cl = this.getClass();
            Method method = cl.getMethod(testNames[i], (Class[])null);
            try {
                method.invoke(this, (Object[])null);
            } catch (InvocationTargetException ite)
            {
                Throwable t = ite.getTargetException();
                if(t instanceof AdminTestException)
                {
                   return testNames[i]+": "+t.getMessage();
                }
                throw ite;
            }
        }
        if(_mode==CREATESAMPLES_MODE)
        {
            if(_printStream!=null)
            {
                _printStream.close();
                _printStream=null;
            }
        }
        else if(_mode==COMPARESAMPLES_MODE)
        {
            if(_compareStream!=null)
            {
                _compareStream.close();
                _compareStream=null;
            }
        }
        return null;

    }

    String[] location;
    BaseAdminMBean mbean;
    AttributeList attrs;
    Object retObject;
    
    public void testMBeanRegistry() throws Exception
    {
        _registry.sortRegistryEntries(_registry.SORT_BY_NAME);
        println(_registry.toString());
    }

    //***********************************************************************************************
    //**** TestCase emulators ****
    private void assertNull(String str, Object obj) throws Exception
    {
        if(_caller!=null)
            _caller.assertNull(str, obj);
    }
    private void assertEquals(String str, Object obj, Object sample) throws Exception
    {
        if(_caller!=null)
            _caller.assertEquals(str, obj, sample);
    }
    //***********************************************************************************************
    
    
    public void testMBeansInstantiation() throws Exception
    {
        //***********************************************************************************************
        title("ejb-container INSTANTIATION (type+location)");
        location = new String[]{"testdomain", "server-config"};
        mbean = _registry.instantiateMBean("ejb-container", location, null, _configContext); 
        assertNull("MBean Instaniation: mbean==null", mbean);
        println(""+mbean.getAttribute("cache_resize_quantity"));
        assertEquals("not equal", mbean.getAttribute("cache_resize_quantity"), new Integer(33));
        //***********************************************************************************************
        title("ejb-container INSTANTIATION (ObjectName)");
        mbean = _registry.instantiateConfigMBean(new ObjectName("testdomain:type=ejb-container,config=server-config,category=config"), 
                    null, _configContext); 
        println(""+mbean.getAttribute("cache_resize_quantity"));
        //***********************************************************************************************
        title("ejb-container INSTANTIATION (ObjectName)");
        mbean = _registry.instantiateConfigMBean(new ObjectName("testdomain:type=ejb-container,config=server-config,category=config"), 
                    null, _configContext); 
        println(""+mbean.getAttribute("cache_resize_quantity"));
        //***********************************************************************************************
        title("config INSTANTIATION By ObjectName");
        mbean = _registry.instantiateConfigMBean(new ObjectName("testdomain:type=config,name=server-config,category=config"), 
                    null, _configContext); 
        println("" + ((mbean!=null)?"ok":"failed"));
    }
    public void testMBeansInfo() throws Exception
    {
    }
    public void testMBeansGettersSetters() throws Exception
    {
        //***********************************************************************************************
        title("ejb-container INSTANTIATION");
        location = new String[]{"testdomain", "server-config"};
        mbean = _registry.instantiateMBean("ejb-container", location, null, _configContext); 
        println(""+mbean.getAttribute("cache_resize_quantity"));

        //***********************************************************************************************
        title("ejb-container set steady_pool_size to 20");
        printAllAttributes("************BEFORE SET ******", "   ", mbean);

        mbean.setAttribute(new Attribute("steady_pool_size", (Object)"20"));
        printAllAttributes("\n************AFTER SET******", "   ", mbean);
    }
    public void testChildOperations() throws Exception
    {
            //***********************************************************************************************
            title("resources Instantiate()");
            location = new String[]{"testdomain"};
            mbean = _registry.instantiateMBean("resources", location, null, _configContext); 
            
            //***********************************************************************************************
            title("resources-> getCustomResource()");
            retObject = mbean.invoke("getCustomResource", null, null); //new Object[]{}, new String[]{});
            printObj("Returned object:", "      ", retObject);

            //***********************************************************************************************
            title("resources-> createCustomResource(testJndiName2/testResType2/testFactoryClass2)");
            attrs = new AttributeList();
            attrs.add(new Attribute("jndi_name", "testJndiName2"));
            attrs.add(new Attribute("res_type", "testResType2"));
            attrs.add(new Attribute("factory_class", "testFactoryClass2"));
            printObj("Input Attributes:", "   ", attrs);
//          retObject = mbean.invoke("createCustomResource", new Object[]{attrs, null, null}, 
//                      new String[]{attrs.getClass().getName(),"java.util.Properties","java.lang.String"});                                
            retObject = mbean.invoke("createCustomResource", new Object[]{attrs}, new String[]{attrs.getClass().getName()});
            printObj("Returned object:", "      ", retObject);
            //***********************************************************************************************
            title("resources-> getCustomResourceByJndiName(testJndiName2)");
            retObject = mbean.invoke("getCustomResourceByJndiName", new Object[]{"testJndiName2"}, new String[]{"java.lang.String"});
            printObj("Returned object:", "      ", retObject);
            
            //***********************************************************************************************
            title("resources-> getCustomResource()");
            retObject = mbean.invoke("getCustomResource", null, null); //new Object[]{}, new String[]{});
            printObj("Returned object:", "      ", retObject);

            //***********************************************************************************************
            title("custom-resource-> Instantiate(testJndiName2)");
            mbean = _registry .instantiateMBean("custom-resource", new String[]{"testdomain","testJndiName2"}, null, _configContext); 
            printObj("returned custom-resource[testJndiName2] mbean:",mbean);
            
            //***********************************************************************************************
            title("custom-resource[testJndiName2]-> getAttributes()");
            printAllAttributes("Attributes:", "   ", mbean);

            //***********************************************************************************************
            title("custom-resource-> Instantiate(testJndiName2) using ObjectName");
            mbean = _registry .instantiateConfigMBean(new ObjectName("testdomain:type=custom-resource,jndi-name=testJndiName2,category=config"), null, _configContext); 
            printObj("returned custom-resource[testJndiName2] mbean:",mbean);
            
            //***********************************************************************************************
            title("custom-resource[testJndiName2]-> getAttributes()");
            printAllAttributes("Attributes:", "   ", mbean);
            title("resources-> getCustomResource()");
            location = new String[]{"testdomain"};
            mbean = _registry .instantiateMBean("resources", location, null, _configContext); 
            retObject = mbean.invoke("getCustomResource", null, null); //new Object[]{}, new String[]{});
            printObj("Returned object:","  ",retObject);
            title("resources-> removeCustomResourceByJndiName(testJndiName2)");
            retObject = mbean.invoke("removeCustomResourceByJndiName", new Object[]{"testJndiName2"}, new String[]{"java.lang.String"});
            printObj("Returned object:", "      ", retObject);
            
            //***********************************************************************************************
            title("resources-> getCustomResource()");
            location = new String[]{"testdomain"};
            mbean = _registry .instantiateMBean("resources", location, null, _configContext); 
            retObject = mbean.invoke("getCustomResource", null, null); //new Object[]{}, new String[]{});
            printObj("Returned object:","  ",retObject);
            
    }
    public void testPropertiesOperations() throws Exception
    {
            //***********************************************************************************************
            title("Domain MBean Instantiate()");
            location = new String[]{"testdomain"};
            mbean = _registry.instantiateMBean("domain", location, null, _configContext); 
            //***********************************************************************************************
            title("Domain -> setProperty()");
            setConfigMbeanProperty("testPropName1", "testPropValue1", mbean);
            setConfigMbeanProperty("testPropName2", "testPropValue2", mbean);
            setConfigMbeanProperty("testPropName3", "testPropValue3", mbean);
            setConfigMbeanProperty("testPropName4", "testPropValue4", mbean);
            
            //***********************************************************************************************
            title("Domain -> getProperties()");
            printAllProperties("Properties:", "      ", mbean);

    }
    public void testDottedNamesProducers() throws Exception
    {
    }
    public void testDefaultValues() throws Exception
    {
            //***********************************************************************************************
            title("Domain MBean Instantiate()");
            location = new String[]{"testdomain"};
            mbean = _registry.instantiateMBean("domain", location, null, _configContext); 
/* NEEDS TO BE INVESTIGATED for FCS why not working
            printObj("Returned object:","  ",mbean);
            title("getDefaultAttributeValues('http-listener', null) test");
            retObject = mbean.invoke("getDefaultAttributeValues", new Object[]{"http-listener", null}, new String[]{"java.lang.String", (new String[0]).getClass().getName()});
            printObj("Returned object:", "      ", retObject);
            title("getDefaultAttributeValues('ejb-container', null) test");
            retObject = mbean.invoke("getDefaultAttributeValues", new Object[]{"ejb-container", null}, new String[]{"java.lang.String", (new String[0]).getClass().getName()});
            printObj("Returned object:", "      ", retObject);
 */
    }
    public void testMBeanArrayAttrs() throws Exception
    {
            //***********************************************************************************************
            title("thread-pools Instantiate()");
            location = new String[]{"testdomain", "server-config"};
            mbean = _registry.instantiateMBean("thread-pools", location, null, _configContext); 
            printObj("Returned object:","  ",mbean);
            title("thread-pools-> createThreadPool");
            attrs = new AttributeList();
            attrs.add(new Attribute("thread_pool_id", "mytestThreadPool"));
            attrs.add(new Attribute("min_thread_pool_size", "100"));
            attrs.add(new Attribute("max_thread_pool_size", "200"));
            attrs.add(new Attribute("num_work_queues", "12"));
            attrs.add(new Attribute("idle_thread_timeout_in_seconds", "50"));
            printObj("Input Attributes:", "   ", attrs);
            retObject = mbean.invoke("createThreadPool", new Object[]{attrs}, new String[]{attrs.getClass().getName()});
            printObj("Returned object:", "      ", retObject);
    }
            
}
