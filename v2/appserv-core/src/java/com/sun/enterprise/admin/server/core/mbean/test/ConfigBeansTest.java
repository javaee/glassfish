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

package com.sun.enterprise.admin.server.core.mbean.test;

//JDK imports
import java.io.*;
import java.util.*;

//JMX imports
import javax.management.*;

//Admin imports
import com.sun.enterprise.admin.common.*;
import com.sun.enterprise.admin.common.constant.*;
import com.sun.enterprise.admin.util.*;
import com.sun.enterprise.admin.server.core.jmx.*;
import com.sun.enterprise.admin.server.core.mbean.config.*;
import com.sun.enterprise.admin.server.core.mbean.meta.*;
import com.sun.enterprise.admin.server.core.*;
public class ConfigBeansTest
{
    
    
    /**
     * @param args the command line arguments
     */
    
    public static void main(String args[])
    {
        ConfigBeansTest testMain = new ConfigBeansTest();
        testMain.test();
    }
    
    private void printLine(String str)
    {
        System.out.println(str);
    }
    
    private void printAttrList(AttributeList attrList, String title)
    {
        Iterator it = attrList.iterator();
        printLine("\n\n\n##################  " + title +"  ##################");
        AdminLocalStringsManager messages = AdminLocalStringsManager.createMessagesManager(this);
        while (it.hasNext())
        {
            Attribute attribute = (Attribute) it.next();
            String name =  attribute.getName();
            Object value = attribute.getValue();
            printLine(messages.getString("mbeanstest.attrvalue", "======DO NOT KNOW=====", new Object[]{name,value}));
            printLine("Attribute name="+name+" value="+value);
        }
    }
    
    private ObjectName getObjectName(String instance, String type, String name) throws Exception
    {
        String objname = "ias:" +
                         ObjectNames.kServerInstanceKeyName + "=" + instance + "," +
                         ObjectNames.kTypeKeyName + "=" + type;
        if(name!=null)
        {
            objname += ("," + ObjectNames.kNameKeyName + "=" + name);
        }
//printLine("OBJNAME="+objname);
        return new ObjectName(objname);
    }
    
    private String maskDots(String instance)
    {
        int idx = 0;
        while((idx=instance.indexOf('.', idx))>=0)
        {
            instance = instance.substring(0,idx)+'\\'+instance.substring(idx);
            idx += 2;
        }
        return instance;
    }
    
    private void test()
    {
        try
        {
            String instance = "ias1";
            String maskedInstance = maskDots(instance);

            /*FileInputStream file = new FileInputStream("/export2/test/test.property");
            Properties props = new Properties(System.getProperties());
            props.load(file);
            System.setProperties(props);
            System.getProperty("com.sun.aas.instanceRoot");*/
            AdminServiceLifeCycle aslc = new AdminServiceLifeCycle();
            aslc.onInitialization(null);
            MBeanServer		mbs				= SunoneInterceptor.getMBeanServerInstance();
            GenericConfigurator genConf = new GenericConfigurator();
//   		mbs.registerMBean(genConf, new ObjectName("ias:type=configurator")); //a server-instance

            AttributeList attrList;

            
            // ############# ejbs ##################
            ManagedJ2EEEjbJarModule ejbmod = new ManagedJ2EEEjbJarModule(instance, "app", "helloworldEjb.jar");
            {
                String[] arr = ejbmod.getEnterpriseBeans();
                for(int j=0; j<arr.length; j++)
                   printLine("&&&&&&&&&+++++> "+arr[j]);  
            }

            // ############# Standalone application ##################
            ManagedJ2EEApplication app = new ManagedJ2EEApplication(instance, "app");
            {
                String[] arr = app.getModules();
                for(int j=0; j<arr.length; j++)
                   printLine("*************+++++> "+arr[j]);  
                arr = app.getEjbModules();
                for(int j=0; j<arr.length; j++)
                   printLine("------> "+arr[j]);  
                arr = app.getModules();
                for(int j=0; j<arr.length; j++)
                   printLine("*************+++++> "+arr[j]);  
            }
if(genConf!=null)
    return;
            attrList = genConf.getGenericAttributes(new String[]{maskedInstance + ".application.app.*"});
            printAttrList(attrList, "Standalone application TEST");
if(genConf!=null)
    return;

            //########### HTTPService TEST  ##################
            ManagedHTTPService httpService = new ManagedHTTPService(instance);
            genConf.setGenericAttribute(maskedInstance + ".httpservice.bandwidthLimit", "TESTbandWidthLimit");
            attrList = genConf.getGenericAttributes(new String[]{maskedInstance + ".httpservice.*"});
            printAttrList(attrList, "HTTPService TEST");
            /** Ramakanth. Signature changed. 03/28/2002. 6:47pm
    httpService.createHTTPListener("myID","myADDRESS",new Integer(123),new Boolean(false),
             "myFAMILY", new Integer(456), new Boolean(false), new Boolean(false));
             */
if(genConf!=null)
    return;

            // ############# JDBCResource ##################
            ManagedJDBCResource jdbc = new ManagedJDBCResource(instance,"name1");
//            mbs.registerMBean(jdbc, getObjectName(instance, ObjectNames.kJdbcResourceType, "name1/name1"));
//          genConf.setGenericAttribute(maskedInstance + ".jdbc.name1-name1.log", "TESTVALUE2");
            attrList = genConf.getGenericAttributes(new String[]{maskedInstance + ".jdbc.name1.*"});
            printAttrList(attrList, "JDBC TEST");
	    // ############# JDBCConnectionPool ##################
            ManagedJDBCConnectionPool jdbcpool = new ManagedJDBCConnectionPool(instance,"pool1");
//            mbs.registerMBean(jdbcpool, getObjectName(instance, ObjectNames.kJdbcConnectionPoolType, "pool1"));
            genConf.setGenericAttribute(maskedInstance + ".jdbcpool.pool1.url", "TESTURL2");
            attrList = genConf.getGenericAttributes(new String[]{maskedInstance + ".jdbcpool.pool1.*"});
            printAttrList(attrList, "JDBCConnectionPool TEST");


 //if(mbs!=null)
//     return;
            
            // ############# ORB ##################
            ManagedORBComponent orb = new ManagedORBComponent(instance);
//            mbs.registerMBean(orb, getObjectName(instance, ObjectNames.kOrbType, null)); //a server-instance
            genConf.setGenericAttribute(maskedInstance + ".orb.max", new Integer(333));
            attrList = genConf.getGenericAttributes(new String[]
            {maskedInstance + ".orb.max"});
            printAttrList(attrList, "ORB max TEST");
            attrList = genConf.getGenericAttributes(new String[]{maskedInstance + ".orb.*"});
            printAttrList(attrList, "ORB TEST");
            
	    // ############# ORBListener ##################
//	    orb.createOrbListener("myTestId5", "nowhere", null, null, new Integer(12345));
            ManagedORBListener orbListener = new ManagedORBListener(instance, "myTestId5");
              // orbListener.createSsl("MYcertNickname", null, null, null, null, null, null, null);
	    
//	    orbListener.createAuthDb("MYauthID", "MYdatabase", "MYbasedn", "MYcertmaps");
            
//            mbs.registerMBean(orbListener, getObjectName(instance, ObjectNames.kOrbListenerType, "myTestId5")); //a server-instance
            genConf.setGenericAttribute(maskedInstance + ".orblistener.myTestId5.address", "TESTADDRESS2");
            attrList = genConf.getGenericAttributes(new String[]{maskedInstance + ".orblistener.myTestId5.*"});
            printAttrList(attrList, "ORBListener TEST");
            

	   //########### JNDI  ##################
          
        ManagedJNDIResource jndi = new ManagedJNDIResource(instance, "myjndi");
//            mbs.registerMBean(jndi, getObjectName(instance, ObjectNames.kJndiResourceType, "myjndi"));
            genConf.setGenericAttribute(maskedInstance + ".jndi.myjndi.description", "my test description");
            attrList = genConf.getGenericAttributes(new String[] {maskedInstance + ".jndi.myjndi.*"});
            printAttrList(attrList, "JNDI Resource test");
          
            
            // ############# Transaction service ##################
            ManagedTransactionService transService =  new ManagedTransactionService(instance);
 //           mbs.registerMBean(transService, getObjectName(instance, ObjectNames.kJtsComponent, null));
            genConf.setGenericAttribute(maskedInstance + ".transaction.transactionLogDir", "logDir");
            attrList = genConf.getGenericAttributes(new String[] {maskedInstance + ".transaction.*"});
           printAttrList(attrList, "Transaction TEST");

	   // ########### MDB Container ################
           ManagedMdbContainer MdbContainer = new ManagedMdbContainer(instance);
 //          mbs.registerMBean(MdbContainer, getObjectName(instance, ObjectNames.kMdbContainer, null));
            genConf.setGenericAttribute(maskedInstance + ".mdbcontainer.queueConnectionFactoryJndiName", "jndi1");
            attrList =genConf.getGenericAttributes(new String[] {maskedInstance + ".mdbcontainer.*"});
           printAttrList(attrList, "MDB Container test");

	   //########### EJB Container ##################
          
            ManagedEjbContainer EjbContainer = new ManagedEjbContainer(instance);
//            mbs.registerMBean(EjbContainer, getObjectName(instance, ObjectNames.kEjbContainer, null));
            genConf.setGenericAttribute(maskedInstance + ".ejbcontainer.minBeansInPool", new Integer(20));
            attrList = genConf.getGenericAttributes(new String[] {maskedInstance + ".ejbcontainer.*"});
           printAttrList(attrList, "EJB Container test");

	   //########### WEB Container ##################
          
            ManagedWebContainer WebContainer = new ManagedWebContainer(instance);
//            mbs.registerMBean(WebContainer, getObjectName(instance, ObjectNames.kWebContainer, null));
            genConf.setGenericAttribute(maskedInstance + ".ejbcontainer.minBeansInPool", new Integer(20));
            attrList = genConf.getGenericAttributes(new String[] {maskedInstance + ".webcontainer.*"});
           printAttrList(attrList, "WEB Container test");

	   //########### JVM  ##################
          
            ManagedJVM Jvm = new ManagedJVM(instance);
//            mbs.registerMBean(Jvm, getObjectName(instance, ObjectNames.kJvmType, null));
//            genConf.setGenericAttribute(maskedInstance + ".jvm.javahome", "/tools/java");
            attrList = genConf.getGenericAttributes(new String[] {maskedInstance + ".jvmconfig.*"});
           printAttrList(attrList, "JVM  test");
          
            // ############# SecurityService ##################
//        ManagedSecurityService sec = new ManagedSecurityService(instance);
//            mbs.registerMBean(jdbcpool, getObjectName(instance, ObjectNames.kJdbcConnectionPoolType, "pool1"));
            attrList = genConf.getGenericAttributes(new String[]{maskedInstance + ".security.*"});
            printAttrList(attrList, "SecurityService TEST");

            // ############# Standalone application ##################
//            ManagedStandaloneJ2EEApplication app = new ManagedStandaloneJ2EEApplication(instance, "app");
            attrList = genConf.getGenericAttributes(new String[]{maskedInstance + ".application.app.*"});
            printAttrList(attrList, "Standalone application TEST");

            // ############# Standalone EjbJar module ##################
//            ManagedStandaloneJ2EEEjbJarModule ejb = new ManagedStandaloneJ2EEEjbJarModule(instance, "ejb");
            attrList = genConf.getGenericAttributes(new String[]{maskedInstance + ".ejbmodule.ejb.*"});
            printAttrList(attrList, "Standalone EjbJar module TEST");
            
            // ############# Standalone Web Module ##################
//            ManagedStandaloneJ2EEWebModule sec = new ManagedStandaloneJ2EEWebModule(instance, "web");
            attrList = genConf.getGenericAttributes(new String[]{maskedInstance + ".webmodule.web.*"});
            printAttrList(attrList, "Standalone Web Module TEST");
            
            // ############# Standalone Connector Module ##################
//            ManagedStandaloneConnectorModule sec = new ManagedStandaloneConnectorModule(instance, "conn");
            attrList = genConf.getGenericAttributes(new String[]{maskedInstance + ".connectormodule.conn.*"});
            printAttrList(attrList, "Standalone Connector Module TEST");
        }
        catch(MBeanException m)
        {
            printLine(m.getMessage());
            Exception t = m.getTargetException();
            while(t instanceof MBeanException)
            {
                t = ((MBeanException)t).getTargetException();
            }
            printLine(t.getMessage());
            printLine(t.toString());
            m.printStackTrace();
        }
        catch(Throwable e)
        {
            printLine(e.getMessage());
            printLine(e.toString());
            e.printStackTrace();
        }
    }
    
    
}
