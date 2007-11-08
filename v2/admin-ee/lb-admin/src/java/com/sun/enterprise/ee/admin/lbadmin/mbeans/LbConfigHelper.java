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

package com.sun.enterprise.ee.admin.lbadmin.mbeans;

import java.util.ArrayList;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.LbConfigs;
import com.sun.enterprise.config.serverbeans.LbConfig;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.ServerRef;


/**
 * Helper class for lb-configs and lb-config element. This class various 
 * utility functions for LB Admin. For example: Getting list of LBs pointing 
 * to a server instance, getting list of LBs pointing to a particular cluster,
 * etc.
 *
 * @author Satish Viswanatham
 */
public class LbConfigHelper {       

    /**
     * Returns the list of LBs which contain a server instance
     *
     */
     public static String[] getLBsForStandAloneServer(ConfigContext ctx, 
                String serverName)  throws ConfigException{
        ArrayList sList = new ArrayList();
        LbConfigs lbs = ((Domain) ctx.getRootConfigBean()).getLbConfigs();

        if (lbs== null) {
            // return, no lb configs
            return null;
        }
        LbConfig[] lbConfigs = lbs.getLbConfig();
        for (int i =0; i < lbConfigs.length; i ++) {
            ServerRef[] sRefs = lbConfigs[i].getServerRef();
            for (int j =0; j < sRefs.length; j++) {
               if (sRefs[j].getRef().equals(serverName) ) {
                    sList.add(lbConfigs[i].getName());     
               }
            }
        }
        String[] strList = new String[sList.size()];
        return ( String [] ) sList.toArray(strList);
        
     }

    /**
     * Returns the list of LBs which contain the specified cluster
     *
     */
     public static String[] getLBsForCluster(ConfigContext ctx, 
                String clusterName)  throws ConfigException{
        ArrayList sList = new ArrayList();
        LbConfigs lbs = ((Domain) ctx.getRootConfigBean()).getLbConfigs();

        if (lbs== null) {
            // return, no lb configs
            return null;
        }
        LbConfig[] lbConfigs = lbs.getLbConfig();
        for (int i =0; i < lbConfigs.length; i ++) {
            ClusterRef[] cRefs = lbConfigs[i].getClusterRef();
            for (int j =0; j < cRefs.length; j++) {
               if (cRefs[j].getRef().equals(clusterName) ) {
                    sList.add(lbConfigs[i].getName());     
               }
            }
        }
        String[] strList = new String[sList.size()];
        return ( String [] ) sList.toArray(strList);
        
     }

    /**
     * Returns the list of clusters in the specified LB
     *
     */
     public static String[] getClustersInLB(ConfigContext ctx, 
                String configName)  throws ConfigException{
        ArrayList sList = new ArrayList();
        LbConfigs lbs = ((Domain) ctx.getRootConfigBean()).getLbConfigs();

        if (lbs== null) {
            // return, no lb configs
            return null;
        }
        LbConfig lbConfig = lbs.getLbConfigByName(configName);
        if (lbConfig == null) {
            return null;
        }
        ClusterRef[] cRefs = lbConfig.getClusterRef();
        for (int j =0; j < cRefs.length; j++) {
                sList.add(cRefs[j].getRef());     
        }
        String[] strList = new String[sList.size()];
        return ( String [] ) sList.toArray(strList);
        
     }

    /**
     * Returns the list of servers in the specified LB
     *
     */
     public static String[] getServersInLB(ConfigContext ctx, 
                String configName)  throws ConfigException{
        ArrayList sList = new ArrayList();
        LbConfigs lbs = ((Domain) ctx.getRootConfigBean()).getLbConfigs();

        if (lbs== null) {
            // return, no lb configs
            return null;
        }
        LbConfig lbConfig = lbs.getLbConfigByName(configName);
        if (lbConfig == null) {
            return null;
        }
        ServerRef[] sRefs = lbConfig.getServerRef();
        for (int j =0; j < sRefs.length; j++) {
                sList.add(sRefs[j].getRef());     
        }
        String[] strList = new String[sList.size()];
        return ( String [] ) sList.toArray(strList);
        
     }

    /**
     * Disables load balancing for a server with a quiescing period specififed 
     * by the timeout .
     *
     * @param target target server whose load balancing has to be disabled
     * @param timeout quiescing time
     */
    public void disableServer(String target, int timeout) {
        //TODO
    }

    /**
     * Enables a server for load balancing.
     *
     * @param target target server whose load balancing has to be enabled
     * @param timeout quiescing time
     */
    public void enableServer(String target) {
        //TODO
    }
    
    /**
     * Disables load balancing for a particular application in a server instance 
     * with a quiescing period specififed by the timeout .
     *
     * @param target target server where the application has been deployed
     * @param appName application name.
     * @param timeout quiescing time
     */
    public void disableApplication(String target, String appName, int timeout) {
        //TODO
    }

    /**
     * Enables load balancing for a particular application in a server instance 
     *
     * @param target target server where the application has been deployed
     * @param appName application name.
     */
    public void enableApplication(String target, String appName) {
        //TODO
    }

}
