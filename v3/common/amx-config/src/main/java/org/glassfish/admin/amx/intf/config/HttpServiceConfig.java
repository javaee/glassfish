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


import org.glassfish.admin.amx.config.DefaultValues;
import org.glassfish.admin.amx.base.Singleton;


import java.util.Map;


/**
	 Configuration for the &lt;http-service&gt; element.
 */
public interface HttpServiceConfig
	extends ConfigElement, PropertiesAccess, DefaultValues, Singleton
{
    public static final String AMX_TYPE = "http-service";
	/**
		@return Map of all HttpListenerConfig proxies, keyed by the name of the listener.
		@see org.glassfish.admin.amx.base.Container#getContaineeMap
	 */
	public Map<String,HttpListenerConfig>		getHttpListener();
	
	/**
		@return Map of all VirtualServerConfig proxies, keyed by the name of the virtual server.
		@see org.glassfish.admin.amx.base.Container#getContaineeMap
	 */
	public Map<String,VirtualServerConfig>		getVirtualServer();
	
	
// 
// 	/**
// 		Creates access-log element with the given params.
// 		@param params
// 		@return Proxy to the AccessLogConfig MBean
// 		@see AccessLogConfigKeys
// 	 */
// 	public AccessLogConfig	createAccessLogConfig( Map<String,String> params );
// 
// 	/**
// 		Removes access-log element.
// 	 */
// 	public void			removeAccessLog();
// 
// 	/**
// 		Creates request-processing element with the given params.
// 		@param params
// 		@return Proxy to the RequestProcessingConfig MBean
// 		@see RequestProcessingConfigKeys
// 	 */
// 	public RequestProcessingConfig	createRequestProcessingConfig( Map<String,String> params );
// 
// 	/**
// 		Removes request-processing element.
// 	 */
// 	public void			removeRequestProcessing();
// 
// 	/**
// 		@return Proxy to the KeepAliveConfig MBean
// 	 */
// 	public KeepAliveConfig	getKeepAlive();
// 
// 	/**
// 		Creates keep-alive element with the given params.
// 		@param params
// 		@return Proxy to the KeepAliveConfig MBean
// 		@see KeepAliveConfigKeys
// 	 */
// 	public KeepAliveConfig	createKeepAliveConfig( Map<String,String> params );
// 
// 	/**
// 		Removes keep-alive element.
// 	 */
// 	public void			removeKeepAlive();
// 
// 	/**
// 		Creates connection-pool element with the given params.
// 		@param params
// 		@return the ObjectName of the ConnectionPoolConfig
// 		@see ConnectionPoolConfigKeys
// 	 */
// 	public ConnectionPoolConfig	createConnectionPoolConfig( Map<String,String> params );
// 
// 	/**
// 		Removes connection-pool element.
// 	 */
// 	public void			removeConnectionPool();
// 
// 	/**
// 		@return Proxy to the HTTPProtocolConfig MBean
// 	 */
// 	public HTTPProtocolConfig	getHTTPProtocol();
// 
// 	/**
// 		Creates http-protocol element with the given params.
// 		@param params
// 		@return Proxy to the HTTPProtocolConfig MBean
// 		@see HTTPProtocolConfigKeys
// 	 */
// 	public HTTPProtocolConfig	createHTTPProtocolConfig( Map<String,String> params );
// 
// 	/**
// 		Removes http-protocol element.
// 	 */
// 	public void			removeHTTPProtocol();
// 
// 	/**
// 		@return Proxy to the HTTPFileCacheConfig MBean
// 	 */
// 	public HTTPFileCacheConfig	getHTTPFileCache();
// 
// 	/**
// 		Creates http-file-cache element with the given params.
// 		@param params
// 		@return Proxy to the HTTPFileCacheConfig MBean
// 		@see HTTPFileCacheConfigKeys
// 	 */
// 	public HTTPFileCacheConfig	createHTTPFileCacheConfig( Map<String,String> params );
// 
// 	/**
// 		Removes http-file-cache element.
// 	 */
// 	public void			removeHTTPFileCache();
// 
// 
// 
// 	/**
// 		Create an <http-listener>
// 		Parameters:
// 		<ul>
// 		<li>{@link HTTPListenerConfigKeys#ADDRESS_KEY}</li>
// 		<li>{@link HTTPListenerConfigKeys#PORT_KEY}</li>
// 		<li>{@link HTTPListenerConfigKeys#ENABLED_KEY}</li>
// 		<li>{@link HTTPListenerConfigKeys#SECURITY_ENABLED_KEY}</li>
// 		<li>{@link HTTPListenerConfigKeys#DEFAULT_VIRTUAL_SERVER_KEY}</li>
// 		<li>{@link HTTPListenerConfigKeys#ACCEPTOR_THREADS_KEY}</li>
// 		<li>{@link HTTPListenerConfigKeys#XPOWERED_BY_KEY}</li>
// 		<li>{@link HTTPListenerConfigKeys#XPOWERED_BY_KEY}</li>
// 		<li>{@link HTTPListenerConfigKeys#IGNORE_MISSING_REFERENCES_KEY} (for defaultVirtualServer)</li>
// 		</ul>
// 	 
// 		
// 		@param name 		the name (id) of the newly created listener
// 		@param address		IP address of the listener
// 		@param port			port of the listener
// 		@param defaultVirtualServer		        The name of the default 
// 		virtual server for this particular connection group.
// 		@param serverName	
// 		@param optional		optional parameters keyed by one of XXX_KEY
// 		@return A proxy to the HttpListenerConfig MBean
// 		@see HTTPListenerConfigKeys
// 	 */
// 	public HttpListenerConfig	createHTTPListenerConfig(
// 							String	name,
// 							String	address,
// 							int		port,
// 							String	defaultVirtualServer,
// 							String	serverName,
// 							Map<String,String>		optional );
// 	
// 	/**
// 		Remove the &lt;http-listener>.  Will fail if the &lt;http-listener> is referred to by
// 		other elements.
// 		@param name		the name (id) of the http listener to be removed.
// 	 */
// 	public void			removeHTTPListenerConfig( String name );
// 	
// 	/**
// 		Create a <virtual-server>.
// 		Most fields will default to reasonable values. Legal keys include:
// 		<ul>
// 			{@link VirtualServerConfigKeys#HTTP_LISTENERS_KEY}</li>
// 			{@link VirtualServerConfigKeys#STATE_KEY}</li>
// 			{@link VirtualServerConfigKeys#DOC_ROOT_PROPERTY_KEY}</li> 
// 			{@link VirtualServerConfigKeys#ACCESS_LOG_PROPERTY_KEY}</li> 
// 		</ul>
// 		
// 		@param name 			the name (id) of the newly created virtual server
// 		@param hosts			comma-separated list of hosts
// 		@param optional			optional params
// 		@return		A proxy to the VirtualServerConfig MBean.
// 		@see VirtualServerConfigKeys
// 	 */
// 	public VirtualServerConfig	createVirtualServerConfig(
// 							String	name,
// 							String	hosts,
// 							Map<String,String>	optional );
// 	
// 	/**
// 		Remove the &lt;virtual-server&gt;.
// 		Will fail if the &lt;virtual-server&gt is referred to by
// 		other elements.
// 		
// 		@param virtualServerName
// 	 */
// 	public void			removeVirtualServerConfig( String virtualServerName );
	
	

	/**
		Get the AccessLogConfig.
	 */
	public AccessLogConfig	getAccessLog();

	/**
		Get the RequestProcessingConfig.
	 */
	public RequestProcessingConfig	getRequestProcessing();

	/**
		Get the ConnectionPoolConfig.
	 */
	public ConnectionPoolConfig	getConnectionPool();

}
