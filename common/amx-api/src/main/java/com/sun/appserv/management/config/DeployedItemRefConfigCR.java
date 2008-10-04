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
package com.sun.appserv.management.config;

import java.util.Map;


/**
	Represents the capability to create or remove ("CR") an instance of
	the &lt;application-ref&gt; element.
*/
public interface DeployedItemRefConfigCR extends ConfigRemover
{
	/** Key for use with createDeployedItemRefConfig() */
	public final static String	REF_KEY				= "Ref";
	/** Key for use with createDeployedItemRefConfig() */
	public final static String	ENABLED_KEY			= "Enabled";
	/** Key for use with createDeployedItemRefConfig() */
	public final static String	VIRTUAL_SERVERS_KEY		= "VirtualServers";
	
	public final static String	LB_ENABLED_KEY                  = "LBEnabled";

	public final static String	DISABLE_TIMEOUT_IN_MINUTES_KEY	= "DisableTimeoutInMinutes";


	/**
		Creates a new application-ref element.

		@param referencedApplicationName Application name.
		@param optional Map of optional aattributes whose keys are defined here.
		@return A proxy to the DeployedItemRefConfig MBean that manages the 
		config of newly created application-ref.
	 */
	public DeployedItemRefConfig createDeployedItemRefConfig(
		String referencedApplicationName, Map<String,String> optional);
        
	/**
		Creates a new application-ref element.

		@param enabled
		@param referencedApplicationName Application name.
		@param virtualServers	Comma separated list of virtual servers.
		@param lbEnabled
		@param disableTimeoutInMinutes
		@return A proxy to the DeployedItemRefConfig MBean that manages the 
		config of newly created application-ref.
	 */
	public DeployedItemRefConfig createDeployedItemRefConfig(String enabled, 
		String referencedApplicationName, String virtualServers,
		String lbEnabled, String disableTimeoutInMinutes);
        
	/**
		Creates a new application-ref element.

		@param referencedApplicationName Application name.
		@return A proxy to the DeployedItemRefConfig MBean that manages the 
		config of newly created application-ref.
	 */
	public DeployedItemRefConfig createDeployedItemRefConfig(String referencedApplicationName);
	
	/**
	 * Remove an existing &lt;application-ref&gt;
	 *
	 * @param referencedApplicationName the name of the application to remove
	 */
	public  void removeDeployedItemRefConfig( String referencedApplicationName );
}
