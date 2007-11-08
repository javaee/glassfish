/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * $Id: OfflineConfigTest.java,v 1.1.1.1 2005/05/28 00:39:19 dpatil Exp $
 */

package com.sun.enterprise.admin.config;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import org.testng.annotations.*;

//junit imports
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.TestResult;
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
import com.sun.enterprise.admin.config.OfflineConfigMgr;

public class OfflineConfigTest extends TestCase
{
    static String TEST_DIR = "build/internal/testclasses/asadmintest";

    private OfflineConfigMgr _mgr; // = new AdminTester(_mode, _registry, _configContext);
    
    public OfflineConfigTest(String name) throws Exception
    {
        super(name);
    }

    @Configuration(beforeTestClass = true)
    protected void setUp()
    {
 /*       System.setProperty("com.sun.aas.installRoot", TEST_DIR);
        System.setProperty("com.sun.aas.instanceRoot","/qq");
        System.setProperty("com.sun.aas.javaRoot","/qq");
        System.setProperty("com.sun.aas.imqLib","/qq");
  */
        try
        {
           _mgr = new OfflineConfigMgr(TEST_DIR+"/domain.xml");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Configuration(afterTestClass = true)
    protected void tearDown()
    {
         _mgr = null;
    }

    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite(OfflineConfigTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception
    {
        int length = args.length;
        if(length<2 ||
           !args[0].equals("-testpath"))
        {
            System.out.println("usage: offlineconfigtest -testpath <path to test directory> [print|create]");
            System.exit(1);
        }
        TEST_DIR = args[1]; 
 System.out.println("TEST_DIR = "+TEST_DIR );       
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(OfflineConfigTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }

    

    
    //******************************************************************
    //******************HELPERS***************************************
    //******************************************************************
    private void printTestTitle(String title)
    {
        System.out.println("\n/***********************************************************/");
        System.out.println("         TEST: "+ title);
        System.out.println("/**********************************************************/");
    }
    private void printAttributes(String title, AttributeList attrs)
    {
        if(title!=null)
            System.out.println(title);
        for(int i=0; i<attrs.size(); i++)
        {
            Attribute attr = (Attribute)attrs.get(i);
            System.out.println("         "+ 
                    attr.getName() + " = " + attr.getValue());
        }
    }
    private void printList(String title, ArrayList list)
    {
        if(title!=null)
            System.out.println(title);
        for(int i=0; i<list.size(); i++)
        {
            System.out.println("         " + list.get(i));
        }
    }

    //******************************************************************
    //******************T E S T S***************************************
    //******************************************************************
    @Test(groups = {"OfflineConfigTests"})
    public void testOfflineMgr() throws Exception
    {
        assertNotNull("_mgr==null", _mgr);
    }
    
    @Test(groups = {"OfflineConfigTests"})
    public void testGetWildcardAttributes() throws Exception
    {
        printTestTitle("testGetWildcardAttributes");
        AttributeList attrs = _mgr.getAttributes("domain.*");
        printAttributes("     ====== get domain.*", attrs);
        attrs = _mgr.getAttributes("server-config.*");
        printAttributes("     ====== get server-config.*", attrs);
        attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.*");
        printAttributes("     ====== server-config.http-service.virtual-server.server.*", attrs);
    }

    @Test(groups = {"OfflineConfigTests"})
    public void testGetAttributes() throws Exception
    {
        printTestTitle("testGetAttributes");
        AttributeList attrs = _mgr.getAttributes("domain.log-root");
        printAttributes("     ====== get domain.log-root", attrs);
        attrs = _mgr.getAttributes("server-config.dynamic-reconfiguration-enabled");
        printAttributes("     ====== get server-config.dynamic-reconfiguration-enabled", attrs);
        attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.id");
        printAttributes("     -====== server-config.http-service.virtual-server.server.id", attrs);
    }
    
    @Test(groups = {"OfflineConfigTests"})
    public void testGetProperties() throws Exception
    {
        printTestTitle("testGetProperties");
        AttributeList attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.property.accesslog");
        printAttributes("     ====== server-config.http-service.virtual-server.server.property.accesslog", attrs);
        attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.property.chubaka");
        printAttributes("     ====== server-config.http-service.virtual-server.server.property.chubaka", attrs);
    }
    
    @Test(groups = {"OfflineConfigTests"})
    public void testGetWildcardProperties() throws Exception
    {
        printTestTitle("testGetWildcardProperties");
        AttributeList attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.property.*");
        printAttributes("     ====== server-config.http-service.virtual-server.server.property.*", attrs);
    }

    @ExpectedExceptions({com.sun.enterprise.admin.AdminValidationException.class})
    @Test(groups = {"OfflineConfigTests"})
    public void testSetAttribute() throws Exception
    {
        printTestTitle("testSetAttribute");
        AttributeList attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.state");
        printAttributes("     ====== get before set ", attrs);
        attrs = _mgr.setAttribute("server-config.http-service.virtual-server.server.state", "qq");
        printAttributes("     ====== set server-config.http-service.virtual-server.server.state=qq", attrs);
        attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.state");
        printAttributes("     ====== get after set", attrs);
    }

    @Test(groups = {"OfflineConfigTests"})
    public void testSetProperty() throws Exception
    {
        printTestTitle("testSetProperty");
        AttributeList attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.property.accesslog");
        printAttributes("     ====== get before set ", attrs);
        attrs = _mgr.setAttribute("server-config.http-service.virtual-server.server.property.accesslog", "aaa");
        printAttributes("     ====== set server-config.http-service.virtual-server.server.property.accesslog=aaa", attrs);
        attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.property.accesslog");
        printAttributes("     ====== get after set", attrs);
    }

    @ExpectedExceptions({com.sun.enterprise.admin.AdminValidationException.class})
    @Test(groups = {"OfflineConfigTests"})
    public void testServerTargetedConfigGetSet() throws Exception
    {
        AttributeList attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.id");
        printAttributes("     ====== get by server", attrs);
        attrs = _mgr.getAttributes("server.http-service.virtual-server.server.state");
        printAttributes("     ====== get before set by server", attrs);
        attrs = _mgr.setAttribute("server.http-service.virtual-server.server.state", "qq2");
        printAttributes("     ====== set server.http-service.virtual-server.server.state=qq2", attrs);
        attrs = _mgr.getAttributes("server.http-service.virtual-server.server.state");
        printAttributes("     ====== get after set by server", attrs);
        attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.state");
        printAttributes("     ====== get after set (by server-config)", attrs);
    }
    
    @Test(groups = {"OfflineConfigTests"})
    public void testAddProperty() throws Exception
    {
        printTestTitle("testAddProperty");
        AttributeList attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.property.qwerty");
        printAttributes("     ====== get before set ", attrs);
        attrs = _mgr.setAttribute("server-config.http-service.virtual-server.server.property.qwerty", "zzz");
        printAttributes("     ====== set server-config.http-service.virtual-server.server.property.qwerty=zzz", attrs);
        attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.property.qwerty");
        printAttributes("     ====== get after set", attrs);
    }
    
    @Test(groups = {"OfflineConfigTests"})
    public void testDeleteProperty() throws Exception
    {
        printTestTitle("testAddProperty");
        AttributeList attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.property.qwerty");
        printAttributes("     ====== get before set ", attrs);
        attrs = _mgr.setAttribute("server-config.http-service.virtual-server.server.property.qwerty", null);
        printAttributes("     ====== set server-config.http-service.virtual-server.server.property.qwerty=null", attrs);
        attrs = _mgr.getAttributes("server-config.http-service.virtual-server.server.property.qwerty");
        printAttributes("     ====== get after set", attrs);
    }

    @Test(groups = {"OfflineConfigTests"})
    public void testGetListDottedNames() throws Exception
    {
        printTestTitle("testGetListDottedNames");
        ArrayList list = _mgr.getListDottedNames("*");
        printList("*", list);
    }
}