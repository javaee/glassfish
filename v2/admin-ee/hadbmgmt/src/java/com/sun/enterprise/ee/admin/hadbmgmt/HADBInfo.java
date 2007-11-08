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
 * HADBCreateDBProps.java
 *
 * Created on April 10, 2004, 4:43 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import com.sun.enterprise.admin.config.BaseConfigMBean;
import java.util.*;
import java.util.logging.*;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.admin.util.JMXConnectorConfig;
import com.sun.enterprise.config.StaleWriteConfigException;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileUtils;
import java.io.*;
import javax.management.MBeanServer;

/**
 * @author  bnevins
 */
public class HADBInfo
{
    ////////////////////////////////////////////////////////////////////////////
    //////  Public Methods
    ////////////////////////////////////////////////////////////////////////////
    
    public HADBInfo(String hosts, String agentPort, String theAdminPassword, String userPasswordFile, String clusterOrDbName, Logger logger,
        ConfigContext configCtx, MBeanServer mbeanServer) throws HADBSetupException
    {
        clusterName             = clusterOrDbName;  // this MUST come before setHosts()
        this.configCtx          = configCtx;        // this MUST come before setHosts()
        systemUser              = "system";
        this.mbeanServer        = mbeanServer;
        this.userPasswordFile   = userPasswordFile;
        
        // Bug: 6175898
        // it should be impossible for logger to be null.  The MBean sends a reference to
        // its logger which can't be null.
        // But impossible is as impossible does.  Let's protect against this impossible situation
        // that apparently *is* possible
        // Normally HADBSetupException automatically logs -- it won't now because there is no logger.
        // so we write to stdout
        
        if(logger == null)
        {
            String mesg = StringHelper.get("hadbmgmt-res.InternalError", "MBean passed in a null logger reference.  This is supposed to be impossible.");
            
            // create a logger for just one logging message and then error out...
            logger = Logger.getLogger("javax.enterprise.system.tools.admin.hadbmanagement");
            logger.severe(mesg);
            throw new HADBSetupException(mesg);
        }
        
        LoggerHelper.set(logger);
        
        this.hosts = hosts;
        agentPortString = agentPort;
        
        // note: theAdminPassword overrides userPasswordFile
        setAdminPassword(theAdminPassword);
    }
    
    /**
     * We can't do the setup in the constructor because we need to call a method that
     * may be overridden in derived classes -- which is illegal in java.  So this extra
     * step is neccessary.
     */
    public void setup() throws HADBSetupException
    {
        verifyStandaloneCluster();
        setHosts();
        checkHosts();
        setHadbRoot();
        setVersion();
        setAgentPort();
        setAutoHadb();
        PasswordManager.setPasswords(this, userPasswordFile);
        HADBResourceManager rm = new HADBResourceManager(this);
        String pw = rm.getAdminPasswordFromConfig();
        
        if(ok(pw))
            setAdminPassword(pw);
        try
        {
            JMXConnectorConfig config = ServerHelper.getJMXConnectorInfo(configCtx,
                ServerHelper.getDAS(configCtx).getName());
            dasPassword = config.getPassword();
            setAdminPassword(dasPassword);
            pw = adminPassword;
            setSystemPassword(pw);
            setDatabasePassword(pw);
            setDatabaseUser(config.getUser());
            adminPasswordManager    = new PasswordManager(this, Constants.ADMINPASSWORD, adminPassword);
            dbPasswordManager       = new PasswordManager(this, Constants.DBPASSWORD, dbPassword);
        }
        catch (Exception e)
        {
            throw new HADBSetupException("hadbmgmt-res.InternalError", e);
        }
        wasSetup = true;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final public void cleanup()
    {
        if(adminPasswordManager != null)
            adminPasswordManager.delete();
        if(dbPasswordManager != null)
            dbPasswordManager.delete();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     *
     * @param s
     */
    final public void setSystemPassword(String s)
    {
        // do nothing if the arg is null OR if it is already set to something
        if(!ok(s) || ok(systemPassword))
            return;
        
        systemPassword = s;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final public void setDatabaseUser(String s)
    {
        // do nothing if the arg is null OR if it is already set to something
        if(!ok(s) || ok(dbUser))
            return;
        
        dbUser = s;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final public void setDatabasePassword(String s)
    {
        // do nothing if the arg is null OR if it is already set to something
        if(!ok(s) || ok(dbPassword))
            return;
        
        dbPassword = s;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final public void setAdminPassword(String s)
    {
        // do nothing if the arg is null OR if it is already set to something
        if(!ok(s) || ok(adminPassword))
            return;
        
        adminPassword = s;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    public void setHostsRequired(boolean what)
    {
        hostsRequired = what;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //////  Package-Private Methods
    ////////////////////////////////////////////////////////////////////////////
    
    void validate() throws HADBSetupException
    {
        // implemented optionally in derived classes
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String getAdminPassword()
    {
        return adminPassword;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String getDASPassword()
    {
        return dasPassword;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final PasswordManager getAdminPasswordManager()
    {
        return adminPasswordManager;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    final PasswordManager getDBPasswordManager()
    {
        return dbPasswordManager;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    final void setDatabasePassword(String s, boolean overwrite)
    {
        if(overwrite == false)
            setDatabasePassword(s);
        
        // do nothing if the arg is null
        if(ok(s))
            dbPassword = s;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final void setAdminPassword(String s, boolean overwrite)
    {
        if(overwrite == false)
            setAdminPassword(s);
        
        // do nothing if the arg is null
        if(ok(s))
            adminPassword = s;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String getClusterName() throws HADBSetupException
    {
        if(!ok(clusterName))
            throw new HADBSetupException("hadbmgmt-res.InternalError", "Cluster name has not been set yet.");
        return clusterName;
    }
    
    //Note: Currently there are two user and passwords maintained. The system user / password which
    //gains access to the database, and the database user / password who owns the database tables.
    //This is requred so that a physical database can be partitioned into multiple logical databases
    //owned by different users. This functionality was inherited by Glaucus in the HADBSessionStoreUtil
    //class. Unfortunately, the system user and db user cannot be the same. Here are the default values
    //currently being used:
    //      system-user : hardcoded to "system";
    //      system-password: the admin password
    //      db-user : the admin user
    //      db-password : the admin password
    final String getSystemUser()
    {
        return systemUser;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String getSystemPassword()
    {
        return systemPassword;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String getDatabaseUser() throws HADBSetupException
    {
        if(!ok(dbUser))
            throw new HADBSetupException("hadbmgmt-res.InternalError", "database user not set");
        
        // HADB will convert uppercase to lowercase.  Make sure we don't have any uppercase!!
        dbUser = dbUser.toLowerCase();
        
        // HADB disallows length > 32
        if(dbUser.length() > 32)
            dbUser = dbUser.substring(0, 32);
        
        // check if the username is one of the special built-in HADB users.  If so we cannot use it!
        
        if(isSpecialUser(dbUser))
        {
            String originalDBUser = dbUser;
            dbUser = clusterName;
            if(isSpecialUser(dbUser))
            {
                // both the clustername AND the AS admin username are special!
                // They are trying hard to break us...
                dbUser = makeNonSpecialName(originalDBUser);
            }
        }
        return dbUser;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String getDatabasePassword()
    {
        return dbPassword;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final File getHadbRoot()
    {
        return hadbRoot;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final File getExecutable()
    {
        if(executable == null)
            executable = new File( new File(getHadbRoot(), "bin"), "hadbm");
        
        return executable;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String[] getStartCommands() throws HADBSetupException
    {
        ArrayList args = new ArrayList();
        args.add("start");
        args.add(getJavaRootArg());
        args.add(adminPasswordManager.getArg());
        args.add(getAgentURLArg());
        args.add(clusterName);
        return toArray(args);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String[] getRestartCommands() throws HADBSetupException
    {
        ArrayList args = new ArrayList();
        args.add("restart");
        args.add(getJavaRootArg());
        args.add(adminPasswordManager.getArg());
        args.add(getAgentURLArg());
        args.add(getYesArg(true));
        args.add(clusterName);
        return toArray(args);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String[] getStopCommands() throws HADBSetupException
    {
        ArrayList args = new ArrayList();
        args.add("stop");
        args.add("--quiet");
        args.add(getJavaRootArg());
        args.add(adminPasswordManager.getArg());
        args.add(getYesArg(true));
        args.add(getAgentURLArg());
        args.add(clusterName);
        return toArray(args);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String[] getDeleteCommands() throws HADBSetupException
    {
        ArrayList args = new ArrayList();
        args.add("delete");
        args.add("--quiet");
        args.add(getJavaRootArg());
        args.add(adminPasswordManager.getArg());
        args.add(getYesArg(true));
        args.add(getAgentURLArg());
        args.add(clusterName);
        return toArray(args);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String[] getJdbcURLCommands() throws HADBSetupException
    {
        ArrayList args = new ArrayList();
        args.add("get");
        args.add(getJavaRootArg());
        args.add(adminPasswordManager.getArg());
        args.add("jdbcURL");
        args.add(getAgentURLArg());
        args.add(clusterName);
        return toArray(args);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String[] getGetAttributesCommands() throws HADBSetupException
    {
        ArrayList args = new ArrayList();
        args.add("get");
        args.add(getJavaRootArg());
        args.add(adminPasswordManager.getArg());
        args.add("--all");
        args.add(getAgentURLArg());
        args.add(getYesArg(true));
        args.add(clusterName);
        return toArray(args);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String[] getNodeListCommands() throws HADBSetupException
    {
        ArrayList args = new ArrayList();
        args.add("status");
        args.add(getJavaRootArg());
        args.add(adminPasswordManager.getArg());
        args.add(getAgentURLArg());
        args.add("--nodes");
        args.add(clusterName);
        return toArray(args);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String[] getListPackagesCommands() throws HADBSetupException
    {
        ArrayList args = new ArrayList();
        args.add("listpackages");
        args.add(getJavaRootArg());
        args.add(adminPasswordManager.getArg());
        args.add(getAgentURLArg());
        args.add(getYesArg(true));
        return toArray(args);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String[] getSetAttributesCommands(Properties props) throws HADBSetupException
    {
        //  hadbm set "connectiontrace=true numberOfLocks=110000"
        ArrayList args = new ArrayList();
        args.add("set");
        args.add(getJavaRootArg());
        args.add(adminPasswordManager.getArg());
        args.add(getAgentURLArg());
        args.add(propAttributesToStringAttributes(props));
        args.add(clusterName);
        
        return toArray(args);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String[] getExistsCommands() throws HADBSetupException
    {
        ArrayList args = new ArrayList();
        args.add("status");
        args.add(getJavaRootArg());
        args.add(adminPasswordManager.getArg());
        args.add(getAgentURLArg());
        args.add(clusterName);
        return toArray(args);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String[] getIsAliveCommands(String host) throws HADBSetupException
    {
        ArrayList args = new ArrayList();
        args.add("list");
        args.add(getJavaRootArg());
        args.add(adminPasswordManager.getArg());
        args.add(getAgentURLArg(host));
        return toArray(args);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    String getAgentURLArg() throws HADBSetupException
    {
        return getAgentURLArg(null);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    String getAgentURLArg(String host) throws HADBSetupException
    {
        return "--agent=" + getAgentURL(host);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    
    String getYesArg(boolean what) throws HADBSetupException
    {
        return "--yes=" + (what ? "true" : "false");
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    final String getHosts()
    {
        return hosts;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    final String[] getHostsArray() throws HADBSetupException
    {
        assertSetup();
        return hostsArray;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    final String getHostsAndPorts() throws HADBSetupException
    {
        return getJdbcURL(true);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String getJdbcURL() throws HADBSetupException
    {
        return getJdbcURL(false);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String getJdbcURL(boolean stripDriverName) throws HADBSetupException
    {
        // here is the output from hadbm:
        // ./hadbm get jdbcURL foobar
        // Attribute Value
        // jdbcURL   jdbc:sun:hadb:bulldozer.red.iplanet.com:15205,bulldozer.red.iplanet.com:15225
        
        // FIXME FIXME FIXME !!!
        if(HADBUtils.noHADB())
        {
            return "bilbo:1111,bilbo:1112";
        }
        
        if(!ok(jdbcURL))
        {
            String[] commands = getJdbcURLCommands();
            HADBMExecutor exec = new HADBMExecutor(getExecutable(), commands);
            int exitValue = exec.exec();
            
            if(exitValue != 0)
            {
                throw new HADBSetupException("hadbmgmt-res.GetJdbcUrlFailed",
                    new Object[]
                {"" + exitValue, exec.getStdout(), exec.getStderr()} );
            }
            
            LoggerHelper.fine("***** getJdbcURL STDOUT\n" + exec.getStdout());
            LoggerHelper.fine("***** getJdbcURL STDERR\n" + exec.getStderr());
            // parse the jdbc url returned on stdout. We only want the
            // list of host/port pairs.
            String jdbcUrlString = exec.getStdout();
            int index = jdbcUrlString.indexOf(DRIVER_NAME);
            
            if (index < 0)
            {
                throw new HADBSetupException("hadbmgmt-res.MalformedJdbcUrl",
                    jdbcUrlString);
            }
            // Strip everything up to the driver name
            jdbcURL = jdbcUrlString.substring(index).trim();
            LoggerHelper.fine("jdbcURL is " + jdbcURL);
        }
        
        //Trim off the driver name if necessary.
        String result = jdbcURL;
        if (stripDriverName)
        {
            result = jdbcURL.substring(DRIVER_NAME.length());
        }
        LoggerHelper.fine("getJdbcURL " + stripDriverName + " returns " + result);
        return result;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String getNodeList() throws HADBSetupException
    {
        // here is the output from hadbm:
        // hadbm status --nodes c1
        // NodeNo HostName   Port  NodeRole NodeState MirrorNode
        // 0      iasengsol6 17200 active   running   1
        // 1      iasengsol6 17220 active   running   0
        
        if(HADBUtils.noHADB())
        {
            return "Phony HADB";
        }
        
        String[] commands = getNodeListCommands();
        HADBMExecutor exec = new HADBMExecutor(getExecutable(), commands);
        int exitValue = exec.exec();
        
        if(exitValue != 0)
        {
            String out = exec.getStdout();
            String err = exec.getStderr();
            String msg = StringHelper.get("hadbmgmt-res.getNodeListFailed",
                new Object[]{"" + exitValue, exec.getStdout(), exec.getStderr()} );
            
            if(exec.isHadbmError(22005))
            {
                msg = StringHelper.get("hadbmgmt-res.AuthError") + " -- " + msg;
            }
            throw new HADBSetupException(msg);
        }
        
        LoggerHelper.finer("***** <STDOUT> hadbm listdomain: " + exec.getStdout());
        LoggerHelper.finer("***** <STDERR> hadbm listdomain: " + exec.getStderr());
        
        return exec.getStdout();
        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final String getPackagesList() throws HADBSetupException
    {
        // here is the output from hadbm:
        // ./hadbm listdomain
        // Hostname   Enabled? Running? Release  Interfaces
        // iasengsol6 Yes      Yes      V4-4-1-7 10.5.79.47
        
        if(HADBUtils.noHADB())
        {
            return "Phony HADB";
        }
        
        String[] commands = getListPackagesCommands();
        HADBMExecutor exec = new HADBMExecutor(getExecutable(), commands);
        //int exitValue = exec.exec(new File("C:/bnbin/spitargs.exe"), commands);
        int exitValue = exec.exec();
        
        if(exitValue != 0)
        {
            throw new HADBSetupException("hadbmgmt-res.getPackagesListFailed",
                new Object[]
            {"" + exitValue, exec.getStdout(), exec.getStderr()} );
        }
        
        LoggerHelper.finer("***** <STDOUT> hadbm listdomain: " + exec.getStdout());
        LoggerHelper.finer("***** <STDERR> hadbm listdomain: " + exec.getStderr());
        
        return exec.getStdout();
        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final MBeanServer getMBeanServer() throws HADBSetupException
    {
        if(mbeanServer == null)
            throw new HADBSetupException("hadbmgmt-res.InternalError", "null mbeanServer");
        
        return mbeanServer;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final ConfigContext getConfigContext()
    {
        return configCtx;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final void setDBPreExists()
    {
        dbPreExists = true;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final boolean getDBPreExists()
    {
        return dbPreExists;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * this may return null in some cases...
     */
    final Config getConfigForCluster() throws HADBSetupException
    {
        try
        {
            return ClusterHelper.getConfigForCluster(getConfigContext(), getClusterName());
        }
        catch(ConfigException ce)
        {
            if(clusterConfigRequired())
                throw new HADBSetupException("hadbmgmt-res.BadConfigContext", ce, getClusterName());
            else
                return null;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    boolean clusterConfigRequired()
    {
        return true;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    boolean areHostsRequired()
    {
        return hostsRequired;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    String getJavaRoot()
    {
        return System.getProperty(SystemPropertyConstants.JAVA_ROOT_PROPERTY);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    String getJavaRootArg()
    {
        return "--javahome=" + getJavaRoot();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    void addMsg(String s)
    {
        msgs.add(s);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    Object[] prepMsgs()
    {
        if(msgs.size() <= 0)
            return null;
        
        if(! msgs.get(msgs.size() - 1).toString().equals("\n"))
            msgs.add("\n");
        
        return msgs.toArray();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    final String getAgentURL(String host) throws HADBSetupException
    {
        if(host != null)
        {
            return host + ":" + agentPort;
        }
        else
        {
            if(agentURL == null)
                agentURL = getHosts() + ":" + agentPort;
            
            return agentURL;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    final void verifyStandaloneCluster() throws HADBSetupException
    {
        if(clusterConfigRequired() == false && getConfigForCluster() == null)
        {
            // no need to see if it's stand-alone -- it doesn't exist!!
            return;
        }
        
        // we do NOT support HADB on Clusters that share configuration.
        
        try
        {
            if(ClusterHelper.isClusterStandAlone(getConfigContext(), getClusterName()))
                return;
        }
        catch(Exception e)
        {
            throw new HADBSetupException(e);
        }
        
        throw new HADBSetupException("hadbmgmt-res.SharedClusterConfig");
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final void setHadbRoot() throws HADBSetupException
    {
        String path = System.getProperty(SystemPropertyConstants.HADB_ROOT_PROPERTY);
        
        if(!ok(path))
            throw new HADBSetupException("hadbmgmt-res.NoHadbRootEnv", SystemPropertyConstants.HADB_ROOT_PROPERTY);
        
        File f = new File(path);
        
        if(!FileUtils.safeIsDirectory(f))
            throw new HADBSetupException("hadbmgmt-res.NoHadbRootPath", new Object[] {SystemPropertyConstants.HADB_ROOT_PROPERTY, path} );
        
        //if(!f.canWrite())
        //throw new HADBSetupException("hadbmgmt-res.NoHadbRootWrite", new Object[] {SystemPropertyConstants.HADB_ROOT_PROPERTY, path} );
        
        // whew!
        
        hadbRoot = f;
    }
    
    final int getAgentPort() throws HADBSetupException
    {
        assertSetup();
        return agentPort;
    }
    /**
     * @return Returns true if the hadbm executable can be located.
     **/
    
    static public Object[] isHadbInstalled()
    {
        // warning: this code is almost all error-message processing!!
        Object[] ret = new Object[2];
        
        String path = System.getProperty(SystemPropertyConstants.HADB_ROOT_PROPERTY);
        
        if(!ok(path))
        {
            String reason = StringHelper.get("hadbmgmt-res.NoHADB.NoSysProp", SystemPropertyConstants.HADB_ROOT_PROPERTY);
            ret[0] = "false";
            ret[1] = StringHelper.get("hadbmgmt-res.NoHADB", reason);
            return ret;
        }
        
        File hadbroot = new File(path);
        
        if(!FileUtils.safeIsDirectory(hadbroot))
        {
            String reason = StringHelper.get("hadbmgmt-res.NoHADB.DirNotExist", hadbroot);
            ret[0] = "false";
            ret[1] = StringHelper.get("hadbmgmt-res.NoHADB", reason);
            return ret;
        }
        
        File hadbmdir = new File(hadbroot, "bin");
        
        if(!FileUtils.safeIsDirectory(hadbmdir))
        {
            String reason = StringHelper.get("hadbmgmt-res.NoHADB.BinDirNotExist", hadbmdir);
            ret[0] = "false";
            ret[1] = StringHelper.get("hadbmgmt-res.NoHADB", reason);
            return ret;
        }
        
        // in order to make this code OS independent - we search for a file that
        // begins with "hadbm".  On Windows it is "hadbm.exe", on UNIX it is "hadbm"
        File[] files = hadbmdir.listFiles
            (
            new FileFilter()
        {
            public boolean accept(File f)
            {
                return f.getName().startsWith("hadbm");
            }
        }
        );
        
        if(files == null || files.length <= 0)
        {
            String reason = StringHelper.get("hadbmgmt-res.NoHADB.HadbmNotExist", hadbmdir);
            ret[0] = "false";
            ret[1] = StringHelper.get("hadbmgmt-res.NoHADB", reason);
            return ret;
        }
        
        ret[0] = "true";
        ret[1] = StringHelper.get("hadbmgmt-res.YesHADB");
        return ret;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final Boolean getAutoHadb() throws HADBSetupException
    {
        assertSetup();
        return autoHadb;
    }
    /**
     * throw an Exception if setup was never called...
     */
    final void assertSetup() throws HADBSetupException
    {
        if(!wasSetup)
            throw new HADBSetupException("hadbmgmt-res.InternalError", StringHelper.get("hadbmgmt-res.NoSetup"));
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    HADBVersion getVersion()
    {
        if(version == null)
            setVersion();
        
        return version;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    //////  Private Methods
    ////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    private final void setHosts() throws HADBSetupException
    {
        // note -- if there is no cluster-config AND if they didn't enter valid hosts
        // we must return an error...
        
        if(!ok(hosts))
        {
            try
            {
                HADBResourceManager rm = new HADBResourceManager(this);
                hosts = rm.getHostsFromConfig();
                // if hosts is kosher -- all is well, return...
                if(ok(hosts))
                    return;
            }
            catch(Exception e)
            {
                // fall through
            }
            // June 2005 -- they may be calling isHA() from the MBean in which case we definitely
            // do NOT want to throw an Exception!!
            
            if(!areHostsRequired())
            {
                hosts = null;
                return;
            }
            // ERROR -- we have a custom message for the case of remove-ha-cluster
            // where the cluster is already gone and they didn't specify hosts...
            if(clusterConfigRequired())
                throw new HADBSetupException("hadbmgmt-res.NoHosts");
            else
                throw new HADBSetupException("hadbmgmt-res.NoHostsOnRemove");
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    private final void checkHosts() throws HADBSetupException
    {
        // no harm, no foul
        if(hosts == null && !areHostsRequired())
            return;
        
        parseHosts();
        
        hosts = "";
        boolean first = true;
        
        for(String host : hostsArray)
        {
            if(first)
                first = false;
            else
                hosts += ",";
            
            hosts += host;
        }
    }
    
    /**
     * example:
     * input: "iasengsol6,iasengsol6"
     * output: ArrayList: "10.5.79.47", "10.5.79.47"
     **/
    private final void parseHosts() throws HADBSetupException
    {
        if(!ok(hosts))
            throw new HADBSetupException("hadbmgmt-res.InternalError", "checkHosts called with no hosts set.  This is supposed to be impossible.");
        
        // must be an even number of hosts >= 2
        // note -- there may be empty strings in ha -- e.g. --hosts "a,,,,c"
        
        String[] ha = hosts.split(",");
        List<String> hl = new ArrayList<String>();
        
        for(String h : ha)
        {
            if(ok(h))
                hl.add(h);
        }
        
        int num = hl.size();
        
        if(num < 2 || ((num % 2) != 0) )
            throw new HADBSetupException("hadbmgmt-res.BadNumberOfHosts", new Object[] { "" + num, hosts} );
        
        hostsArray = new String[hl.size()];
        int index = 0;
        
        for(String host : hl)
        {
            // bnevins 10-26-2004
            // ip address work-around, bug#6184878
            if(HADBUtils.useIP())
                host = HADBUtils.getIP(host);
            
            hostsArray[index++] = host;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    private final void setAgentPort() throws HADBSetupException
    {
        // 6287592 -- October 2005
        // if they enter a "bad" port number it is now a fatal error
        // If they didn't enter a port number then do steps 2 and 3
        
        // Use in order of preference:
        // 1) a valid number they specified
        // 2) the agent port property in Config
        // 3) the default port
        //
        
        if(agentPortString != null)
        {
            try
            {
                agentPort = Integer.parseInt(agentPortString);
                
                if(agentPort > 1024 && agentPort < 65536)
                    return; // case (1)
            }
            catch(Exception e)
            {
                // fall through
            }
            //either an Exception was thrown or the number was "bad"...
            throw new HADBSetupException("hadbmgmt-res.BadAgentPort", agentPortString);
        }
        
        // try case (2)
        HADBResourceManager rm = new HADBResourceManager(this);
        agentPortString = rm.getAgentPortFromConfig();
        
        try
        {
            agentPort = Integer.parseInt(agentPortString);
            
            if(agentPort > 0 && agentPort < 65536)
                return;
        }
        catch(Exception e)
        {
        }
        
        // case (3)
        LoggerHelper.warning("hadbmgmt-res.NoAgentPort", "" + DEFAULT_AGENT_PORT);
        agentPort = DEFAULT_AGENT_PORT;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final void setAutoHadb(Boolean what)
    {
        autoHadb = what;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    final void setAutoHadb() throws HADBSetupException
    {
        // Use in order of preference:
        // 1) a Boolean they specified
        // 2) the auto-hadb property in Config
        // 3) the default
        
        // 1
        
        if(autoHadb != null)
            return;
        
        // 2
        HADBResourceManager rm = new HADBResourceManager(this);
        autoHadb = rm.getAutoHadbFromConfig();
        
        if(autoHadb != null)
            return;
        
        // 3
        autoHadb = new Boolean(DEFAULT_AUTO_HADB);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private final String[] toArray(List list)
    {
        String[] ss = new String[list.size()];
        return (String[])list.toArray(ss);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private final String propAttributesToStringAttributes(Properties props)
    {
        // do NOT leave explicit double-quotes in there -- hadbm won't take them!!
        // #1) Format: e.g. "foo=goo goo=ioo"
        // #2 or:  "foo=goo","hoo=ioo"
        // we are doing #2
        
        Set<Map.Entry<Object,Object>>   propSet     = props.entrySet();
        StringBuilder                   sb          = new StringBuilder();
        boolean                         firstEntry  = true;
        
        for(Map.Entry<Object,Object> entry : propSet)
        {
            // add a preceding comma for all but the first
            if(firstEntry)
                firstEntry = false;
            else
                sb.append(',');
            
            String key = (String)entry.getKey();
            String val = (String)entry.getValue();
            
            sb.append(key).append("=").append(val);
        }
        
        return sb.toString();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private boolean isSpecialUser(String user)
    {
        if(user == null)
            return false;
        
        return user.equals("public") || user.equals("guest") || user.equals("system");
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private String makeNonSpecialName(String user)
    {
        // At first I added random letters -- but we have to ALWAYS create the
        // exact same made-up name.  Otherwise create-ha-store would have problems
        return "as" + user;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private final void setVersion()
    {
        version = new HADBVersion(this);
    }
    ///////////////////////////////////////////////////////////////////////////
    
    private static boolean ok(String s)
    {
        // it is used so much in this class that this is here as a readability
        // improvement...
        
        return StringUtils.ok(s);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private                 String          hosts;
    private                 String[]        hostsArray;
    private                 int             agentPort;
    private                 String          agentURL;
    private                 String          clusterName;
    private                 String          jdbcURL;
    private                 String[]        args;
    private                 String          systemUser;
    private                 String          systemPassword;
    private                 String          adminPassword;
    private                 String          dasPassword;
    private                 PasswordManager adminPasswordManager;
    private                 PasswordManager dbPasswordManager;
    private                 String          dbUser;
    private                 String          dbPassword;
    private                 ConfigContext   configCtx;
    //private                   Config          clusterConfig;
    private                 File            executable;
    private                 MBeanServer     mbeanServer;
    private                 File            hadbRoot;
    private                 boolean         dbPreExists             = false;
    private                 String          agentPortString;
    private                 List            msgs                    = new ArrayList();
    private                 String          userPasswordFile;
    private                 boolean         wasSetup                = false;
    private                 Boolean         autoHadb;
    private                 boolean         hostsRequired           = true;
    private                 HADBVersion     version;
    private static final    String          DRIVER_NAME             = "jdbc:sun:hadb:";
    private static final    int             DEFAULT_AGENT_PORT      = 1862;
    private static final    boolean         DEFAULT_AUTO_HADB       = false;
    
}
