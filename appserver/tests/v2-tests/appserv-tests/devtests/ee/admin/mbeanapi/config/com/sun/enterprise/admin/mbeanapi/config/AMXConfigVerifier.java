/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.enterprise.admin.mbeanapi.config;

import com.sun.enterprise.admin.mbeanapi.common.AMXConnector;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.IIOPServiceConfig;
import com.sun.appserv.management.config.IIOPListenerConfig;

import javax.management.MBeanServerConnection;


import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.stringifier.StringifierRegistryIniterImpl;
import com.sun.appserv.management.util.stringifier.StringifierRegistryImpl;


/**
 *
 * @author alexkrav
 *         Date: Aug 23, 2004
 * @version $Revision: 1.3 $
 */
public class AMXConfigVerifier {
    private final DomainRoot	mDomainRoot;
    private final MBeanServerConnection	mMBeanServerConnection;

    public AMXConfigVerifier(final String host, final int port, final String adminUser, final String adminPassword, final boolean useTLS) throws Exception
    {
        final AMXConnector ct	= new AMXConnector( host, port, adminUser, adminPassword, useTLS );
        mDomainRoot	= ct.getDomainRoot();
        mMBeanServerConnection = ct.getAppserverConnectionSource().getMBeanServerConnection(false);

        testDomainElements();
        testHttpServiceElements();
    }
    


    public static void   main( final String[] args )
    {
        new StringifierRegistryIniterImpl( StringifierRegistryImpl.DEFAULT );

        try
        {
            new AMXConfigVerifier(System.getProperty("HOST", "localhost"),
                                        Integer.parseInt(System.getProperty("PORT","8686")),
                                        System.getProperty("ADMIN_USER", "admin"),
                                        System.getProperty("ADMIN_PASSWORD", "adminadmin"),
                                        Boolean.getBoolean("USE_TLS"));
        }
        catch( Throwable t )
        {
            ExceptionUtil.getRootCause( t ).printStackTrace();
        }
    }

    private ElemTester getElementTester( String str) throws Exception
    {
        return getElementTester(null,str);
    }

    private ElemTester getElementTester(AMXConfig master, String str) throws Exception
    {
        TestElement elem = new TestElement(str);
        ElemTester tester;
        if(master == null ){
            tester =  new ElemTester(mMBeanServerConnection, mDomainRoot, elem);
        }
        else {
            tester =  new ElemTester(mMBeanServerConnection, mDomainRoot, elem, master);
        }
        return tester;
    }
    
    private void runGenericTest(String str) throws Exception
    {
        runGenericTest(null, str);
    }

    private void runGenericTest(AMXConfig master, String str) throws Exception
        {
            getElementTester(master, str).runGenericTest();
        }


    private void testDomainElements() throws Exception
    {
        TestElemRegistry.initRegistry("server-config");

        //custom-resource test
        runGenericTest( "<custom-resource jndi-name=testCustomResource res-type=testType factory-class=java.lang.String>");
        //external-jndi-resource test
        runGenericTest( "<jndi-resource jndi-name=testExternalResource res-type=testType factory-class=java.lang.String jndi-lookup-name=testLookupName>");
        //mail-resource test
        runGenericTest( "<mail-resource jndi-name=testExternalResource host=localhost user=shmuser from=anotherUser>");
        //persistence-manager-factory-resource test
        runGenericTest( "<persistence-manager-factory-resource jndi-name=testPersistResource>");
        //jdnc-connection-pool test
        runGenericTest( "<jdbc-connection-pool name=testJdbcPool datasource-classname=java.lang.String>");
        //connector-connection-pool test
        runGenericTest( "<connector-connection-pool-resource name=testConnectorPool resource-adapter-name=testAdapterName connection-definition-name=testDefinitionName>");

        //jdbc-resource test
        ElemTester poolTester = getElementTester( "<jdbc-connection-pool name=testPool datasource-classname=java.lang.String>");
        poolTester.createElement();
        runGenericTest( "<jdbc-resource jndi-name=testJdbcResource pool-name=testPool description=testDescr>");
        poolTester.deleteElement();

        //connector-resource test
        ElemTester poolTester2 = getElementTester( "<connector-connection-pool-resource name=testConnectorPool2 resource-adapter-name=testAdapterName2 connection-definition-name=testDefinitionName>");
        poolTester2.createElement();
        runGenericTest( "<connector-resource jndi-name=testConnectorResource pool-name=testConnectorPool description=testDescr>");
        poolTester2.deleteElement();

        // resource-adapter test
        runGenericTest( "<resource-adapter resource-adapter-name=testResourceAdapterName2>");

        // admin-object-resource test
        runGenericTest( "<admin-object-resource jndi-name=testAdminObjectName res-type=testResType res-adapter=testResourceAdapterName>");
    }
        
    private void testHttpServiceElements() throws Exception
    {
        TestElemRegistry.initRegistry("server-config");
        
        runGenericTest( "<virtual-server id=testVirtualServer hosts=localhost>");
        
        ElemTester virtServer = getElementTester( "<virtual-server id=testVirtualServer2 hosts=localhost>");
        virtServer.createElement();

        runGenericTest( "<http-listener id=testHttpListener address=localhost port=7171 default-virtual-server=testVirtualServer2 server-name=testServerName\"\">");
        virtServer.deleteElement();
        final AMXConfig iiopService = ((ConfigConfig)mDomainRoot.getDomainConfig().getConfigConfigMap().get("server-config")).getIIOPServiceConfig();
        runGenericTest(iiopService, "<iiop-listener id=testIIOPListener address=localhost port=1234>");

        ElemTester iiopListenerTester = getElementTester("<iiop-listener id=testIIOPListener address=localhost port=1234>");
        iiopListenerTester.createElement();
        final AMXConfig iiopListener = (AMXConfig) ((IIOPServiceConfig)iiopService).getIIOPListenerConfigMap().get("testIIOPListener");
        runGenericTest(iiopListener, "<ssl cert-nickname=iiopCertNick>");
    }
}
