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
 * $Id: TargetBuilder.java,v 1.5 2007/03/30 22:56:59 llc Exp $
 */

package com.sun.enterprise.admin.target;

//config imports
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.util.IAdminConstants;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.ServerTags;

import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;
import com.sun.enterprise.server.ApplicationServer;

public class TargetBuilder
{
    public static final TargetBuilder INSTANCE;
    
    static {
        PluggableFeatureFactory featureFactory =
            ApplicationServer.getServerContext().getPluggableFeatureFactory();
        TargetFactory tFactory = featureFactory.getTargetFactory();
        INSTANCE = tFactory.getTargetBuilder();
    }

    /**
     * i18n strings manager object
     */
    private static final StringManager strMgr = 
        StringManager.getManager(TargetBuilder.class);

    public TargetBuilder()
    {
    }

    public Target createTarget(String name, ConfigContext domainContext)
        throws ConfigException
    {
        return createTarget(null, getDefaultValidTargets(), name, domainContext);
    }
   
    protected TargetType[] getDefaultValidTargets()
    {
        return new TargetType[] {TargetType.DAS};
    }
    
    public Target createTarget(TargetType validTypes[], String name, ConfigContext domainContext)
        throws ConfigException
    {
        return createTarget(null, validTypes, name, domainContext);
    }
        
    public Target createTarget(String defaultTargetName, String name, ConfigContext domainContext)
        throws ConfigException
    {
        return createTarget(defaultTargetName, getDefaultValidTargets(), name, domainContext);
    }
    
    public Target createTarget(String defaultTargetName, TargetType validTypes[], 
        String name, ConfigContext domainContext) throws ConfigException
    {
        checkArg(domainContext, strMgr.getString("target.config_context"));        
        if (name == null || "".equals(name) || "null".equals(name)) { 
            if (defaultTargetName == null || "".equals(defaultTargetName)) {                
                name = getDefaultTarget(domainContext); 
            } else {
                name = defaultTargetName;
            }
        }
        TargetType type = getTargetType(name, domainContext);
        validateTargetType(validTypes, type, name, domainContext);
        
        Target target = null;
        if (type == TargetType.DAS) {
            target = new DASTarget(name, domainContext);
        } else if (type == TargetType.SERVER) {
            target = new ServerTarget(name, domainContext);
        } else if (type == TargetType.DOMAIN) {
            target = new DomainTarget(name, domainContext);
        } else if (type == TargetType.CONFIG) {
            target = new ConfigTarget(name, domainContext);
        } else if (type == TargetType.CLUSTER) {
            target = new ClusterTarget(name, domainContext);
        } else if (type == TargetType.NODE_AGENT) {
            target = new NodeAgentTarget(name, domainContext);
        } else {
            //Should throw more specific exception. This really should never happen
            throw new ConfigException(
                strMgr.getString("target.cant_determine_type", name));
        }
        return target;
    }
    
    /**
     * Validate that the given target is one of the specified types.
     */
    private void validateTargetType(TargetType[] validTypes, TargetType targetType, String targetName,
        ConfigContext domainContext) throws ConfigException
    {
        if (validTypes != null) {
            boolean isValid = false;
            String validTargets = new String();
            for (int i = 0; i < validTypes.length; i++) {
                if (targetType == validTypes[i]) {                    
                    isValid = true;
                } else if (targetType == TargetType.SERVER) {
                    if (validTypes[i] == TargetType.STANDALONE_SERVER) {
                        if (ServerHelper.isServerStandAlone(domainContext, targetName)) {
                            isValid = true;
                        }
                    } else if (validTypes[i] == TargetType.UNCLUSTERED_SERVER) {
                        if (!ServerHelper.isServerClustered(domainContext, targetName)) {
                            isValid = true;
                        }
                    } 
                } else if (targetType == TargetType.CLUSTER && validTypes[i] == TargetType.STANDALONE_CLUSTER) {
                    if (ClusterHelper.isClusterStandAlone(domainContext, targetName)) {
                        isValid = true;
                    }
                }
                validTargets += validTypes[i].getName();
                if (i < validTypes.length - 1) {
                    validTargets += ", ";
                }
            }
            if (!isValid) {
                throw new ConfigException(strMgr.getString("target.invalid_type",
                    targetName, targetType.getName(), validTargets));
            }
        }
    }

    private TargetType getTargetType(String targetName, ConfigContext domainContext)
        throws ConfigException
    {
        TargetType targetType;
        if (targetName.equals(IAdminConstants.DOMAIN_TARGET)) {
            targetType = TargetType.DOMAIN;
        } else if (ConfigAPIHelper.isAConfig(domainContext, targetName)) {
            targetType = TargetType.CONFIG;                             
        } else if (ServerHelper.isAServer(domainContext, targetName)) {
            if (ServerHelper.isDAS(domainContext, targetName)) {
                targetType = TargetType.DAS;
            } else {
                targetType = TargetType.SERVER;                       
            }
        } else if (ClusterHelper.isACluster(domainContext, targetName)) {
            targetType = TargetType.CLUSTER;
        } else if (NodeAgentHelper.isANodeAgent(domainContext, targetName)) {
            targetType = TargetType.NODE_AGENT;
        } else {
            //Should throw more specific exception
            throw new ConfigException(
                strMgr.getString("target.cant_determine_type", targetName));
        }
        return targetType;
    }

    protected String getDefaultTarget(ConfigContext cc) throws ConfigException
    {
        //We assume that the 0th server is the DAS
        Server[] servers = ServerHelper.getServersInDomain(cc);
        if (servers.length > 0) {
            return servers[0].getName();
        } else {
            throw new ConfigException(
                strMgr.getString("target.no_servers"));
        }
    }

    private void checkArg(Object o, Object name)
    {        
        if (null == o)
        {
            throw new IllegalArgumentException(
                strMgr.getString("target.cant_be_null", name.toString()));
        }
    }
    
    //*************************************************
    // getting Target correspondent to config node xpath
    //*************************************************
    
    // predefined xpath prefixes for different Target types
    private static final String XPATH_CONFIG_PREFIX = "/" + ServerTags.DOMAIN + "/" + 
                                ServerTags.CONFIGS + "/" + 
                                ServerTags.CONFIG + "[@" + ServerTags.NAME + "='";
    private static final String XPATH_SERVER_PREFIX = "/" + ServerTags.DOMAIN + "/" + 
                                ServerTags.SERVERS + "/" + 
                                ServerTags.SERVER + "[@" + ServerTags.NAME + "='";
    private static final String XPATH_CLUSTER_PREFIX = "/" + ServerTags.DOMAIN + "/" + 
                                ServerTags.CLUSTERS + "/" + 
                                ServerTags.CLUSTER + "[@" + ServerTags.NAME + "='";
    private static final String XPATH_NODE_AGENT_PREFIX = "/" + ServerTags.DOMAIN + "/" + 
                                ServerTags.NODE_AGENTS + "/" + 
                                ServerTags.NODE_AGENT + "[@" + ServerTags.NAME + "='";
    private static final String XPATH_DOMAIN_PREFIX = "/" + ServerTags.DOMAIN + "/";

    private static final String XPATH_APPLICATIONS_PREFIX = "/" + ServerTags.DOMAIN + "/" + ServerTags.APPLICATIONS + "/";
    private static final String XPATH_RESOURCES_PREFIX = "/" + ServerTags.DOMAIN + "/" + ServerTags.RESOURCES + "/";
    
    /* 
     *  private helper method - returns name for named prefixed xpath
     *  if prefix matches xpath
     */
    private static  String[] extractElementNameAndTypeForPrefix(String xpath, String prefix)
                        throws ConfigException
    {
        xpath = xpath.trim();
        if(xpath!=null && xpath.startsWith(prefix) && xpath.endsWith("']"))
        {
            xpath = xpath.substring(prefix.length(), xpath.length()-2);
            int idx   = xpath.lastIndexOf('[');
            if(idx<0)
                return null;
            String type = xpath.substring(0, idx);
            if(type.indexOf('\'')>=0)
                return null;
            idx = xpath.indexOf('\'', idx);
            if(idx<0)
                return null;
            String key = xpath.substring(idx+1);
            if(key.indexOf('\'')>=0)
                return null;
            return (new String[]{key, type});
        }
        return null;
    }

    /* 
     *  private helper method - returns name for named prefixed xpath
     *  if prefix matches xpath
     */
    private static String extractElementNameForPrefix(String xpath, String prefix)
                        throws ConfigException
    {
        String name = null;
        if(xpath!=null && xpath.startsWith(prefix))
        {
            if(prefix.endsWith("'"))
            {
                int    beg = prefix.length();
                int    end = xpath.indexOf("'", beg); 
                if(end>=0)
                    name = xpath.substring(beg, end);
            }
        }
        return name;
    }

    /* 
     *  private helper method - testing by Target type predefined xpath prefixes
     *  returns Target object if xpath element belongs to type
     *  
     */
    private Target testForPrefix(String xpath, String prefix, TargetType type, ConfigContext domainContext)
                        throws ConfigException
    {
        if(xpath!=null && xpath.startsWith(prefix))
        {
            String name = extractElementNameForPrefix(xpath, prefix);
            return createTarget(type.getName(), null,  name, domainContext);
        }
        return null;
    }
    
    /* creates Target object for specified config element's xpath
     * @param xpath config element's xpath 
     * @param domainContext 
     * @throws ConfigException
     */
    public Target createTargetForXPath(String xpath, ConfigContext domainContext)
        throws ConfigException
    {
        Target target = null;
        if( (target=testForPrefix(xpath, XPATH_CONFIG_PREFIX,  TargetType.CONFIG,  domainContext))!=null ||
            (target=testForPrefix(xpath, XPATH_SERVER_PREFIX,  TargetType.SERVER,  domainContext))!=null ||
           // (target=testForPrefix(xpath, XPATH_CLUSTER_PREFIX, TargetType.CLUSTER, domainContext))!=null ||
            (target=testForPrefix(xpath, XPATH_NODE_AGENT_PREFIX, TargetType.NODE_AGENT, domainContext))!=null ||
            // DOMAIN should be last 
            (target=testForPrefix(xpath, XPATH_DOMAIN_PREFIX, TargetType.DOMAIN, domainContext))!=null )
            return target;
        return null;
    }

    /* 
     * returns target name
     * @param xpath config element's xpath 
     * @param domainContext 
     * @bIncludingAppAndRes - including virtual "target" names for applications and resources
     * @throws ConfigException
     */
    public String getTargetNameForXPath(String xpath, ConfigContext ctx, boolean bIncludingAppAndRes )
        throws ConfigException
    {
        
        if(bIncludingAppAndRes)
        {
            //now time for applications
            String[] pair = null;
            if((pair=extractElementNameAndTypeForPrefix(xpath, XPATH_APPLICATIONS_PREFIX))!=null)
               return ServerTags.APPLICATIONS+"|"+pair[0]+"|"+pair[1];
            //now time for applications
            if((pair=extractElementNameAndTypeForPrefix(xpath, XPATH_RESOURCES_PREFIX))!=null)
               return ServerTags.RESOURCES+"|"+pair[0]+"|"+pair[1];
                
        }
        Target target = createTargetForXPath(xpath, ctx);
        return target.getName();        
    }
}
