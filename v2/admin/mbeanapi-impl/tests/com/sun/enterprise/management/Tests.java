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
package com.sun.enterprise.management;


import java.util.List;
import java.util.ArrayList;

import com.sun.appserv.management.util.misc.TypeCast;

import com.sun.enterprise.management.TestTemplateTest;
import com.sun.enterprise.management.DomainRootTest;
import com.sun.enterprise.management.GenericsTest;

import com.sun.enterprise.management.base.*;
import com.sun.enterprise.management.config.*;
import com.sun.enterprise.management.monitor.*;
import com.sun.enterprise.management.client.*;
import com.sun.enterprise.management.support.*;
import com.sun.enterprise.management.helper.*;
import com.sun.enterprise.management.deploy.*;
import com.sun.enterprise.management.j2ee.*;
import com.sun.enterprise.management.ext.logging.*;
import com.sun.enterprise.management.ext.wsmgmt.*;
import com.sun.enterprise.management.ext.offline.*;
import com.sun.enterprise.management.util.misc.*;



/**
	<b>The place</b> to put list any new test; the official list
	of tests.  The file amxtest.classes is also used, but since
	it may be inadvertantly modified, this is the official list
	of tests.
 */
public class Tests 
{
	private	Tests()	{}
	
	private static final Class<junit.framework.TestCase>[]	TestClasses	=
	    TypeCast.asArray( new Class[]
	{
	    TestTemplateTest.class, // ensure that the template one works OK, too!
	    
	    
	    // these tests are standalone and do not require a
	    // server connection
        GenericsTest.class,
	    AMXDebugTest.class,
        SetUtilTest.class,
        ThrowableMapperTest.class,
        OfflineDottedNamesRegistryTest.class,
        OfflineDottedNamePrefixesTest.class,
        LogQueryEntryImplTest.class,
        LogQueryResultImplTest.class,
        ParamNameMapperTest.class,
        CircularListTest.class,
        SerializableTest.class,
        StatisticTest.class,
        CoverageInfoTest.class,

        //  Tests that follow require a server connection
		//AppserverConnectionSourceTest.class,
	    RunMeFirstTest.class,
		
		ComSunAppservMonitorTest.class,
        ProxyTest.class,
        ProxyFactoryTest.class,
        AMXTest.class,
		GetSetAttributeTest.class,
        ContainerTest.class,
        GenericTest.class,
        PropertiesAccessTest.class,
        SystemPropertiesAccessTest.class,

        LogMBeanTest.class,
        LoggingTest.class,
        LoggingHelperTest.class,
        StatefulLoggingHelperTest.class,

        DomainRootTest.class,
        UploadDownloadMgrTest.class,
        BulkAccessTest.class,
        QueryMgrTest.class,
        NotificationEmitterServiceTest.class,
        NotificationServiceMgrTest.class,
        NotificationServiceTest.class,
        MiscTest.class,

        MonitorTest.class,
        JMXMonitorMgrTest.class,

        J2EETest.class,
        ServletTest.class,

        DeploymentProgressTest.class,
        DeploymentSourceTest.class,
        DeploymentStatusTest.class,
        DeploymentMgrTest.class,

		DanglingRefsTest.class,
		ConfigRunMeFirstTest.class,
		DescriptionTest.class,
		EnabledTest.class,
		LibrariesTest.class,
		RefHelperTest.class,
		ListenerTest.class,
		ComSunAppservConfigTest.class,
        DomainConfigTest.class,
        ConfigConfigTest.class,
        SecurityServiceConfigTest.class,
        MessageSecurityConfigTest.class,
        StandaloneServerConfigTest.class,
        ClusteredServerConfigTest.class,
        NodeAgentConfigTest.class,
        CustomMBeanConfigTest.class,
        LifecycleModuleConfigTest.class,
        ReferencesTest.class,
        HTTPServiceConfigTest.class,
        HTTPListenerConfigTest.class,
        ClusterConfigTest.class,
        SSLConfigTest.class,
        JMXConnectorConfigTest.class,
        IIOPListenerConfigTest.class,
        HTTPListenerConfigTest.class,
        AuditModuleConfigTest.class,
        AuthRealmConfigTest.class,
        JavaConfigTest.class,
        ProfilerConfigTest.class,
        VirtualServerConfigTest.class,
        JACCProviderConfigTest.class,
        AdminObjectResourceConfigTest.class,
        JDBCResourceConfigTest.class,
        MailResourceConfigTest.class,
        ConnectorConnectionPoolConfigTest.class,
        JDBCConnectionPoolConfigTest.class,
        PersistenceManagerFactoryResourceConfigTest.class,
        JNDIResourceConfigTest.class,
        ThreadPoolConfigTest.class,
        LBTest.class,
        ManagementRulesConfigTest.class,
        SecurityMapConfigTest.class,
        ConnectorConnectionPoolConfigTest.class,
        ResourceAdapterConfigTest.class,
        CustomResourceConfigTest.class,
        ConnectorServiceConfigTest.class,
        ManagementRulesConfigTest.class,
        DiagnosticServiceConfigTest.class,

        PerformanceTest.class,
	    CallFlowMonitorTest.class,
	    RunMeLastTest.class,
	} );
	
		public static List<Class<junit.framework.TestCase>>
	getTestClasses()
	{
	    final List<Class<junit.framework.TestCase>> classes   =
	        new ArrayList<Class<junit.framework.TestCase>>();
	    
	    for( int i = 0; i < TestClasses.length; ++i )
	    {
	        final Class<junit.framework.TestCase> testClass   = TestClasses[i];
	        
	        classes.add( testClass );
	    }

		return( classes );
	}
	
};

