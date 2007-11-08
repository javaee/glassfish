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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/**
	Generated: Fri Jun 25 19:01:55 PDT 2004
	Generated from:
	com.sun.appserv:type=jms-service,config=default-config,category=config
	com.sun.appserv:type=jms-service,config=server-config,category=config
*/

package com.sun.enterprise.management.support.oldconfig;

import java.util.Map;
import java.util.List;
import java.util.Properties;
import javax.management.ObjectName;
import javax.management.AttributeList;

public interface OldApplicationsConfigMBean extends OldProperties
{
	public void	associate( final String param1, final String param2 );
	public void	associateApplication( final Properties param1, final String param2 );
	public ObjectName	createAppclientModule( final AttributeList attribute_list );
	//public DeploymentStatus	createApplicationReference( final String param1, final String param2, final Map param3 );
	//public DeploymentStatus	createApplicationReference( final String param1, final boolean param2, final String param3, final String param4 );
	public Map	createApplicationReferenceAndReturnStatusAsMap( final String param1, final String param2, final Map param3 );
	public ObjectName	createConnectorModule( final AttributeList attribute_list );
	public ObjectName	createEjbModule( final AttributeList attribute_list );
	public ObjectName	createJ2eeApplication( final AttributeList attribute_list );
	public ObjectName	createLifecycleModule( final AttributeList attribute_list );
	public ObjectName	createLifecycleModule( final AttributeList param1, final String param2 );
	public ObjectName	createLifecycleModule( final AttributeList param1, final Properties param2, final String param3 );
	//public DeploymentStatus	createLifecycleModuleReference( final String param1, final String param2, final Map param3 );
	public String	createMBean( final String param1, final String param2 );
	public String	createMBean( final String param1, final Map param2 );
	public String	createMBean( final String param1, final Map param2, final Map param3 );
	public void	createMBeanRef( final String param1, final String param2 );
	public ObjectName	createMbean( final AttributeList attribute_list );
	public ObjectName	createWebModule( final AttributeList attribute_list );
	//public DeploymentStatus	deleteApplicationReference( final String param1, final String param2 );
	//public DeploymentStatus	deleteApplicationReference( final String param1, final String param2, final Map param3 );
	public Map	deleteApplicationReferenceAndReturnStatusAsMap( final String param1, final String param2, final Map param3 );
	public String	deleteMBean( final String param1, final String param2 );
	public void	deleteMBeanRef( final String param1, final String param2 );
	//public DeploymentStatus	deploy( final Properties param1 );
	//public DeploymentStatus	deploy( final Properties param1, final String[] param2 );
	public boolean	deployConnectorModule( final Properties param1 );
	public boolean	deployEJBJarModule( final Properties param1 );
	public boolean	deployJ2EEApplication( final Properties param1 );
	public boolean	deployWarModule( final Properties param1 );
	public boolean	destroyConfigElement();
	public void	disable( final String param1, final String param2, final String param3 );
	public void	disassociate( final String param1, final String param2 );
	public void	disassociateApplication( final Properties param1, final String param2 );
	public void	enable( final String param1, final String param2, final String param3 );
	public boolean	existsMBean( final String param1, final String param2 );
	public javax.management.ObjectName[]	getAllDeployedAppclientModules( final String param1 );
	public javax.management.ObjectName[]	getAllDeployedComponents();
	public javax.management.ObjectName[]	getAllDeployedComponents( final String param1 );
	public javax.management.ObjectName[]	getAllDeployedConnectors();
	public javax.management.ObjectName[]	getAllDeployedConnectors( final String param1 );
	public javax.management.ObjectName[]	getAllDeployedEJBModules();
	public javax.management.ObjectName[]	getAllDeployedEJBModules( final String param1 );
	public javax.management.ObjectName[]	getAllDeployedJ2EEApplications();
	public javax.management.ObjectName[]	getAllDeployedJ2EEApplications( final String param1 );
	public javax.management.ObjectName[]	getAllDeployedWebModules();
	public javax.management.ObjectName[]	getAllDeployedWebModules( final String param1 );
	public String[]	getAllSystemConnectors();
	public javax.management.ObjectName[]	getAllUserDeployedComponents();
	public javax.management.ObjectName[]	getAllUserDeployedComponents( final String param1 );
	public javax.management.ObjectName[]	getAppclientModule();
	public ObjectName	getAppclientModuleByName( final String key );
	public String[]	getAvailableModules( final String param1 );
	public String[]	getAvailableModules( final String param1, final String[] param2 );
	public String[]	getAvailableUserModules( final String param1, final String[] param2 );
	public String[]	getAvailableVersions( final String param1, final String param2 );
	public javax.management.ObjectName[]	getConnectorModule();
	public ObjectName	getConnectorModuleByName( final String key );
	public String	getDefaultVersion( final String param1, final String param2 );
	public String[]	getDeployedConnectors();
	public String[]	getDeployedEJBModules();
	public String[]	getDeployedJ2EEApplications();
	public String[]	getDeployedWebModules();
	public String	getDeploymentDescriptor( final String param1 );
	public String[]	getDeploymentDescriptorLocations( final String param1, final String param2 );
	public javax.management.ObjectName[]	getEjbModule();
	public ObjectName	getEjbModuleByName( final String key );
	public String[]	getEmbeddedConnectorNames( final String param1, final String param2 );
	//public HostAndPort	getHostAndPort();
	//public HostAndPort	getHostAndPort( final boolean param1 );
	//public HostAndPort	getHostAndPort( final String param1, final boolean param2 );
	//public String	getHostName( final HostAndPort param1 );
	public javax.management.ObjectName[]	getJ2eeApplication();
	public ObjectName	getJ2eeApplicationByName( final String key );
	public String	getLastModified( final String param1, final String param2 );
	public javax.management.ObjectName[]	getLifecycleModule();
	public javax.management.ObjectName[]	getLifecycleModule( final String param1 );
	public ObjectName	getLifecycleModuleByName( final String key );
	public ObjectName	getLifecycleModuleByName( final String param1, final String param2 );
	public int	getMaxApplicationVersions();
	public javax.management.ObjectName[]	getMbean();
	public ObjectName	getMbeanByName( final String key );
	public String[]	getModuleComponents( final String param1 );
	public String[]	getModuleComponents( final String param1, final String param2 );
	public Integer	getModuleType( final String param1 );
	public String[]	getNonRunningModules( final String param1, final String[] param2 );
	public String[]	getNonRunningUserModules( final String param1, final String[] param2 );
	public int	getRepositoryCleanerPollingInterval();
	public String[]	getRunningModules( final String param1, final String[] param2 );
	public String[]	getRunningUserModules( final String param1, final String[] param2 );
	public boolean	getStatus( final String param1, final String param2 );
	public String[]	getTargets();
	//public HostAndPort	getVirtualServerHostAndPort( final String param1, final boolean param2 );
	public javax.management.ObjectName[]	getWebModule();
	public ObjectName	getWebModuleByName( final String key );
	public boolean	isAutoDeployEnabled();
	public boolean	isAutoDeployJspPreCompilationEnabled();
	public Boolean	isLifecycleModuleRegistered( final String param1 );
	public boolean	isMBeanEnabled( final String param1, final String param2 );
	public boolean	isRedeploySupported();
	public boolean	isRepositoryCleanerEnabled();
	public String[]	listApplicationReferencesAsString( final String param1 );
	public List	listMBeanConfigObjectNames( final String param1 );
	public List	listMBeanConfigObjectNames( final String param1, final int param2, final boolean param3 );
	public List	listMBeanNames( final String param1 );
	public javax.management.ObjectName[]	listReferencees( final String param1 );
	public void	removeAppclientModuleByName( final String key );
	public void	removeConnectorModuleByName( final String key );
	public void	removeEjbModuleByName( final String key );
	public void	removeJ2eeApplicationByName( final String key );
	public void	removeLifecycleModuleByName( final String key );
	public void	removeLifecycleModuleByName( final String param1, final String param2 );
	//public DeploymentStatus	removeLifecycleModuleReference( final String param1, final String param2 );
	public void	removeMbeanByName( final String key );
	public void	removeWebModuleByName( final String key );
	public void	setAutoDeployEnabled();
	public void	setAutoDeployJspPreCompilationEnabled();
	public void	setMaxApplicationVersions( final int param1 );
	public void	setRepositoryCleanerPollingInterval( final int param1 );
	//public DeploymentStatus	start( final String param1, final String param2, final Map param3 );
	public Map	startAndReturnStatusAsMap( final String param1, final String param2, final Map param3 );
	//public DeploymentStatus	stop( final String param1, final String param2, final Map param3 );
	public Map	stopAndReturnStatusAsMap( final String param1, final String param2, final Map param3 );
	//public DeploymentStatus	undeploy( final Properties param1 );
	//public DeploymentStatus	undeploy( final Properties param1, final String[] param2 );

}
