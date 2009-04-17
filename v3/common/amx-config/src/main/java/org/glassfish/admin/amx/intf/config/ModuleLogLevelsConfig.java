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
package org.glassfish.admin.amx.intf.config;

import org.glassfish.admin.amx.base.Singleton;


import java.util.Map;



/**
	 Configuration for the &lt;module-log-levels&gt; element.
	 
	 Values are those defined in {@link LogLevelValues}.
*/

public interface ModuleLogLevelsConfig extends PropertiesAccess, ConfigElement, Singleton
{
    public static final String AMX_TYPE = "module-log-levels";
	/**

CANNOT support this generically
		Get a Map keyed by Attribute name of all log levels.
		Attribute names correspond to the various get methods eg
		"Admin" for {@link #getAdmin}, "CMP" for {@link #getCMP}, etc.
		<p>
		The possible levels are as defined in {@link LogLevelValues}.
	public Map<String,String>		getAllLevels();
	 */
	
	
	/**
CANNOT support this generically

		Change all log levels to the specified value.
		
		@param value one of the values in {@link LogLevelValues}
	public void		changeAll( final String value );
	 */
	
	/**
	    @since AppServer 9.0
	 */
	public String getNodeAgent();
	/**
	    @since AppServer 9.0
	 */
	public void setNodeAgent( String level );
	
	/**
	    @since AppServer 9.0
	 */
	public String getUtil();
	/**
	    @since AppServer 9.0
	 */
	public void setUtil( String level );
	
	/**
	    @since AppServer 9.0
	 */
	public String getSelfManagement();
	/**
	    @since AppServer 9.0
	 */
	public void setSelfManagement( String level );
	
	/**
	    @since AppServer 9.0
	 */
	public String getSynchronization();
	/**
	    @since AppServer 9.0
	 */
	public void setSynchronization( String level );
	
	/**
	    @since AppServer 9.0
	 */
	public String getGroupManagementService();
	/**
	    @since AppServer 9.0
	 */
	public void setGroupManagementService( String level );
	
	
	/**
	    @since AppServer 9.0
	 */
	public String getManagementEvent();
	/**
	    @since AppServer 9.0
	 */
	public void setManagementEvent( String level );
	
	
	public String	getAdmin();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setAdmin( String value );

	public String	getClassloader();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setClassloader( String value );

	public String	getCMPContainer();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setCMPContainer( String value );

	public String	getCMP();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setCMP( String value );

	public String	getConfiguration();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setConfiguration( String value );

	public String	getConnector();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setConnector( String value );

	public String	getCORBA();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setCORBA( String value );

	public String	getDeployment();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setDeployment( String value );

	public String	getEJBContainer();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setEJBContainer( String value );

	public String	getJavamail();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setJavamail( String value );

	public String	getJAXR();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setJAXR( String value );

	public String	getJAXRPC();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setJAXRPC( String value );

	public String	getJDO();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setJDO( String value );

	public String	getJMS();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setJMS( String value );

	public String	getJTA();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setJTA( String value );

	public String	getJTS();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setJTS( String value );

	public String	getMDBContainer();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setMDBContainer( String value );

	public String	getNaming();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setNaming( String value );

	public String	getResourceAdapter();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setResourceAdapter( String value );

	public String	getRoot();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setRoot( String value );

	public String	getSAAJ();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setSAAJ( String value );

	public String	getSecurity();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setSecurity( String value );

	public String	getServer();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setServer( String value );

	public String	getVerifier();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setVerifier( String value );

	public String	getWebContainer();
	/** @param value any value defined in {@link LogLevelValues} */
	public void	setWebContainer( String value );





}
