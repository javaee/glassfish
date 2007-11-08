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
 * 
 */

package com.sun.enterprise.admin.mbeans;

import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.util.i18n.StringManager;
import java.util.List;

import javax.management.Attribute;

import com.sun.enterprise.config.serverbeans.ServerTags;
import javax.management.MBeanException;
import javax.management.MBeanServer;


/**
 * Static utility class for common functions in config mbeans. Place all common
 * utility type functions into this class for access across all config mbeans.
 *
 * @author  Rob Ruyak
 */
public class ConfigMBeanUtil {
    
    /**
     * i18n strings manager object
     */
    private static final StringManager localStrings = 
        StringManager.getManager(ConfigsMBean.class);
    
    
    //disallow the instantiation of this object
    private ConfigMBeanUtil() {
    }
    
    /**
     * Given a list and an Attribute object, returns whether or not the 
     * attribute with the specified name exists.
     *
     * @param list the list of Attribute objects
     * @param name the name of the attribute to find
     * @return true/false
     */
    public static boolean attributeDefinedInList(final List list, 
            final String name) {
        if(name != null) {            
            for(java.util.Iterator itr = list.iterator(); itr.hasNext(); ) {
                Attribute attr = (Attribute) itr.next();
                if(name != null && name.equals(attr.getName())) {
                    return true; 
                } 
            }
        }
        return false;
    }
    
    /**
     *
     *
     */
    public static MBeanServer getMBeanServer() throws MBeanException {
        return com.sun.enterprise.admin.common.MBeanServerFactory.getMBeanServer();
    }
    
    
    /**
     *
     *
     */
    public static Target getTarget(String targetName, TargetType[] targetTypes,
            ConfigContext configContext) throws MBeanException {        
                
        try {               
            Target target = TargetBuilder.INSTANCE.createTarget(
                    targetTypes, targetName, configContext);
            if (target.getType() == TargetType.SERVER || 
                target.getType() == TargetType.DAS) {
                //If we are operating on a server, ensure that the server is the only entity 
                //referencing its config
                String configName = ServerHelper.getConfigForServer(configContext,
                    target.getName()).getName();
                if (!ConfigAPIHelper.isConfigurationReferencedByServerOnly(configContext, 
                    configName, target.getName())) {
                        throw new ConfigException(localStrings.getString(
                            "configurationHasMultipleRefs", target.getName(), configName, 
                            ConfigAPIHelper.getConfigurationReferenceesAsString(
                                configContext, configName)));   
                }                                              
            } else if (target.getType() == TargetType.CLUSTER) {
                //If we are operating on a cluster, ensure that the cluster is the only entity 
                //referencing its config
                String configName = ClusterHelper.getConfigForCluster(configContext, 
                    target.getName()).getName();
                if (!ConfigAPIHelper.isConfigurationReferencedByClusterOnly(configContext, 
                    configName, target.getName())) {
                        throw new ConfigException(localStrings.getString(
                            "configurationHasMultipleRefs", target.getName(), configName, 
                            ConfigAPIHelper.getConfigurationReferenceesAsString(
                                configContext, configName))); 
                }     
            }
            return target;
        }
        catch (Exception e) {
            throw MBeanExceptionFormatter.toMBeanException(e, null);
        }        
    }
}
