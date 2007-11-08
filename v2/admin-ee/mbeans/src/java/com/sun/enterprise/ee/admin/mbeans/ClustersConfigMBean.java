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

package com.sun.enterprise.ee.admin.mbeans;

import com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException;
import java.util.Arrays;
import javax.management.MBeanException;
import javax.management.ObjectName;

import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.WebContainerAvailability;



import com.sun.logging.ee.EELogDomains;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;

/*
    ISSUE: Do we really want to throws an AgentException here as this will
    clients using this mbean to have our runtime; however we seem to be
    throwing our own exceptions everywhere else in the mbeans. The problem with
    MBeanException currently is that it masks the real exception (due to the
    fact that MBeanHelper does some bogus formatting on the exception).
 */

public class ClustersConfigMBean extends  EEBaseConfigMBean
        implements IAdminConstants, com.sun.enterprise.ee.admin.mbeanapi.ClustersConfigMBean, ClusterHealthCheckerMBean
{
    private final ClusterHealthCheckerMBean chc;
    public ClustersConfigMBean()
    {
        super();
        chc = new ClusterHealthChecker(super.getConfigContext());
    }
    
    public String[] listClustersAsString(
            String targetName, boolean withStatus) throws InstanceException
    {
        return getClustersConfigBean().listClustersAsString(targetName, withStatus);
    }
    
    
    /**
     * Lists clusters.
     */
    public ObjectName[] listClusters(String targetName)
    throws InstanceException, MBeanException
    {
        return toClusterONArray(listClustersAsString(targetName, false));
    }
    
    public RuntimeStatusList getRuntimeStatus(
            String clusterName) throws InstanceException
    {
        return getClustersConfigBean().getRuntimeStatus(clusterName);
    }
    
    public void clearRuntimeStatus(String clusterName)
    throws InstanceException
    {
        getClustersConfigBean().clearRuntimeStatus(clusterName);
    }
    
    /**
     * Starts the specified cluster. This operation is invoked by the
     * asadmin start-cluster command.
     */
    public RuntimeStatusList startCluster(String clusterName)
    throws InstanceException
    {
        return startCluster(null, clusterName);
    }
    
    /**
     * Starts the specified cluster. This operation is invoked by the
     * asadmin start-cluster command.
     *
     */
    public RuntimeStatusList startCluster(String autoHadbOverride, String clusterName)
    throws InstanceException
    {
        boolean hadbWasStarted = false;
        boolean startHadb = getAutoHadb(autoHadbOverride, clusterName);
        
        if(startHadb)
        {
            try
            {
                HadbConfigMBean mbean = new HadbConfigMBean();
                String status = mbean.getDatabaseStatus(null, null, null, null, clusterName);
                if("FaultTolerant".equals(status))
                {
                    String s =_strMgr.getString("CSCMB.starthadbwarning", new Object[] { clusterName });
                    getLogger().log(Level.WARNING, s);
                }
                else
                {
                    mbean.startDB(null, null, null, null, clusterName);
                    hadbWasStarted = true;
                }
            }
            catch(Exception e)
            {
                String s =_strMgr.getString("CSCMB.starthadberror", new Object[] { clusterName, e});
                getLogger().log(Level.WARNING, s);
                throw new InstanceException(s);
            }
        }
        try
        {
            return getClustersConfigBean().startCluster(clusterName);
        }
        catch(InstanceException ie)
        {
            // rollback the start
            if(hadbWasStarted)
            {
                HadbConfigMBean mbean = new HadbConfigMBean();
                try
                {
                    mbean.stopDB(null, null, null, null, clusterName);
                }
                catch(Exception e)
                {
                }
            }
            
            throw ie;
        }
    }
    
    
    /**
     * Stops the specified server instance. This operation is invoked by the
     * asadmin stop-instance command.
     */
    
    public RuntimeStatusList stopCluster(String clusterName)
    throws InstanceException
    {
        // note: it's important for the first arg to be null -- not false
        return stopCluster(null, clusterName);
    }
    
    /**
     * Stops the specified server instance. This operation is invoked by the
     * asadmin stop-instance command.
     */
    public RuntimeStatusList stopCluster(String autoHadbOverride, String clusterName)
    throws InstanceException
    {
        boolean stopHadb = getAutoHadb(autoHadbOverride, clusterName);
        
        RuntimeStatusList ret = null;
        
        // stop cluster first -- then HADB
        
        ret = getClustersConfigBean().stopCluster(clusterName);
        
        if(stopHadb)
        {
            try
            {
                HadbConfigMBean mbean = new HadbConfigMBean();
                mbean.stopDB(null, null, null, null, clusterName);
            }
            catch(Exception e)
            {
                String s =_strMgr.getString("CSCMB.stophadberror", new Object[] { clusterName, e});
                getLogger().log(Level.WARNING, s);
                throw new InstanceException(s);
            }
        }
        
        return ret;
    }
    
    /**
     * Deletes the specified server instance. This operation is invoked by the asadmin delete-instance
     * command.
     */
    public void deleteCluster(String clusterName)
    throws InstanceException
    {
        deleteCluster(null, clusterName);
    }
    
    public void deleteCluster(String autoHadbOverride, String clusterName)
    throws InstanceException
    {
        // here is a BIG complication:
        // we should NOT delete the HADB instance until after the cluster is
        // SUCCESSFULLY deleted.  I.e. if delete-cluster fails - leave the DB in place.
        // The complication is that once the cluster is deleted, the name of the hosts
        // and the HADB Agent port is gone!
        // Solution: fetch this info BEFORE the cluster is deleted.
        
        boolean deleteHadb = getAutoHadb(autoHadbOverride, clusterName);
        HadbConfigMBean mbean = new HadbConfigMBean();
        String hosts = null;
        String agentPort = null;
        
        if(deleteHadb)
        {
            try
            {
                hosts		= mbean.getHosts(clusterName);
                agentPort	= mbean.getAgentPort(clusterName);
            }
            catch(Exception e)
            {
                String s =_strMgr.getString("CSCMB.fetcherror", new Object[] { clusterName, e});
                getLogger().log(Level.WARNING, s);
            }
        }
        
        // delete cluster first -- then HADB
        
        getClustersConfigBean().deleteCluster(clusterName);
        
        com.sun.enterprise.ManagementObjectManager mgmtObjManager =
                com.sun.enterprise.Switch.getSwitch().getManagementObjectManager();
        mgmtObjManager.unregisterJ2EECluster(clusterName);
        
        // If there was an error we will leave before getting here.
        if(deleteHadb)
        {
            try
            {
                mbean.deleteHACluster(hosts, agentPort, null, null, clusterName);
            }
            catch(Exception e)
            {
                String s =_strMgr.getString("CSCMB.removehadberror", new Object[] { clusterName, e});
                getLogger().log(Level.WARNING, s);
                throw new InstanceException(s);
            }
        }
    }
    
    /**
     * Creates a new server instance. This operation is invoked by the asadmin create-instance
     * command.
     */
    public ObjectName createCluster(String clusterName,
            String configName, Properties props) throws InstanceException, MBeanException
    {
        getClustersConfigBean().createCluster(clusterName, configName, props);
        
        com.sun.enterprise.ManagementObjectManager mgmtObjManager =
                com.sun.enterprise.Switch.getSwitch().getManagementObjectManager();
        mgmtObjManager.registerJ2EECluster(clusterName);
        
        return getClusterObjectName(clusterName);
    }
    
    /**
     * Creates a new server instance. This operation is invoked by the asadmin create-instance
     * command.
     */
    public ObjectName createCluster(
            String clusterName,
            String configName,
            Properties props,
            String		hosts,
            String		haagentport,
            String		haadminpassword,
            String		haadminpasswordfile,
            String		devicesize,
            Properties	haprops,
            Boolean		autohadb,
            String		portbase) throws InstanceException, MBeanException
    {
        ObjectName ret = null;
        
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append("ClustersConfigMBean.createCluster called with: ");
            sb.append("[").append("clusterName").append("=").append(clusterName).append("]");
            sb.append("[").append("configName").append("=").append(configName).append("]");
            sb.append("[").append("props").append("=").append(props).append("]");
            sb.append("[").append("hosts").append("=").append(hosts).append("]");
            sb.append("[").append("haagentport").append("=").append(haagentport).append("]");
            sb.append("[").append("haadminpassword").append("=").append(haadminpassword).append("]");
            sb.append("[").append("haadminpasswordfile").append("=").append(haadminpasswordfile).append("]");
            sb.append("[").append("devicesize").append("=").append(devicesize).append("]");
            sb.append("[").append("haprops").append("=").append(haprops).append("]");
            sb.append("[").append("autohadb").append("=").append(autohadb).append("]");
            sb.append("[").append("portbase").append("=").append(portbase).append("]");
            
            getLogger().log(Level.INFO, sb.toString());
        }
        catch(Exception e)
        {
        }
        
        // Check for user-input errors...
        
        boolean haRequired =	(hosts					!= null) ? true : false;
        boolean haOptional =	(((haagentport			!= null) ? true : false) ||
                ((haadminpassword		!= null) ? true : false) ||
                ((haadminpasswordfile	!= null) ? true : false) ||
                ((haprops				!= null) ? true : false) ||
                ((autohadb				!= null && autohadb == true) ? true : false) ||
                ((portbase				!= null) ? true : false));
        
        // it's an error to specify optional args but not the required arg.
        if(haOptional && !haRequired)
        {
            String s =_strMgr.getString("CSCMB.nohosts");
            getLogger().log(Level.WARNING, s);
            
            // s has everything we need in it now...
            throw new MBeanException(new HADBSetupException(s));
        }
        
        boolean configha = haRequired;
        ret = createCluster(clusterName, configName, props);
        
        if(configha)
        {
            try
            {
                HadbConfigMBean mbean = new HadbConfigMBean();
                Object[] objs = mbean.createHACluster(
                        hosts,
                        haagentport,
                        haadminpassword,
                        haadminpasswordfile,
                        devicesize,
                        autohadb,
                        portbase,
                        clusterName,
                        haprops);
                String s =_strMgr.getString("CSCMB.createhadbOK", new Object[] { clusterName, Arrays.toString(objs)});
                getLogger().log(Level.INFO, s);
            }
            catch(Exception e)
            {
                // Kludge-fest.
                // I tried "throw new MBeanException(e, s);" but CLI totally ignored "s".
                // there also is no MBeanException(String) constructor.
                // So I'm bending CLI to my will by reconstructing the original Exception
                // note: YARRRGGGHHH!!
                String s =_strMgr.getString("CSCMB.createhadberror", new Object[] { clusterName, e});
                getLogger().log(Level.WARNING, s);
                
                // never call a method that can throw an Exception from a catch block!!
                try
                {
                    deleteCluster("false", clusterName);
                }
                catch(Exception e2)
                {
                    s += e2.getMessage();
                }
                
                // s has everything we need in it now...
                throw new MBeanException(new HADBSetupException(s));
            }
        }
        
        return ret;
    }
    
    private boolean getAutoHadb(String autoHadbOverride, String clusterName) throws InstanceException
    {
        // the autoHadbOverride option has three states -- true, false, default.
        // default (null)  means use whatever is in the cluster-config
        // If this cluster uses REPLICATION rather than HA -- return flse -- fast!
        
        boolean ret = false;

        if(getPersistenceType(clusterName) != PersistenceType.HA)
            return false;
        
        if(autoHadbOverride != null && autoHadbOverride.length() > 0)
        {
            // enforce that it is either "true" or "false"
            String ah = autoHadbOverride.toLowerCase();
            
            if(ah.equals("true"))
                ret = true;
            else if(ah.equals("false"))
                ret = false;
            else
            {
                String s =_strMgr.getString("CSCMB.badAutoHadbOverrideArg", autoHadbOverride);
                getLogger().log(Level.WARNING, s);
                throw new InstanceException(s);
            }
        }
        else
        {
            // autoHadbOverride is null or empty -- use persisted value...
            ret = getAutoHadbFromConfig(clusterName);
        }
        
        String s =_strMgr.getString("CSCMB.autohadbResult", new Boolean(ret));
        getLogger().log(Level.INFO, s);
        return ret;
    }
    
    private boolean getAutoHadbFromConfig(String clusterName)
    {
        try
        {
            HadbConfigMBean mbean = new HadbConfigMBean();
            Boolean b = mbean.getAutoHadb(clusterName);
            
            if(b != null)
                return b;
        }
        catch(Exception e)
        {
            // fall through...
        }
        return false;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private static Logger getLogger()
    {
        if (_logger == null)
        {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private PersistenceType getPersistenceType(String clusterName)
    {
        try
        {
            Config                      config      = ClusterHelper.getConfigForCluster(getConfigContext(), clusterName);
            AvailabilityService         avail       = config.getAvailabilityService();
            WebContainerAvailability    webAvail    = avail.getWebContainerAvailability();
            String type = webAvail.getPersistenceType();

            if("ha".equals(type))
                return PersistenceType.HA;
        }
        catch(ConfigException ce)
        {
            // fall through and return the default.
        }
        return PersistenceType.REPLICATION;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private enum PersistenceType { HA, REPLICATION };
    private static final StringManager _strMgr = StringManager.getManager(ClustersConfigMBean.class);
    private static Logger _logger = null;
    
    public Map<String, List<Long>> getClusterHealth(final String targetCluster) throws InstanceException
    {
        return ( chc.getClusterHealth(targetCluster) );
    }
}
