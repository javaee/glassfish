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

/*
 * $Id: AdminInfraTest.java,v 1.1.1.1 2005/05/28 00:39:19 dpatil Exp $
 */

package com.sun.enterprise.admin;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

//junit imports
import junit.framework.*;
import junit.textui.TestRunner;

//JMX
import javax.management.DynamicMBean;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.management.AttributeNotFoundException;
//config imports
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigContext;

//config mbean imports
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
//import com.sun.enterprise.admin.meta.*;

public class AdminInfraTest extends TestCase
{
    static String TEST_DIR = "publish/internal/testclasses/asadmintest";
    static int COMPARESAMPLES_MODE   = 0;
    static int CREATESAMPLES_MODE    = 1;
    static int PRINT_MODE            = 2;

    private static int _mode = COMPARESAMPLES_MODE;
    private MBeanRegistry _registry;
    private ConfigContext _configContext;
    private PrintWriter      _printStream; //for CREATESAMPLES_MODE only
    private LineNumberReader _compareStream; //for COMPARE_MODE only
    private AdminTester _tester; // = new AdminTester(_mode, _registry, _configContext);

    
    public AdminInfraTest(String name) throws Exception
    {
        super(name);
    }

    protected void setUp()
    {
        System.setProperty("com.sun.aas.installRoot", TEST_DIR);
        System.setProperty("com.sun.aas.instanceRoot","/qq");
        System.setProperty("com.sun.aas.javaRoot","/qq");
        System.setProperty("com.sun.aas.imqLib","/qq");
        try
        {
           /* URL url = AdminInfraTest.class.getResource("/testfiles/descriptors.xml");  //standard for pe
            InputStream stream = url.openStream();
            _registry = new MBeanRegistry();
            _registry.loadMBeanRegistry(stream);*/

            _registry  = MBeanRegistryFactory.getMBeanRegistry(TEST_DIR+"/descriptors.xml", false);
            MBeanRegistryFactory.setAdminMBeanRegistry(_registry);
            _configContext = ConfigFactory.createConfigContext(TEST_DIR+"/test.xml");
            _tester = new AdminTester(this, _mode, _registry, _configContext, TEST_DIR);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    protected void tearDown()
    {
        _registry       = null;
        _configContext  = null;
        _printStream    = null;
        _compareStream  = null;
    }

    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite(AdminInfraTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception
    {
        if(args==null || args.length<2)
        args = new String[]{"-testpath", "publish/internal/testclasses/asadmintest", "create"};
        int length = args.length;
        if(length<2 ||
           !args[0].equals("-testpath"))
        {
            System.out.println("usage: admininfratest -testpath <path to test directory> [print|create]");
            System.exit(1);
        }
        TEST_DIR = args[1];
        if(length>2)
        {    
            System.out.println("MODE="+args[2]);
            if(args[2].trim().equals("print"))
                _mode=2;
            else if(args[2].trim().equals("create"))
                _mode=1;
        }
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(AdminInfraTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }

    

    //******************************************************************
    //******************MBeanServer emulation***************************
    //******************************************************************
    private Object getAttribute(String onName, String attr) throws Exception
    {
        mbean = _registry.instantiateConfigMBean(new ObjectName(onName), null, _configContext); 
        return mbean.getAttribute(attr);
    }
    
    //******************************************************************
    //******************T E S T S***************************************
    //******************************************************************

    private void runTestCase(String testCaseName, String sampleName) throws Exception
    {
        String errStr = _tester.runTestCase(testCaseName, sampleName);
        assertNull(errStr, errStr);
    }

    private void runTestCase(String[] testCaseNames, String sampleName) throws Exception
    {
        String errStr = _tester.runTestCase(testCaseNames, sampleName);
        assertNull(errStr, errStr);
    }

    private void runTestCase(String testCaseName) throws Exception
    {
        runTestCase(testCaseName, testCaseName);
    }

    public void testMBeanRegistry() throws Exception
    {
        runTestCase("testMBeanRegistry");
    }
    
    String[] location;
    BaseAdminMBean mbean;
    AttributeList attrs;
    Object retObject;
    
    public void testMBeansInstantiation() throws Exception
    {
        //***********************************************************************************************
        location = new String[]{"testdomain", "server-config"};
        mbean = _registry.instantiateMBean("ejb-container", location, null, _configContext); 
        assertNotNull("ejb-container INSTANTIATION (type+location): mbean==null", mbean);
        assertEquals("cache_resize_quantity not equal 32", mbean.getAttribute("cache_resize_quantity"), "32");

        //***********************************************************************************************
        mbean = _registry.instantiateConfigMBean(new ObjectName("testdomain:type=ejb-container,config=server-config,category=config"), 
                    null, _configContext); 
        assertNotNull("ejb-container INSTANTIATION (ObjectName): mbean==null", mbean);
        
        //***********************************************************************************************
        mbean = _registry.instantiateConfigMBean(new ObjectName("testdomain:type=config,name=server-config,category=config"), 
                    null, _configContext); 
        assertNotNull("config INSTANTIATION By ObjectName: mbean==null", mbean);
//_tester.testMBeansInstantiation();
//runTestCase("testMBeansInstantiation");
    }

    public void testMBeansGetAttribue() throws Exception
    {
        //***********************************************************************************************
        location = new String[]{"testdomain", "server-config"};
        mbean = _registry.instantiateMBean("ejb-container", location, null, _configContext); 
        assertNotNull("ejb-container INSTANTIATION (type+location): mbean==null", mbean);
        
        assertEquals("cache_resize_quantity not equal 32", mbean.getAttribute("cache_resize_quantity"),  "32");
        assertEquals("cache_idle_timeout_in_seconds not equal 600", mbean.getAttribute("cache_idle_timeout_in_seconds"), "600");
        //enumeration
        assertEquals("commit-option != B", mbean.getAttribute("commit_option"), "B");
        
        try 
        {
            mbean.getAttribute("wrong_attribute");
            fail("wrong_attribute did not cause exception");
        } catch (AttributeNotFoundException anfe)
        {
        }
        try 
        {
            //scase sensibility test
            mbean.getAttribute("commit_Option");
            fail("getAttribute():sensibility test failure");
        } catch (AttributeNotFoundException anfe)
        {
        }
        
    }

/*    public void testMBeansCreateChild() throws Exception
    {
    }*/
    public void testGetSetCompareCases() throws Exception
    {
        runTestCase(new String[]{"testMBeansGettersSetters"}, "GettersSetters");
    }
    
    public void testMBeansSetAttribue() throws Exception
    {
        //***********************************************************************************************
        location = new String[]{"testdomain", "server-config"};
        mbean = _registry.instantiateMBean("ejb-container", location, null, _configContext); 
        assertNotNull("ejb-container INSTANTIATION (type+location): mbean==null", mbean);
        
        mbean.setAttribute(new Attribute("cache_resize_quantity",  "35"));
        assertEquals("cache_resize_quantity not equal 35", mbean.getAttribute("cache_resize_quantity"),  "35");
        mbean.setAttribute(new Attribute("cache_resize_quantity",  "36"));
        assertEquals("cache_resize_quantity not equal 36", mbean.getAttribute("cache_resize_quantity"),  "36");
        mbean.setAttribute(new Attribute("cache_idle_timeout_in_seconds", "601"));
        assertEquals("cache_idle_timeout_in_seconds not equal 601", mbean.getAttribute("cache_idle_timeout_in_seconds"), "601");
        mbean.setAttribute(new Attribute("commit_option", "C"));
        assertEquals("commit_option != C", mbean.getAttribute("commit_option"), "C");
    }

    public void testChildCompareCases() throws Exception
    {
        runTestCase(new String[]{"testChildOperations"}, "childOperations");
    }
    public void testArrayAttrs() throws Exception
    {
        runTestCase(new String[]{"testMBeanArrayAttrs"}, "testArrayAttrs");
    }
    public void testProperties() throws Exception
    {
        runTestCase(new String[]{"testPropertiesOperations"}, "testPropertiesOperations");
    }
    public void testDefaultValues() throws Exception
    {
        runTestCase(new String[]{"testDefaultValues"}, "testDefaultValues");
    }
    public void testAttributeEmptyValuesDuringCreation() throws Exception
    {
        //***********************************************************************************************
        location = new String[]{"testdomain", "server-config"};
        mbean = _registry.instantiateMBean("http-service", location, null, _configContext); 
        assertNotNull("http-service INSTANTIATION (type+location): mbean==null", mbean);
        attrs = new AttributeList();
        attrs.add(new Attribute("address","0.0.0.0"));
        attrs.add(new Attribute("default-virtual-server","server"));
        attrs.add(new Attribute("server-name",""));
        attrs.add(new Attribute("id","test-listener"));
        attrs.add(new Attribute("port","12345"));
        attrs.add(new Attribute("enabled","false"));
        attrs.add(new Attribute("security-enabled","false"));
        attrs.add(new Attribute("redirect-port",""));
        retObject = mbean.invoke("createHttpListener", new Object[]{attrs}, new String[]{attrs.getClass().getName()});
        assertNotNull("createHttpListener: retObject==null", retObject);
        //***********************************************************************************************
        location = new String[]{"testdomain", "server-config", "test-listener"};
        mbean = _registry.instantiateMBean("http-listener", location, null, _configContext); 
        assertNotNull("http-listener INSTANTIATION (type+location): mbean==null", mbean);
        assertEquals("server-name attribute value is not \"empty\" as expected", mbean.getAttribute("server-name"),"");
        assertNull("server-name attribute value is not null as expected", mbean.getAttribute("redirect-port"));
        _configContext.flush();       
    }

}
