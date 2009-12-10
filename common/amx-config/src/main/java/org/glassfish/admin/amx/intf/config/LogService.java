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
public interface LogService
	extends ConfigElement, Singleton
{
	public String	getAlarms();
	public void	setAlarms( String value );

	public String	getFile();
	public void	setFile( String value );

	public String	getLogFilter();
	public void	setLogFilter( String value );

	public String	getLogHandler();
	public void	setLogHandler( String value );

    /** Note bad spelling to match bug in config 'Timelimit'.
        See https://glassfish.dev.java.net/issues/show_bug.cgi?id=10114
    */
	public String	getLogRotationTimelimitInMinutes();
	public void	setLogRotationTimelimitInMinutes( String value );
    
	public String	getLogRotationLimitInBytes();
	public void	setLogRotationLimitInBytes( String value );

    
	public String	getLogToConsole();
	public void	setLogToConsole( String value );

    
	public String	getUseSystemLogging();
	public void	setUseSystemLogging( String value );
	
    
	public String  getRetainErrorStatisticsForHours();
	public void    setRetainErrorStatisticsForHours( String hours );
	
	/**
		Get the ModuleLogLevels MBean.
	 */
	public ModuleLogLevels getModuleLogLevels();
}
