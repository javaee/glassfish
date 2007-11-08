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

import com.sun.enterprise.ee.admin.hadbmgmt.HADBRuntimeInfo;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBPingAgent;
import java.util.logging.*;
import java.util.*;
import javax.management.MBeanException;
import javax.management.Attribute;

import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBConfigureCluster;

import com.sun.enterprise.ee.admin.hadbmgmt.HADBConfigurePersistence;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBConfigurePersistenceInfo;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBInfo;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBCreateDBInfo;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBRemoveCluster;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBCreateSchema;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBRemoveClusterInfo;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBStopDB;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBStartDB;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBRestartDB;
import com.sun.enterprise.ee.admin.hadbmgmt.HADBResourceManager;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 * This is the MBean interface to the world of HADB Administration
 * @author Byron Nevins
 * @since 8.0
 */
public class HadbConfigMBean extends EEBaseConfigMBean
    implements com.sun.enterprise.ee.admin.mbeanapi.HadbConfigMBean
{
    /**
     * Does nothing
     */
    public HadbConfigMBean()
    {
        super();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private Logger getLogger()
    {
        if (logger == null)
        {
            logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return logger;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Convenience method
     * Designed for GUI
     * @param props The usual arguments packaged in a Properties object
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors
     * @return Display Text
     * @since 8.0
     */
    public Object[] createHACluster(Properties props) throws HADBSetupException
    {
        String		hosts;
        String		agentPort;
        String		haAdminPassword;
        String		haAdminPasswordFile;
        String		deviceSize;
        Boolean		autohadb;
        String		portbase;
        String		clusterName;
        Properties	hadbProps = new Properties();
        
        // Properties is derived from java.util.Hashtable<Object,Object> -- which
        // is weird, but that's the fact!
        Set<Map.Entry<Object,Object>> propSet = props.entrySet();
        
        if(propSet == null || propSet.size() < 2)
            throw new IllegalArgumentException();
        
        // get (and remove) the official CLI attributes
        hosts				= getValue(propSet, "hosts");
        agentPort			= getValue(propSet, "agentPort");
        haAdminPassword		= getValue(propSet, "haAdminPassword");
        haAdminPasswordFile	= getValue(propSet, "haAdminPasswordFile");
        deviceSize			= getValue(propSet, "deviceSize");
        portbase			= getValue(propSet, "portbase");
        clusterName			= getValue(propSet, "clusterName");
        
        String s = getValue(propSet, "autohadb");
        if(s == null)
            autohadb = null;
        else
            autohadb = new Boolean(s);
        
        // stuff the leftovers into the generic Properties object
        
        for(Map.Entry<Object,Object> entry : propSet)
        {
            hadbProps.setProperty((String)entry.getKey(), (String)entry.getValue());
        }
        
        return createHACluster(
            hosts,
            agentPort,
            haAdminPassword,
            haAdminPasswordFile,
            deviceSize,
            autohadb,
            portbase,
            clusterName,
            hadbProps
            );
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create an HADB instance, start it and then associate it with the cluster
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param deviceSize The size of the database
     * @param autohadb Sets the automatic cluster lifecycling
     * @param portbase THe port number base used for communicating with the database itself
     * @param clusterName The name of the cluster
     * @param props The <b>hadbm create</b> command has many more options then CLI explicitly defined.
     * These other arguments can be packed into props.
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 8.0
     */
    public Object[] createHACluster(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String deviceSize,
        Boolean autohadb,
        String portbase,
        String clusterName,
        Properties props) throws HADBSetupException
    {
        HADBCreateDBInfo info = null;
        
        try
        {
            String pw = (haAdminPassword == null ? "null" : "******");
            String s = "HADBConfigMBean.createHACluster() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", deviceSize=" + deviceSize +
                ", autohadb=" + autohadb +
                ", portbase=" + portbase +
                ", clusterName=" + clusterName +
                ", props=" + props;
            
            getLogger().log(Level.INFO, s);
            
            info = new HADBCreateDBInfo(
                hosts,
                agentPort,
                haAdminPassword,
                haAdminPasswordFile,
                autohadb,
                portbase,
                clusterName,
                getLogger(),
                getConfigContext(),
                getMBeanServer());
            
            info.setup();
            info.setDeviceSize(deviceSize);
            info.setProperties(props);
            HADBConfigureCluster hcc = new HADBConfigureCluster(info);
            return hcc.configure();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    
    /**
     * Does the following:<ul><li>Stops the HADB instance<li>Deletes the HADB instance
     * <li>Removes configuration information.</ul>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 8.0
     */
    public Object[] deleteHACluster(
        String clusterName) throws HADBSetupException
    {
        return deleteHACluster(null,null,null,null,clusterName);
    }
    
    /**
     * Does the following:<ul><li>Stops the HADB instance<li>Deletes the HADB instance
     * <li>Removes configuration information.</ul>
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 8.0
     */
    public Object[] deleteHACluster(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String clusterName) throws HADBSetupException
    {
        HADBRemoveClusterInfo info = null;
        
        try
        {
            String pw = (haAdminPassword == null ? "null" : "******");
            String s = "HADBConfigMBean.deleteHACluster() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", clusterName=" + clusterName;
            
            getLogger().log(Level.INFO, s);
            
            info = new HADBRemoveClusterInfo(hosts, agentPort, haAdminPassword, haAdminPasswordFile,  clusterName, getLogger(),
                getConfigContext(), getMBeanServer());
            info.setup();
            HADBRemoveCluster rc = new HADBRemoveCluster(info);
            return rc.remove();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    /**
     * Create the database tables
     * @return Display Text
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param storeuser the database user name
     * @param storepassword the database password
     * @param dbsystempassword the database system password
     * @param databaseName the name of the database
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     */
    public Object[] createHASchema(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String storeuser,
        String storepassword,
        String dbsystempassword,
        String databaseName) throws HADBSetupException
    {
        HADBCreateDBInfo info = null;
        
        try
        {
            String a_pw = (haAdminPassword == null ? "null" : "******");
            String d_pw = (dbsystempassword == null ? "null" : "******");
            String s_pw = (storepassword == null ? "null" : "******");
            String s = "HADBConfigMBean.createHASchema() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + a_pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", storeuser=" + storeuser +
                ", storepassword=" + s_pw +
                ", dbsystempassword=" + d_pw +
                ", databaseName=" + databaseName;
            
            getLogger().log(Level.INFO, s);
            
            info = new HADBCreateDBInfo(
                hosts,
                agentPort,
                haAdminPassword,
                haAdminPasswordFile,
                false, // auto-hadb not used
                null, // portbase not used
                databaseName,
                getLogger(),
                getConfigContext(),
                getMBeanServer());
            
            // important -- call these methods BEFORE calling setup()
            info.setDatabaseUser(storeuser);
            info.setDatabasePassword(storepassword);
            info.setSystemPassword(dbsystempassword);
            
            info.setup();
            
            HADBCreateSchema creator = new HADBCreateSchema(info);
            return creator.create();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    /**
     * Clear the database tables
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 8.0
     */
    public Object[] clearHASchema(
        String databaseName) throws HADBSetupException
    {
        return clearHASchema(null,null,null,null,null,null,null,databaseName);
    }
    /**
     * Clear the database tables
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 8.0
     */
    public Object[] clearHASchema(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String storeuser,
        String storepassword,
        String dbsystempassword,
        String databaseName) throws HADBSetupException
    {
        HADBCreateDBInfo info = null;
        
        try
        {
            String a_pw = (haAdminPassword == null ? "null" : "******");
            String d_pw = (dbsystempassword == null ? "null" : "******");
            String s_pw = (storepassword == null ? "null" : "******");
            String s = "HADBConfigMBean.createHASchema() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + a_pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", storeuser=" + storeuser +
                ", storepassword=" + s_pw +
                ", dbsystempassword=" + d_pw +
                ", databaseName=" + databaseName;
            
            getLogger().log(Level.INFO, s);
            
            info = new HADBCreateDBInfo(
                hosts,
                agentPort,
                haAdminPassword,
                haAdminPasswordFile,
                false, // auto-hadb not used
                null, // portbase not used
                databaseName,
                getLogger(),
                getConfigContext(),
                getMBeanServer());
            
            // important -- call these methods BEFORE calling setup()
            info.setDatabaseUser(storeuser);
            info.setDatabasePassword(storepassword);
            info.setSystemPassword(dbsystempassword);
            
            info.setup();
            
            HADBCreateSchema creator = new HADBCreateSchema(info);
            return creator.clear();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    /**
     * A convenience method for setting configuration items
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 8.0
     */
    public Object[] configureHAPersistence(
        String type,
        String frequency,
        String scope,
        String store,
        Properties props,
        String clusterName	) throws HADBSetupException
    {
        try
        {
            getLogger().log(Level.INFO, "HADBConfigMBean.configureHAPersistence() called with " +
                "type = " + type + ", frequency = " + frequency + ", scope = " + scope +", store = " +
                store + ", clusterName = " + clusterName + ", props = " + props);
            
            HADBConfigurePersistenceInfo info = new HADBConfigurePersistenceInfo(
                clusterName, getLogger(), getConfigContext(),
                getMBeanServer(), type, frequency, scope, store, props);
            info.setup();
            
            HADBConfigurePersistence worker = new HADBConfigurePersistence(info);
            return worker.commit();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
    }
    /**
     * Stop the HADB instance
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 9.0
     */
    public Object[] stopDB(
        String clusterName) throws HADBSetupException
    {
        return stopDB(null,null,null,null,clusterName);
    }
    /**
     * Stop the HADB instance
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 9.0
     */
    public Object[] stopDB(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String clusterName) throws HADBSetupException
    {
        HADBInfo info = null;
        
        try
        {
            String pw = (haAdminPassword == null ? "null" : "******");
            String s = "HADBConfigMBean.stopDB() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", clusterName=" + clusterName;
            
            getLogger().log(Level.INFO, s);
            info = new HADBInfo(hosts, agentPort, haAdminPassword, haAdminPasswordFile,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setup();
            HADBStopDB sdb = new HADBStopDB(info);
            return sdb.stopDB();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    
    /**
     * Start the HADB instance
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 9.0
     */
    public Object[] startDB(
        String clusterName) throws HADBSetupException
    {
        return startDB(null,null,null,null,clusterName);
    }
    /**
     * Start the HADB instance
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 9.0
     */
    
    public Object[] startDB(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String clusterName) throws HADBSetupException
    {
        HADBInfo info = null;
        
        try
        {
            String pw = (haAdminPassword == null ? "null" : "******");
            String s = "HADBConfigMBean.startDB() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", clusterName=" + clusterName;
            
            getLogger().log(Level.INFO, s);
            
            info = new HADBInfo(hosts, agentPort, haAdminPassword, haAdminPasswordFile,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setup();
            HADBStartDB sdb = new HADBStartDB(info);
            return sdb.startDB();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    /**
     * Restart the HADB instance
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 9.0
     */
    public Object[] restartDB(
        String clusterName) throws HADBSetupException
    {
        return restartDB(null,null,null,null,clusterName);
    }
    /**
     * Restart the HADB instance
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 9.0
     */
    public Object[] restartDB(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String clusterName) throws HADBSetupException
    {
        HADBInfo info = null;
        
        try
        {
            String pw = (haAdminPassword == null ? "null" : "******");
            String s = "HADBConfigMBean.restartDB() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", clusterName=" + clusterName;
            
            getLogger().log(Level.INFO, s);
            info = new HADBInfo(hosts, agentPort, haAdminPassword, haAdminPasswordFile,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setup();
            HADBRestartDB rsdb = new HADBRestartDB(info);
            return rsdb.restartDB();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Are the HADB client binaries installed?
     * @return true if HADB is installed, false if it can't be found
     */
    public Object[] isHadbInstalled()
    {
        Object[] ret = HADBInfo.isHadbInstalled();
        // log it
        String s = "HADBConfigMBean.isHadbInstalled() called.  Returned: " +
            Arrays.toString(ret);
        
        getLogger().log(Level.INFO, s);
        return ret;
    }
    /**
     * Determine if the HADB Management Agent is reachable
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 9.0
     */
    public Object[] pingHadbAgent(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String clusterName) throws HADBSetupException
    {
        HADBInfo info = null;
        
        try
        {
            String pw = (haAdminPassword == null ? "null" : "******");
            String s = "HADBConfigMBean.pingHadbAgent() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", clusterName=" + clusterName;
            
            getLogger().log(Level.INFO, s);
            info = new HADBInfo(hosts, agentPort, haAdminPassword, haAdminPasswordFile,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setup();
            HADBPingAgent pinger = new HADBPingAgent(info);
            return pinger.ping();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Convenience method for getting the value of auto-hadb
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return the value of auto-hadb
     * @since 9.0
     */
    public Boolean getAutoHadb(String clusterName) throws HADBSetupException
    {
        HADBInfo info = null;
        
        try
        {
            getLogger().log(Level.INFO, "HADBConfigMBean.getAutoHadb() called with cluster = " +
                clusterName);
            info = new HADBInfo(null, null, null, null,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setHostsRequired(false);
            info.setup();
            HADBResourceManager hrm = new HADBResourceManager(info);
            return hrm.getAutoHadbFromConfig() && hrm.isAvailabilityEnabled();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Convenience method for setting the value of auto-hadb
     * @param newAutoHadb the new value to set for auto-hadb in the cluster configuration
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException In case of any errors
     * @since 9.0
     */
    public void setAutoHadb(Boolean newAutoHadb, String clusterName) throws HADBSetupException
    {
        HADBInfo info = null;
        
        try
        {
            getLogger().log(Level.INFO, "HADBConfigMBean.setAutoHadb() called with cluster = " +
                clusterName + ", new autohadb = " + newAutoHadb);
            info = new HADBInfo(null, null, null, null,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setup();
            HADBResourceManager hrm = new HADBResourceManager(info);
            hrm.setAutoHadb(newAutoHadb);
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     *
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException In case of any errors
     * @return the hosts
     * @since 9.0
     */
    public String getHosts(String clusterName) throws HADBSetupException
    {
        HADBInfo info = null;
        
        try
        {
            info = new HADBInfo(null, null, null, null,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setup();
            HADBResourceManager hrm = new HADBResourceManager(info);
            String hosts = hrm.getHostsFromConfig();
            getLogger().log(Level.INFO, "HADBConfigMBean.getHosts() called with cluster = " +
                clusterName + " Returned: " + hosts);
            return hosts;
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
    }
    
    /**
     * Get the agent port number from config.
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return The port number as a String
     * @since 9.0
     */
    public String getAgentPort(String clusterName) throws HADBSetupException
    {
        HADBInfo info = null;
        
        try
        {
            info = new HADBInfo(null, null, null, null,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setup();
            HADBResourceManager hrm = new HADBResourceManager(info);
            String port = hrm.getAgentPortFromConfig();
            getLogger().log(Level.INFO, "HADBConfigMBean.getAgentPort() called with cluster = " +
                clusterName + " Returned: " + port);
            return port;
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
    }
    /**
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 9.0
     */
    public Object[] getHADBInfo(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String clusterName) throws HADBSetupException
    {
        
        Properties p = 	getHADBDatabaseAttributes(
            hosts,
            agentPort,
            haAdminPassword,
            haAdminPasswordFile,
            clusterName);
        
        // attrs is sorted
        List<String> info = nameValuePropsToStrings(p);
        
        // add the general info
        info.add(0, getHADBRuntimeInfo(
            hosts,
            agentPort,
            haAdminPassword,
            haAdminPasswordFile,
            clusterName));
        
        // add a header
        info.add(1, STR_MGR.getString("HADB.RuntimeAttributesHeader"));
        
        return info.toArray();
    }
    
    /**
     * designed for CLI use
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 9.0
     */
    public Object[] setAndThenGetHADBInfo(
        String		hosts,
        String		agentPort,
        String		haAdminPassword,
        String		haAdminPasswordFile,
        String		clusterName,
        String		props)  throws HADBSetupException
    {
        // pprops are from a call to 'get --all'
        // AFTER the setting completed...
        Properties pprops = setHADBDatabaseAttributes(
            hosts,
            agentPort,
            haAdminPassword,
            haAdminPasswordFile,
            clusterName,
            props);
        
        List<String> info = nameValuePropsToStrings(pprops);
        return info.toArray();
    }
    /**
     * designed for CLI
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 9.0
     */
    
    public Properties setHADBDatabaseAttributes(
        String		hosts,
        String		agentPort,
        String		haAdminPassword,
        String		haAdminPasswordFile,
        String		clusterName,
        String		props)  throws HADBSetupException
    {
        return setHADBDatabaseAttributes(
            hosts,
            agentPort,
            haAdminPassword,
            haAdminPasswordFile,
            clusterName,
            props,
            null);
    }
    public Properties setHADBDatabaseAttributes(
        String		hosts,
        String		agentPort,
        String		haAdminPassword,
        String		haAdminPasswordFile,
        String		clusterName,
        Properties	props)  throws HADBSetupException
    {
        return setHADBDatabaseAttributes(
            hosts,
            agentPort,
            haAdminPassword,
            haAdminPasswordFile,
            clusterName,
            null,
            props);
    }
    
    public Properties setHADBDatabaseAttributes(
        String		clusterName,
        Properties	props)  throws HADBSetupException
    {
        return setHADBDatabaseAttributes(
            null,
            null,
            null,
            null,
            clusterName,
            null,
            props);
    }
    
    private Properties setHADBDatabaseAttributes(
        String		hosts,
        String		agentPort,
        String		haAdminPassword,
        String		haAdminPasswordFile,
        String		clusterName,
        String		sprops,
        Properties pprops)  throws HADBSetupException
    {
        HADBInfo info = null;
        try
        {
            String pw = (haAdminPassword == null ? "null" : "******");
            String s = "HADBConfigMBean.pingHadbAgent() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", clusterName=" + clusterName +
                ", sprops=" + sprops +
                ", pprops=" + pprops;
            
            getLogger().log(Level.INFO, s);
            info = new HADBInfo(hosts, agentPort, haAdminPassword, haAdminPasswordFile,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setup();
            HADBRuntimeInfo rtinfo = new HADBRuntimeInfo(info);
            
            if(pprops != null)
                rtinfo.setAttributes(pprops);
            else if(sprops != null)
                rtinfo.setAttributes(sprops);
            
            return rtinfo.getAttributes();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    
    
    /**
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 9.0
     */
    public Properties getHADBDatabaseAttributes(
        String clusterName) throws HADBSetupException
    {
        return getHADBDatabaseAttributes(null, null, null, null, clusterName);
    }
    
    public Properties getHADBDatabaseAttributes(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String clusterName) throws HADBSetupException
    {
        HADBInfo info = null;
        try
        {
            String pw = (haAdminPassword == null ? "null" : "******");
            String s = "HADBConfigMBean.pingHadbAgent() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", clusterName=" + clusterName;
            
            getLogger().log(Level.INFO, s);
            info = new HADBInfo(hosts, agentPort, haAdminPassword, haAdminPasswordFile,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setup();
            HADBRuntimeInfo rtinfo = new HADBRuntimeInfo(info);
            readWriteAttributes = rtinfo.getReadWriteAttributes();
            readOnlyAttributes = rtinfo.getReadOnlyAttributes();
            return rtinfo.getAttributes();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    
    /**
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return All the read-only DB attributes
     * @since 9.0
     * Only used by GUI
     */
    public Properties getHADBReadOnlyDatabaseAttributes(
        String clusterName) throws HADBSetupException
    {
        // this call will set readOnlyAttributes
        getHADBDatabaseAttributes(null, null, null, null, clusterName);
        
        return readOnlyAttributes;
    }
    
    /**
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return All the read-write DB attributes
     * @since 9.0
     * Only used by GUI
     */
    public Properties getHADBReadWriteDatabaseAttributes(
        String clusterName) throws HADBSetupException
    {
        // this call will set readWriteAttributes
        getHADBDatabaseAttributes(null, null, null, null, clusterName);
        
        return readWriteAttributes;
    }
    
    /**
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return Display Text
     * @since 9.0
     */
    public String getHADBRuntimeInfo(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String clusterName) throws HADBSetupException
    {
        HADBInfo info = null;
        try
        {
            String pw = (haAdminPassword == null ? "null" : "******");
            String s = "HADBConfigMBean.pingHadbAgent() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", clusterName=" + clusterName;
            
            getLogger().log(Level.INFO, s);
            info = new HADBInfo(hosts, agentPort, haAdminPassword, haAdminPasswordFile,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setup();
            HADBRuntimeInfo rtinfo = new HADBRuntimeInfo(info);
            return rtinfo.getOtherInfo();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    /**
     * designed for GUI
     * @since 9.0
     *
     */
    public Object[] isHA(String clusterName)
    {
        try
        {
            HADBInfo info = new HADBInfo(null, null, null, null,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            
            // This method (isHA) is called ALL THE TIME from the GUI for clusters that have no
            // HA set up.  This next call will prevent an Exception from getting thrown.
            info.setHostsRequired(false);
            
            info.setup();
            HADBResourceManager hrm = new HADBResourceManager(info);
            boolean isha = hrm.isHA();
            getLogger().log(Level.INFO, "HADBConfigMBean.isHA() called with cluster = " +
                clusterName + " Returned: " + isha);
            return new Object[] { isha };
        }
        catch(Exception e)
        {
            return new Object[] { false };
        }
    }
    /**
     * Get the list of nodes that are in the HADB domain
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return properties -- key is the hostname, value is the 'Running' attribute
     * @since 9.0
     */
    public String[] getNodeList(
        String clusterName) throws HADBSetupException
    {
        return getNodeList(null, null, null, null, clusterName);
    }
    
    public String[] getNodeList(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String clusterName) throws HADBSetupException
    {
        HADBInfo info = null;
        try
        {
            String pw = (haAdminPassword == null ? "null" : "******");
            String s = "HADBConfigMBean.getNodeList() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", clusterName=" + clusterName;
            
            getLogger().log(Level.INFO, s);
            info = new HADBInfo(hosts, agentPort, haAdminPassword, haAdminPasswordFile,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setup();
            HADBRuntimeInfo rtinfo = new HADBRuntimeInfo(info);
            return rtinfo.getNodeList();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    
    
    /**
     * Get the status of the domain
     * @param hosts The possibly remote hosts for the hadb domain - format = <b>host1,host2</b>
     * @param agentPort The HADB Management Agent port for the HADB Domain
     * @param haAdminPassword The HADB Admin Password
     * @param haAdminPasswordFile A file containing the HADB Admin Password.  The format in the file is <b>HADBM_ADMINPASSWORD=password</b>
     * @param clusterName The name of the cluster
     * @throws com.sun.enterprise.ee.admin.hadbmgmt.HADBSetupException If any errors are encountered
     * @return the status
     * @since 9.0
     */
    public String getDatabaseStatus(
        String hosts,
        String agentPort,
        String haAdminPassword,
        String haAdminPasswordFile,
        String clusterName) throws HADBSetupException
    {
        HADBInfo info = null;
        try
        {
            String pw = (haAdminPassword == null ? "null" : "******");
            String s = "HADBConfigMBean.getdatabaseStatus() called with " +
                "hosts=" + hosts +
                ", agentPort=" + agentPort +
                ", haAdminPassword=" + pw +
                ", haAdminPasswordFile=" + haAdminPasswordFile +
                ", clusterName=" + clusterName;
            
            getLogger().log(Level.INFO, s);
            info = new HADBInfo(hosts, agentPort, haAdminPassword, haAdminPasswordFile,
                clusterName, getLogger(), getConfigContext(), getMBeanServer());
            info.setup();
            HADBRuntimeInfo rtinfo = new HADBRuntimeInfo(info);
            return rtinfo.getDatabaseStatus();
        }
        catch(HADBSetupException hse)
        {
            throw hse;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        finally
        {
            if(info != null)
                info.cleanup();
        }
    }
    
    /**
     * Look inside this String and see if the given
     * hadbm error number is in there
     */
    
    public boolean isHadbmError(String s, int errno)
    {
        if(s == null)
            return false;
        
        s = s.toLowerCase();
        String err = "hadbm:error " + errno;
        
        return s.indexOf(err) >= 0;
    }
    /**
     * Look inside this String and see if the given
     * hadbm error number is in there
     */
    
    public boolean isAuthError(String s)
    {
        return isHadbmError(s, 22005);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private String getValue(Set<Map.Entry<Object,Object>> set, String name)
    {
        for(Iterator<Map.Entry<Object,Object>> it = set.iterator(); it.hasNext(); )
        {
            Map.Entry<Object,Object> entry = it.next();
            String key = (String)entry.getKey();
            if(key.equalsIgnoreCase(name))
            {
                it.remove();
                return (String)entry.getValue();
            }
        }
        
        return null;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private List<String> nameValuePropsToStrings(Properties props)
    {
        List<String> list = new ArrayList<String>();
        Set<Map.Entry<Object,Object>> set = props.entrySet();
        
        for(Map.Entry<Object,Object> entry : set)
        {
            list.add(new String(entry.getKey().toString() + " = " + entry.getValue().toString()));
        }
        Collections.sort(list);
        return list;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public Object[] backdoor(String s1, String s2, String s3, String s4, String s5)
    {
        List<String> ss = new ArrayList<String>();
        
        if(s1 != null)	ss.add(s1);
        if(s2 != null)	ss.add(s2);
        if(s3 != null)	ss.add(s3);
        if(s4 != null)	ss.add(s4);
        if(s5 != null)	ss.add(s5);
        
        com.sun.enterprise.ee.admin.hadbmgmt.BackDoor bd = new com.sun.enterprise.ee.admin.hadbmgmt.BackDoor(ss);
        return bd.exec();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private Properties readWriteAttributes;
    private Properties readOnlyAttributes;
    
    private static final	StringManager	STR_MGR = StringManager.getManager(HadbConfigMBean.class);
    private static 	        Logger			logger;
    public static final		String			AgentPortKey	= "agent-port";
    //public static final		String			hostsKey		= "agent-port";
    
}
