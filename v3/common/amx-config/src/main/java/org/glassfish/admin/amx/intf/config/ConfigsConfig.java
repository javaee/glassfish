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
import org.glassfish.admin.amx.annotation.ManagedOperation;


/**
    Configuration for the &lt;configs&gt; element; it is an internal "node" which
    groups all resources under itself.
    @since Glassfish V3
*/
public interface ConfigsConfig
	extends ConfigElement, ConfigCollectionElement, Singleton
{
    public static final String AMX_TYPE = "configs";
	/**
		Calls Container.getContaineeMap( XTypes.CONFIG_CONFIG ).
		@return Map of items, keyed by name.
		@see org.glassfish.admin.amx.base.Container#getContaineeMap
	 */
	public Map<String,ConfigConfig>		getConfig();
	
// 	/**
// 		Create a new &lt;config&gt; element. Legal keys in the optional Map include:
// 		<ul>
// 		<li>{@link ConfigConfigKeys#DYNAMIC_RECONFIGURATION_ENABLED_KEY}</li>
// 		<li>{@link ConfigConfigKeys#SRC_CONFIG_NAME_KEY}</li>
// 		</ul>
// 		<p>
// 		A new config is created by copying an existing one.  Unless a specific
// 		config is specified via {@link ConfigConfigKeys#SRC_CONFIG_NAME_KEY},
// 		the default config as given by {@link ConfigConfigKeys#DEFAULT_SRC_CONFIG_NAME}
// 		will be copied.
// 		<p>
// 		Depending on the context in which the config is to be used, it may or may 
// 		not conflict with values found in other configs.
// 
// 		@param name		name of the &lt;config>
// 		@param optional		optional attributes for config creation
// 		@return Returns a proxy to the ConfigConfig MBean.
// 		@see ConfigConfigKeys
// 	*/
//     @ManagedOperation
// 	public ConfigConfig	createConfigConfig( String name, Map<String,String> optional );
// 
// 	/**
// 		Remove a config.  This will fail if any <server> or <cluster>
// 		refers to it.
// 
// 		@param name The config name.
// 	*/
//     @ManagedOperation
// 	public void		removeConfigConfig( String name );
// 
}







