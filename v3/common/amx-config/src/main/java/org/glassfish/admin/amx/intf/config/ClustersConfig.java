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




import java.util.Map;


/**
    Configuration for the &lt;clusters&gt; element; it is an internal "node" which
    groups all resources under itself.
    @since Glassfish V3
*/
public interface ClustersConfig
	extends ConfigElement, ConfigCollectionElement
{
    public static final String AMX_TYPE = "clusters";
    
	/**
		Calls Container.getContaineeMap( XTypes.CLUSTER_CONFIG ).
		@return Map of items, keyed by name.
		@see org.glassfish.admin.amx.base.Container#getContaineeMap
	 */
	public Map<String,ClusterConfig>	getCluster();
	
//         /**
// 		Create a new ClusterConfig.  The 'referencedConfigName' must be non-null
// 		and must not be "default-config" or "server-config".  If it is desired
// 		to create a new ClusterConfig which uses a copy of default-config,
// 		use the createClusterConfig( name, reserved ) form.
// 
// 		@param name the name of the cluster to create.
// 		@param referencedConfigName the non-null name of the config to reference.
// 		@param optional	optional values, properties only
// 
// 		@return a ClusterConfig
// 	 */
// 	public ClusterConfig createClusterConfig(String name, String referencedConfigName,
//                 Map<String,String> optional);
//                 
// 	/**
//             Create a new ClusterConfig which refers to a copy of the default-config.
// 
//             @param name the name of the cluster to create.
//             @param optional	optional values, properties only
// 
//             @return a ClusterConfig
// 	 */ 
// 	public ClusterConfig createClusterConfig(String name, Map<String,String> optional);
// 
// 	/**
//           Remove an existing &lt;cluster&gt;.
//           @param name the name of the cluster to remove.
// 	 */
// 	public void removeClusterConfig(String name);

}







