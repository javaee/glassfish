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
	Configuration for the &lt;log-service&gt; element.
	 
	@see ModuleLogLevelsConfigKeys
 */
public interface LogServiceConfig
	extends PropertiesAccess, ConfigElement, Singleton
{
    public static final String AMX_TYPE = "log-service";
    
    @ResolveTo(Boolean.class)
	public String	getAlarms();
	public void	setAlarms( String value );

	public String	getFile();
	public void	setFile( String value );

	public String	getLogFilter();
	public void	setLogFilter( String value );

	public String	getLogHandler();
	public void	setLogHandler( String value );

    /**
        @since AppServer 9.0
     */
    @ResolveTo(Integer.class)
	public String	getLogRotationTimeLimitInMinutes();
	public void	setLogRotationTimeLimitInMinutes( String value );
	
    @ResolveTo(Integer.class)
	public String	getLogRotationLimitInBytes();
	public void	setLogRotationLimitInBytes( String value );

    @ResolveTo(Boolean.class)
	public String	getLogToConsole();
	public void	setLogToConsole( String value );

    @ResolveTo(Boolean.class)
	public String	getUseSystemLogging();
	public void	setUseSystemLogging( String value );
	
    @ResolveTo(Integer.class)
	public String  getRetainErrorStatisticsForHours();
	public void    setRetainErrorStatisticsForHours( String hours );
	
// -------------------- Operations --------------------
// 	/**
// 		Creates new module-log-levels element.
// 
// 		@param params Map of optional attributes whose keys are defined in
// 		ModuleLogLevelsParams class.
// 		@return A proxy to the ModuleLogLevelsConfig MBean.
// 		@see ModuleLogLevelsConfigKeys
// 	 */
// 	public ModuleLogLevelsConfig	createModuleLogLevelsConfig( Map<String,String> params );
// 
// 	/**
// 		Removes module-log-levels element.
// 	 */
// 	public void			removeModuleLogLevels();

	/**
		Get the ModuleLogLevelsConfig MBean.
	 */
	public ModuleLogLevelsConfig getModuleLogLevels();
}
