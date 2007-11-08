
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
package com.sun.enterprise.ee.admin.mbeanapi;

import java.util.*;
import javax.management.MBeanException;
import javax.management.Attribute;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException;

/**
 * Interface HadbConfigMBean
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface HadbConfigMBean {

	public Object[] createHACluster(Properties props) throws HADBSetupException;

	public Object[] createHACluster(
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String deviceSize,	
		Boolean autohadb,
		String portbase, 
		String clusterName, 
		Properties props) throws HADBSetupException;
	
	public Object[] deleteHACluster(		
		String clusterName) throws HADBSetupException;
	
	public Object[] deleteHACluster(		
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String clusterName) throws HADBSetupException;
	
    public Object[] createHASchema(			
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String storeuser, 
		String storepassword, 
		String dbsystempassword, 
		String databaseName) throws HADBSetupException; 
	
	public Object[] clearHASchema(			
		String databaseName) throws HADBSetupException; 
	
	public Object[] clearHASchema(			
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String storeuser, 
		String storepassword, 
		String dbsystempassword, 
		String databaseName) throws HADBSetupException; 
	
	public Object[] configureHAPersistence(	
		String type,		
		String frequency,	
		String scope,		
		String store,		
		Properties props,	
		String clusterName	) throws HADBSetupException; 
	
	public Object[] stopDB(		
		String clusterName) throws HADBSetupException;

	public Object[] stopDB(		
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String clusterName) throws HADBSetupException;

	public Object[] startDB(		
		String clusterName) throws HADBSetupException;

	public Object[] startDB(		
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String clusterName) throws HADBSetupException;

	public Object[] restartDB(		
		String clusterName) throws HADBSetupException;

	public Object[] restartDB(		
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String clusterName) throws HADBSetupException;

	public Object[] isHadbInstalled();
	
	public Object[] pingHadbAgent(
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String clusterName) throws HADBSetupException;

	public Boolean getAutoHadb(
		String clusterName) throws HADBSetupException;

	public Object[] getHADBInfo(
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String clusterName) throws HADBSetupException;

	public Properties getHADBDatabaseAttributes(
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String clusterName) throws HADBSetupException;

	public Properties getHADBDatabaseAttributes(
		String clusterName) throws HADBSetupException;

	public Properties getHADBReadOnlyDatabaseAttributes(
		String clusterName) throws HADBSetupException;

	public Properties getHADBReadWriteDatabaseAttributes(
		String clusterName) throws HADBSetupException;

	public String getHADBRuntimeInfo(
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String clusterName) throws HADBSetupException;

	public Properties setHADBDatabaseAttributes(
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String clusterName, 
		String props) throws HADBSetupException;

	public Properties setHADBDatabaseAttributes(
		String clusterName, 
		Properties props) throws HADBSetupException;
	
	public Properties setHADBDatabaseAttributes(
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String clusterName, 
		Properties props) throws HADBSetupException;
	
	public String getAgentPort(
		String clusterName) throws HADBSetupException;
	
	public Object[] isHA(String clusterName);

	public String[] getNodeList(
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String clusterName) throws HADBSetupException;

	public String[] getNodeList(
		String clusterName) throws HADBSetupException;

	public String getDatabaseStatus(
		String hosts,		
		String agentPort,	
		String haAdminPassword,	
		String haAdminPasswordFile,	
		String clusterName) throws HADBSetupException;

	public boolean isHadbmError(String s, int errno);
	public boolean isAuthError(String s);
	
	public Object[] backdoor(String s1, String s2, String s3, String s4, String s5);
}
