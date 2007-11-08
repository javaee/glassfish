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
 *   $Id: AdminTest.java,v 1.3 2005/12/25 03:47:27 tcfujii Exp $
 *   @author: alexkrav
 *
 *   $Log: AdminTest.java,v $
 *   Revision 1.3  2005/12/25 03:47:27  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.2  2005/06/27 21:19:39  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.18  2004/11/14 07:04:15  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.17  2004/02/20 03:56:05  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.16.4.1  2004/02/02 07:25:12  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.16  2003/09/20 17:17:51  sridatta
 *   changing from standard-pe-config to server-config
 *   ql pass
 *   sqe smoke pass
 *   cts samples pass
 *
 *   Revision 1.15  2003/08/15 23:09:51  kravtch
 *   calls to notifyRegisterMBean/UnregisterMBean from posrRegister/postDeregister
 *   removeChild support is added;
 *   new test cases for dotted names testing
 *
 *   Revision 1.14  2003/08/14 23:01:36  kravtch
 *   test cases added  for:
 *      jvm-options (set/get String[] attribute);
 *      security-map;
 *      auth-realm;
 *
 *   Revision 1.13  2003/08/07 00:41:03  kravtch
 *   - new DTD related changes;
 *   - properties support added;
 *   - getDefaultAttributeValue() implemented for config MBeans;
 *   - merge Jsr77 and config activity in runtime mbeans;
 *
 *   Revision 1.12  2003/07/29 18:59:34  kravtch
 *   MBeanRegistryEntry:
 *   	- support for toFormatString();
 *   	- instantiateMBean() method modified to instantiate runtime MBeans as well;
 *   MBeanRegistryFactory:
 *   	- fixed bug in getRuntimeRegistry();
 *   MBeanNamingInfo:
 *   	- less strict requirements for parm_list_array size in constructor (can be more then needed);
 *   BaseRuntimeMBean:
 *   	- exception ClassCastException("Managed resource is not a Jsr77ModelBean") handling;
 *   ManagedJsr77MdlBean:
 *   	- call managed bean bug fixed ( getDeclaredMethod()->getMethod())
 *   admin/dtds/runtime-mbeans-descriptors.xml - modified to represent new runtime mbeans;
 *
 *   Revision 1.11  2003/07/18 20:14:42  kravtch
 *   1. ALL config mbeans are now covered by descriptors.xml
 *   2. new infrastructure for runtime mbeans is added
 *   3. generic constructors added to jsr77Mdl beans (String[])
 *   4. new test cases are added to admintest
 *   5. MBeanRegistryFactory has now different methods to obtain admin/runtime registries
 *   6. runtime-descriptors xml-file is added to build
 *
 *   Revision 1.10  2003/06/25 20:03:35  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin;

import javax.management.*;

import com.sun.enterprise.admin.meta.*;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class AdminTest
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int length = args.length;
/*        if (length < 2) {
            usage();
            System.exit(1);
        }
*/
     //******* call T E S T S *******
        runAdminMbeanTests();
//        runRuntimeMbeanTests();

    
    }

    static void runAdminMbeanTests()
    {
        println("Start....!");
        try 
        { 
            MBeanRegistry registry  = MBeanRegistryFactory.getMBeanRegistry("dtds/admin-mbeans-descriptors.xml");
            ConfigContext configContext = ConfigFactory.createConfigContext("/home/kravtch/domain.xml");
//           MBeanRegistry registry  = MBeanRegistryFactory.getMBeanRegistry("k:\\export\\ias\\admin-core\\admin\\dtds\\admin-mbeans-descriptors2.xml");
//            ConfigContext configContext = ConfigFactory.createConfigContext("k:\\domain2.xml");
            Object retObject;
            
registry.generateAndRegisterAllDottedNames(configContext, "abc.def.xyz");
if(configContext!=null)
   return;
//***********************************************************************************************
//            title("REGISTRY");

            title("REGISTRY (in XPath value order)");
            registry.sortRegistryEntries(registry.SORT_BY_XPATH);

//            println(registry.toFormatString());
//            println(registry.toString());
            String[] location;
            BaseAdminMBean mbean;
            AttributeList attrs;

            //***********************************************************************************************
            title("ejb-container INSTANTIATION");
            location = new String[]{"testdomain", "server-config"};
            mbean = registry.instantiateMBean("ejb-container", location, null, configContext); 
            println(""+mbean.getAttribute("cache_resize_quantity"));

            //***********************************************************************************************
            title("ejb-container set steady_pool_size to 20");
            printAllAttributes("************BEFORE SET ******", "   ", mbean);
            
            mbean.setAttribute(new Attribute("steady_pool_size", (Object)"20"));
            printAllAttributes("\n************AFTER SET******", "   ", mbean);

            mbean.setAttribute(new Attribute("steady_pool_size", (Object)"2"));

            //***********************************************************************************************
            title("resources Instantiate()");
            location = new String[]{"testdomain"};
            mbean = registry.instantiateMBean("resources", location, null, configContext); 
            
            //***********************************************************************************************
            title("resources-> getJdbcResource()");
            retObject = mbean.invoke("getJdbcResource", null, null); //new Object[]{}, new String[]{});
            printObj("Returned object:", "      ", retObject);
                    
            //***********************************************************************************************
            title("jdbc-resource-> Instantiate(jdbc/PointBase)");
            mbean = registry.instantiateMBean("jdbc-resource", new String[]{"testdomain","jdbc/__TimerPool"}, null, configContext); 
            println("returned jdbc-resource[jdbc/PointBase] mbean:"+mbean);

            //***********************************************************************************************
            title("jdbc-resource[jdbc/PointBase]-> getAttributes()");
   
            //***********************************************************************************************
            title("resources Instantiate()");
            location = new String[]{"testdomain"};
            mbean = registry.instantiateMBean("resources", location, null, configContext); 
            
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
            mbean = registry.instantiateMBean("custom-resource", new String[]{"testdomain","testJndiName2"}, null, configContext); 
            printObj("returned custom-resource[testJndiName2] mbean:",mbean);
            
            //***********************************************************************************************
            title("custom-resource[testJndiName2]-> getAttributes()");
            printAllAttributes("Attributes:", "   ", mbean);

            //***********************************************************************************************
            title("custom-resource-> Instantiate(testJndiName2) using ObjectName");
            mbean = registry.instantiateConfigMBean(new ObjectName("testdomain:type=custom-resource,jndi-name=testJndiName2,category=config"), null, configContext); 
            printObj("returned custom-resource[testJndiName2] mbean:",mbean);
            
            //***********************************************************************************************
            title("custom-resource[testJndiName2]-> getAttributes()");
            printAllAttributes("Attributes:", "   ", mbean);
            //***********************************************************************************************
/*            title("custom-resource[testJndiName2]-> setProperty()");
            setConfigMbeanProperty("testPropName1", "testPropValue1", mbean);
            setConfigMbeanProperty("testPropName2", "testPropValue2", mbean);
            setConfigMbeanProperty("testPropName3", "testPropValue3", mbean);
            setConfigMbeanProperty("testPropName4", "testPropValue4", mbean);
            
            //***********************************************************************************************
            title("custom-resource[testJndiName2]-> getProperties()");
            printAllProperties("Properties:", "      ", mbean);
*/
            //***********************************************************************************************
//title("custom-resource[testJndiName2]-> destroyConfigElement()");
//retObject = mbean.invoke("destroyConfigElement", null, null);
//println("Returned object after delete:"+retObject);

            title("resources-> getCustomResource()");
            location = new String[]{"testdomain"};
            mbean = registry.instantiateMBean("resources", location, null, configContext); 
            retObject = mbean.invoke("getCustomResource", null, null); //new Object[]{}, new String[]{});
            printObj("Returned object:","  ",retObject);
            title("resources-> removeCustomResourceByJndiName(testJndiName2)");
            retObject = mbean.invoke("removeCustomResourceByJndiName", new Object[]{"testJndiName2"}, new String[]{"java.lang.String"});
            printObj("Returned object:", "      ", retObject);
            
            //***********************************************************************************************
            title("resources-> getCustomResource()");
            location = new String[]{"testdomain"};
            mbean = registry.instantiateMBean("resources", location, null, configContext); 
            retObject = mbean.invoke("getCustomResource", null, null); //new Object[]{}, new String[]{});
            printObj("Returned object:","  ",retObject);
            
            //***********************************************************************************************
            title("applications-> Instantantiate()");
            location = new String[]{"testdomain"};
            mbean = registry.instantiateMBean("applications", location, null, configContext); 
            
/*            //***********************************************************************************************
            title("applications-> deployApplication()");
            retObject = mbean.invoke("deployApplication", new Object[]{"testName", "testLocation", null, null, null}, new String[]{"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String"});
            printObj("Returned object:", "      ", retObject);

            //***********************************************************************************************
            title("j2ee-application-> getAttributes()");
            location = new String[]{"testdomain","testName"};
            mbean = registry.instantiateMBean("j2ee-application", location, null, configContext); 
            printAllAttributes("Attributes:", "   ", mbean);

            //***********************************************************************************************
            title("j2ee-application-> destroyConfigElement()");
            retObject = mbean.invoke("destroyConfigElement", null, null);
            printObj("Returned object:", "      ", retObject);
            configContext.flush();
*/            
            //***********************************************************************************************
            title("thread-pools Instantiate()");
            location = new String[]{"testdomain", "server-config"};
            mbean = registry.instantiateMBean("thread-pools", location, null, configContext); 
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
            
            //***********************************************************************************************
            title("jdbc-connection-pool[PointBasePool] Instantiate()");
            location = new String[]{"testdomain", "PointBasePool"};
            mbean = registry.instantiateMBean("jdbc-connection-pool", location, null, configContext); 
            printObj("Returned object:","  ",mbean);
            //***********************************************************************************************
            title("jdbc-connection-pool[PointBasePool] PrintAllAttributes");
            printAllAttributes("Attributes:", "   ", mbean);
            //***********************************************************************************************
            title("jdbc-connection-pool[PointBasePool] PrintAllProperties");
            printAllProperties("Properties:", "   ", mbean);
            //***********************************************************************************************
            title("set properties: 'testPropName1' and 'testPropName2'");
            setConfigMbeanProperty("testPropName1", "testPropValue1", mbean);
            setConfigMbeanProperty("testPropName2", "testPropValue2", mbean);
            //***********************************************************************************************
            title("jdbc-connection-pool[PointBasePool] PrintAllProperties");
            printAllProperties("Properties:", "   ", mbean);
            //***********************************************************************************************
            title("remove properties: 'testPropName1' and 'testPropName2'");
            setConfigMbeanProperty("testPropName1", null, mbean);
            setConfigMbeanProperty("testPropName2", null, mbean);
            //***********************************************************************************************
            title("jdbc-connection-pool[PointBasePool] PrintAllProperties");
            printAllProperties("Properties:", "   ", mbean);
            //***********************************************************************************************
            title("jdbc-connection-pool.getDefaultAttributeValue[max-pool-size]");
            println("Value:"+mbean.getAttribute("max_pool_size"));
            retObject = mbean.invoke("getDefaultAttributeValue", new Object[]{"max_pool_size"}, new String[]{"java.lang.String"});
            println("Default:"+retObject);
            //***********************************************************************************************
            title("thread-pools Instantiate()");
            location = new String[]{"testdomain", "server-config"};
            mbean = registry.instantiateMBean("java-config", location, null, configContext); 
            printObj("Returned object:","  ",mbean);
            title("jvm-options - String[] attribute test");
            printObj("Value:", "   ", mbean.getAttribute("jvm_options"));
            title("jvm-options - String[] SETattribute test");
            mbean.setAttribute(new Attribute("jvm_options",new String[]{"abc","def","xyz"}));
            printObj("Value:", "   ", mbean.getAttribute("jvm_options"));
            //***********************************************************************************************
            title("auth-realm['file'] Instantiate()");
            location = new String[]{"testdomain", "server-config","file"};
            mbean = registry.instantiateMBean("auth-realm", location, null, configContext); 
            printObj("Returned object:","  ",mbean);
            title("auth-realm['file']-> getProperties()");
            printAllProperties("Properties:", "      ", mbean);
            title("auth-realm['file']-> AddUser()");
            
            String[] groups = new String[]{"testGroup1","testGroup2","testGroup3"};
            retObject = mbean.invoke("updateUser", new Object[]{"testUser", "testPassword", groups}, new String[]{"java.lang.String", "java.lang.String", groups.getClass().getName()});
            printObj("updateUser:Returned object:", "      ", retObject);
            retObject = mbean.invoke("getUserGroupNames", new Object[]{"testUser"}, new String[]{"java.lang.String"});
            printObj("getUserGroupNames:Returned object:", "      ", retObject);
            retObject = mbean.invoke("getGroupNames", new Object[]{}, new String[]{});
            printObj("getGroupNames:Returned object:", "      ", retObject);
            retObject = mbean.invoke("getUserNames", new Object[]{}, new String[]{});
            printObj("getUserNames:Returned object:", "      ", retObject);
            title("resources-> getCustomResource()");
            //***********************************************************************************************
            title("domain Instantiate()");
            location = new String[]{"testdomain"};
            mbean = registry.instantiateMBean("domain", location, null, configContext); 
            printObj("Returned object:","  ",retObject);
            title("domain-> removeResources");
            retObject = mbean.invoke("removeResources", null, null);
            printObj("Returned object:", "      ", retObject);
            
        }
        catch (MBeanException mbe)
        {
            //mbe.printStackTrace();
            Exception e = mbe.getTargetException();
            if(e instanceof InvocationTargetException)
            {
                InvocationTargetException ite = (InvocationTargetException)e;
                Throwable t = ite.getTargetException();
                println(t.getMessage());
                t.printStackTrace();
            }
            else
            {
                println(e.getMessage());
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            if(e instanceof InvocationTargetException)
            {
                InvocationTargetException ite = (InvocationTargetException)e;
                Throwable t = ite.getTargetException();
                println(t.getMessage());
                t.printStackTrace();
            }
            else
            {
                println(e.getMessage());
                e.printStackTrace();
            }
        }
        catch (Throwable t)
        {
                println(t.getMessage());
                t.printStackTrace();
        }
        println("Bye....!");
    }

    static void runRuntimeMbeanTests()
    {
        println("Start....!");
        try 
        {
            MBeanRegistry registry  = MBeanRegistryFactory.getMBeanRegistry("dtds/runtime-mbeans-descriptors.xml");
            ConfigContext configContext = ConfigFactory.createConfigContext("/home/kravtch/domain.xml");
            Object retObject;
            
            //***********************************************************************************************
            title("REGISTRY");
            println(registry.toString());
            String[] location;
            BaseAdminMBean mbean;
            AttributeList attrs;

        }
/*        catch (MBeanException mbe)
        {
            //mbe.printStackTrace();
            Exception e = mbe.getTargetException();
            if(e instanceof InvocationTargetException)
            {
                InvocationTargetException ite = (InvocationTargetException)e;
                Throwable t = ite.getTargetException();
                println(t.getMessage());
                t.printStackTrace();
            }
            else
            {
                println(e.getMessage());
                e.printStackTrace();
            }
        }
*/
        catch (Exception e)
        {
            if(e instanceof InvocationTargetException)
            {
                InvocationTargetException ite = (InvocationTargetException)e;
                Throwable t = ite.getTargetException();
                println(t.getMessage());
                t.printStackTrace();
            }
            else
            {
                println(e.getMessage());
                e.printStackTrace();
            }
        }
        catch (Throwable t)
        {
                println(t.getMessage());
                t.printStackTrace();
        }
        println("Bye....!");
    }

    private static void title(String str)
    {
        System.out.println("\n\n************* " + str + " *******************");
    }
    private static void println(String str)
    {
        System.out.println(str);
    }
    
    private static void printObj(String title, String pref, Object obj)
    {
            println(title);
            printObj(pref, obj);
    }
    
    private static void printObj(String pref, Object obj)
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
        else
        {
            println(pref  + obj);
        }
    }

    private static void printAllAttributes(String title, String pref, DynamicMBean mbean)
    {
            printObj(title, pref, mbean.getAttributes(new String[]{""}));
    }
    
    private static void printAllProperties(String title, String pref, DynamicMBean mbean) throws Exception
    {
        Object ret = mbean.invoke("getProperties", null, null);    
//            retObject = mbean.invoke("deployApplication", new Object[]{"testName", "testLocation", null, null, null}, new String[]{"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String"});
        printObj(title, pref, ret);
    }
    private static Object getConfigMbeanProperty(String name, DynamicMBean mbean) throws Exception
    {
        return mbean.invoke("getPropertyValue", new Object[]{name}, new String[]{"java.lang.String"});    
    }
    private static void setConfigMbeanProperty(String name, Object value, DynamicMBean mbean) throws Exception
    {
        Object ret = mbean.invoke("setProperty", new Object[]{new Attribute(name, value)}, new String[]{"javax.management.Attribute"});    
    }
}
