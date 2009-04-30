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
    Configuration for the &lt;servers&gt; element; it is an internal "node" which
    groups all resources under itself.
    @since Glassfish V3
*/
public interface ServersConfig
	extends ConfigElement, ConfigCollectionElement, Singleton
{
    public static final String AMX_TYPE = "servers";
    
//     /**
//         Create a new &lt;server&gt; given an existing config and node-agent.
//         These are required parameters for the server instance to be created.
// 
//         @param name the name of the server to create
//         @param nodeAgentName the node agent that the server will reference
//         @param configName the config that the server will reference
//         @param optional properties for this new server
//           This is a Map object consisting of key/value for a given property, that can be applied to
//           this server instance. The Map may also contain additional properties that can be applied to this server instance.
//           <p>Note that Properties that relate to ports of listeners are stored as system-properties and have
//           specific key names and must be specified to override values defined in the config to any of the relevant
//           ports - this is particularly required when the instance being created is on the same machine as other
//           instances in the domain.
//           <p>Legal property keys are those found in {@link ServerConfigKeys}.
// 
//         @return A proxy to the StandaloneServerConfig MBean that manages the newly created server
//      */
//     public StandaloneServerConfig createStandaloneServerConfig(String name, String nodeAgentName,
//             String configName, Map<String,String> optional);
//             
//     /**
// 	 * Creates a new &lt;server&gt; that belongs to a cluster.
// 
//      @param name			Name of the server.
//      @param nodeAgentName	Name of the node agent that should manage this instance
//      @param clusterName	    Name of the cluster to which this server should belong.
//         <p>Note that it is prefereable to pass in an existing nodeagent's name. A non-existent nodeagent name can be
//         passed in but this nodeagent's hostname attribute will be marked as "localhost" as an assumption is made that the
//         nodeagent is local. The nodeagent should be created through the create-node-agent command
//         using the Command Line Interface(CLI) on the machine where this instance is intended to reside after this create()
//         operation. Prior to starting this instance, that nodeagent will have to be started using the CLI command
//         start-node-agent.
//      @param optional Attributes and properties for this new server.
//        <p>Note that Properties that relate to ports of listeners are stored as system-properties and have
//        specific key names and must be specified to override values defined in the config to any of the relevant
//        ports - this is particularly required when the instance being created is on the same machine as other
//        instances in the domain.
//        <p>Legal keys are those defined in {@link ServerConfigKeys}.
// 
//      @return	A proxy to the ClusteredServerConfig MBean.
// 	 */
// 	public ClusteredServerConfig createClusteredServerConfig(String name, 
//             String clusterName, String nodeAgentName,
//             java.util.Map<String,String> optional);


    /**
		Calls Container.getContaineeMap( XTypes.STANDALONE_SERVER_CONFIG ).
		@return Map of items, keyed by name.
		@see org.glassfish.admin.amx.base.Container#getContaineeMap
	 */
	public Map<String,StandaloneServerConfig>		getStandaloneServer();
	
	/**
		Calls Container.getContaineeMap( XTypes.CLUSTERED_SERVER_CONFIG ).
		@return Map of items, keyed by name.
		@see org.glassfish.admin.amx.base.Container#getContaineeMap
	 */
	public Map<String,ClusteredServerConfig>		getClusteredServer();
	
// 	public void		removeStandaloneServerConfig( String name );
//     
// 	public void		removeClusteredServerConfig( String name );


}







