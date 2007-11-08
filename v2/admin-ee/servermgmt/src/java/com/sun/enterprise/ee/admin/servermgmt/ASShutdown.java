/*
 * ASShutdown.java
 *
 * Created on May 23, 2007, 12:08 PM
 *
 */

package com.sun.enterprise.ee.admin.servermgmt;

import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.servermgmt.launch.ASLauncherException;
import com.sun.enterprise.admin.servermgmt.pe.PEInstancesManager;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.ee.admin.servermgmt.EEInstancesManager;
import java.io.*;

/**
 * These environmental variables must be set
 * INSTANCE_ROOT, SERVER_NAME, AS_INSTALL, AS_CONFIG
 *
 *
 * @author bnevins
 */

public class ASShutdown
{
    public void shutdown() throws ASLauncherException
    {
        setup();
        
        try 
        {
            switch(type)
            {
                case DAS:
                    new PEInstancesManager(config).stopInstance();
                    break;
                case NA:    
                    new AgentManager((AgentConfig)config).stopInstance();
                    break;
                case INSTANCE:
                    new EEInstancesManager(config).stopInstance();
                    break;
            }
        }
        catch(Exception e)
        {
            throw new ASLauncherException("Error trying to stop server.", e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    
    public static void main(String[] args)
    {
        try
        {
            ASShutdown shutdown = new ASShutdown();
            shutdown.shutdown();
            System.out.println("Server was successfully stopped.");
        } 
        catch (ASLauncherException ex)
        {
            ex.printStackTrace();
        }
        
    }
    
    ///////////////////////////////////////////////////////////////////////////
    ///////////   Everything Below is private    //////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    private enum ServerType { DAS, NA, INSTANCE };
    
    private void setup() throws ASLauncherException
    {
        // these 2 are used by RepositoryConfig
        setSystemProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY,    ENV_INSTALL_ROOT);
        setSystemProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY,     ENV_CONFIG_ROOT);
        
        setServerDir();
        setType();
        createConfig();
    }
    
    ///////////////////////////////////////////////////////////////////////////

    private void createConfig() throws ASLauncherException
    {
        // 3 kinds of servers == 3 kinds of Config calls
        
        try // don't trust "outsiders" like RepositoryConfig
        {
            File parent = serverDir.getParentFile();
            
            switch(type)
            {
                case DAS:
                    // e.g. /as/domains/domain1 --> repName=domain1, repRoot=/as/domains
                config = new RepositoryConfig(serverDir.getName(), parent.getPath());
                break;
                
                case INSTANCE:
                config = new RepositoryConfig(serverDir.getAbsolutePath());
                break;

                case NA:
                    // e.g. /as/nodeagents/na1/agent --> repName=na1, 
                    // repRoot=/as/nodeagents and instanceName=agent
                config = new AgentConfig(
                            parent.getName(), 
                            parent.getParent(),
                            serverDir.getName());
                break;
            }
        }
        catch(Exception e)
        {
            throw new ASLauncherException("Error attempting to create Repository Config", e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private void setServerDir() throws ASLauncherException
    {
        serverDir = new File(getEnvironmentVariable(ENV_INSTANCE_ROOT));
        
        if(!serverDir.isDirectory())
        {
            throw new ASLauncherException("Server root directory, " + serverDir + ", doesn't exist.");
        }
        
        // JDK bug with getAbsoluteFile ????
        try
        {
            serverDir = new File(serverDir.getCanonicalPath());
        } 
        catch (IOException ex)
        {
            serverDir = new File(serverDir.getAbsolutePath());
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private void setType() throws ASLauncherException
    {
        // this is painful.  
        // There is one and only server that has the name "server"
        // and that is DAS.  That's how we figure out if it is DAS.
        // We need to know because we have to use a different  RepositoryConfig
        // constructor for DAS
        //
        // We also need to know if it's a NodeAgent.  NodeAgent uses AgentConfig --
        // a subclass of RepositoryConfig and also uses EEInstanceManager.  
        // This is overly-complex but entrenched in the AS codebase...
        // We find out if it's NA by looking at this:
        // PROCESS_NAME="s1as8-nodeagent"

        String name = getEnvironmentVariable(ENV_SERVER_NAME);
        String proc = System.getenv(ENV_PROCESS_NAME);
        
        if(name.equals(ID_DAS))
        {
            type = ServerType.DAS;
            return;
        }
        if(ok(proc) && proc.indexOf(ID_NA) >= 0)
        {
            type = ServerType.NA;
            return;
        }
        
        type = ServerType.INSTANCE;
    }
    
    ///////////////////////////////////////////////////////////////////////////

    private void setSystemProperty(String sysProp, String envProp) throws ASLauncherException
    {
        System.setProperty(sysProp, getEnvironmentVariable(envProp));
    }

    ///////////////////////////////////////////////////////////////////////////

    private String getEnvironmentVariable(String envProp) throws ASLauncherException
    {
        // -D property takes precedence over Env. Var.
        
        String envValue = System.getProperty(envProp);
        
        if(!ok(envValue))
        {
            // check the OS environment
            envValue = System.getenv(envProp);

            if(!ok(envValue))
            {
                throw new ASLauncherException("The environmental variable or System Property, " + envProp + ", must be set.");
            }
        }
        
        return envValue;
    }
    
    ///////////////////////////////////////////////////////////////////////////

    private boolean ok(String s)
    {
        return s != null && s.length() > 0;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    private File                serverDir;
    private RepositoryConfig    config;
    private ServerType          type;
    private final static String ENV_INSTALL_ROOT    = "AS_INSTALL";
    private final static String ENV_CONFIG_ROOT     = "AS_CONFIG";
    private final static String ENV_INSTANCE_ROOT   = "INSTANCE_ROOT";
    private final static String ENV_SERVER_NAME     = "SERVER_NAME";
    private final static String ENV_PROCESS_NAME    = "PROCESS_NAME";
    private final static String ID_DAS              = "server";
    private final static String ID_NA               = "nodeagent";
}
