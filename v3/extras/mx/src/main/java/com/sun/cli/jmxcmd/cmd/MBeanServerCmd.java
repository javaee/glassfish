/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/MBeanServerCmd.java,v 1.32 2005/05/20 00:42:45 llc Exp $
 * $Revision: 1.32 $
 * $Date: 2005/05/20 00:42:45 $
 */
package com.sun.cli.jmxcmd.cmd;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;

import java.util.HashMap;
import java.util.Map;

import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.Attribute;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanRegistrationException;
import javax.management.ReflectionException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.NotCompliantMBeanException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;


import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorServer;
//import javax.management.remote.generic.GenericConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.rmi.RMIConnectorServer;



import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.IllegalUsageException;
import com.sun.cli.jcmd.framework.ClassSource;
import com.sun.cli.jcmd.framework.CmdException;
import com.sun.cli.jcmd.framework.ClassSourceFromStrings;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.DisallowedOptionDependency;
import com.sun.cli.jcmd.util.cmd.RequiredOptionDependency;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.misc.StringifiedList;

import com.sun.cli.jmxcmd.security.TLSSetup;
import com.sun.cli.jmxcmd.security.rmi.RMISSLServerSocketFactory;
import com.sun.cli.jmxcmd.security.rmi.RMISSLClientSocketFactory;



import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;

import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

import com.sun.cli.jmxcmd.security.sasl.SaslServerSetup;
import java.util.List;

/**
Manages in-process mbean servers.
 */
public class MBeanServerCmd extends JMXCmd
{

    public MBeanServerCmd(final CmdEnv env)
    {
        super(env);
    }

    static final class MBeanServerCmdHelp extends CmdHelpImpl
    {

        public MBeanServerCmdHelp()
        {
            super(getCmdInfos());
        }
        private final static String NAME = MBeanServerCmd.CMD_NAME;
        private final static String SYNOPSIS = "manage in-process MBeanServers";
        private final static String HELP_TEXT =
                "Start, stop or list MBeanServers.\n\n" +
                "Example of creating and connecting to an in-process MBeanServer:\n" +
                "    start-mbs mbs1\n" +
                "    connect --protocol=inprocess --options=mbeanserver=mbs1\n" +
                "";


        public String getName()
        {
            return (NAME);
        }


        public String getSynopsis()
        {
            return (formSynopsis(SYNOPSIS));
        }


        public String getText()
        {
            return (HELP_TEXT);
        }
    }
    private final static OptionInfo INIT_OPTION = new OptionInfoImpl("init", "i");
    private final static OptionInfo MBEANS_OPTION = new OptionInfoImpl("mbeans", "m");
    private final static OptionInfo PORT_OPTION = new OptionInfoImpl("port", "p", PORT_NUMBER_ARG);
    private final static OptionInfo RMI_REGISTRY_PORT_OPTION =
            new OptionInfoImpl("rmi-registry-port", "R", PORT_NUMBER_ARG);
    private final static OptionInfo SASL_OPTION = createSaslOption();
    private final static OptionInfo KEYSTORE_FILE_OPTION =
            new OptionInfoImpl("keystore-file", "k", PATH_ARG, false);
    private final static OptionInfo ACL_FILE_OPTION =
            new OptionInfoImpl("acl-file", "a", PATH_ARG, false);
    private final static OptionInfo AUTH_FILE_OPTION =
            new OptionInfoImpl("auth-file", "u", PATH_ARG, false);
    private final static OptionInfo PASSWORD_FILE_OPTION = createPasswordFileOption();
    private final static OptionInfo PROMPT_OPTION = createPromptOption();
    static private final OptionInfo[] START_OPTIONS_INFOS =
    {
        INIT_OPTION,
        MBEANS_OPTION,
    };
    static private final OptionInfo[] LOAD_JMXMP_CONNECTOR_INFOS =
    {
        PORT_OPTION,
        KEYSTORE_FILE_OPTION,
        PASSWORD_FILE_OPTION,
        PROMPT_OPTION,
        ACL_FILE_OPTION,
        AUTH_FILE_OPTION,
        SASL_OPTION
    };
    static private final OptionInfo[] LOAD_HTML_ADAPTER_INFOS =
    {
        PORT_OPTION,
    };
    static private final OptionInfo[] LOAD_RMI_CONNECTOR_INFOS =
    {
        RMI_REGISTRY_PORT_OPTION,
        KEYSTORE_FILE_OPTION,
        PASSWORD_FILE_OPTION,
        PROMPT_OPTION,
        ACL_FILE_OPTION,
    };


    static
    {
        PROMPT_OPTION.addDependency(new DisallowedOptionDependency(PASSWORD_FILE_OPTION));

        final RequiredOptionDependency keystoreRequired =
                new RequiredOptionDependency(KEYSTORE_FILE_OPTION);
        PROMPT_OPTION.addDependency(keystoreRequired);
        PASSWORD_FILE_OPTION.addDependency(keystoreRequired);

        AUTH_FILE_OPTION.addDependency(new RequiredOptionDependency(ACL_FILE_OPTION));
    }
    public final static String CMD_NAME = "mbean-server";
    public final static String START_NAME = "start-" + CMD_NAME;
    public final static String STOP_NAME = "stop-" + CMD_NAME;
    public final static String LIST_NAME = "list-" + CMD_NAME + "s";
    public final static String LOAD_JMXMP_CONNECTOR_NAME = "load-jmxmp-connector";
    public final static String LOAD_RMI_CONNECTOR_NAME = "load-rmi-connector";
    public final static String LOAD_HTML_ADAPTER_NAME = "load-html-adapter";
    private final static CmdInfo START_INFO =
            new CmdInfoImpl(START_NAME,
            new OptionsInfoImpl(START_OPTIONS_INFOS),
            new OperandsInfoImpl("<mbeanserver-name>", 1, 1));
    private final static CmdInfo STOP_INFO =
            new CmdInfoImpl(STOP_NAME,
            new OperandsInfoImpl("<mbeanserver-name>", 1, 1));
    private final static CmdInfo LIST_INFO =
            new CmdInfoImpl(LIST_NAME, OperandsInfoImpl.NONE);
    private final static CmdInfo LOAD_JMXMP_INFO =
            new CmdInfoImpl(LOAD_JMXMP_CONNECTOR_NAME,
            new OptionsInfoImpl(LOAD_JMXMP_CONNECTOR_INFOS),
            new OperandsInfoImpl("[object-name]", 0, 1));
    private final static CmdInfo LOAD_RMI_INFO =
            new CmdInfoImpl(LOAD_RMI_CONNECTOR_NAME,
            new OptionsInfoImpl(LOAD_RMI_CONNECTOR_INFOS),
            new OperandsInfoImpl("[object-name]", 0, 1));
    private final static CmdInfo LOAD_HTML_INFO =
            new CmdInfoImpl(LOAD_HTML_ADAPTER_NAME,
            new OptionsInfoImpl(LOAD_HTML_ADAPTER_INFOS),
            new OperandsInfoImpl("[object-name]", 0, 1));


    public static CmdInfos getCmdInfos()
    {
        return (new CmdInfos(new CmdInfo[]
                {
                    START_INFO,
                    STOP_INFO,
                    LIST_INFO,
                    LOAD_JMXMP_INFO,
                    LOAD_RMI_INFO,
                    LOAD_HTML_INFO
                }));
    }


    public CmdHelp getHelp()
    {
        return (new MBeanServerCmdHelp());
    }


    private void initMBeanServerAndClasses(
            final String serverName,
            final MBeanServer server,
            final List<Class<? extends MBeanServerIniter>> initerClasses)
            throws IllegalAccessException, ClassNotFoundException, InstantiationException,
            Exception
    {
        for (int i = 0; i < initerClasses.size(); ++i)
        {
            final MBeanServerIniter initer = initerClasses.get(i).newInstance();

            initer.initMBeanServer(serverName, server);
            println(serverName + ": called initer: " + initer.getClass().getName());
        }
    }


    private void doStart(String name, String[] initerClassnames, String[] mbeanClassnames)
            throws Exception
    {
        if (findMBeanServer(name) == null)
        {
            final MBeanServer server = MBeanServerFactory.createMBeanServer();

            addMBeanServer(name, server);

            println("MBean server " + name + " started.");

            final List<Class<? extends MBeanServerIniter>> classes = new ClassSourceFromStrings<MBeanServerIniter>(initerClassnames, false).getClasses();
            initMBeanServerAndClasses(name, server, classes);

            final ClassSource<Object> mbeansClassSource = new ClassSourceFromStrings<Object>(mbeanClassnames, false);
            new MBeansIniter(mbeansClassSource.getClasses()).initMBeanServer(name, server);
        }
        else
        {
            println("MBeanServer " + name + " already running");
        }
    }

    private class MBeansIniter implements MBeanServerIniter
    {

        final List<Class<?>> mClasses;


        MBeansIniter(List<Class<?>> classnames)
        {
            mClasses = classnames;
        }


        public void initMBeanServer(
                final String serverName,
                final MBeanServer server) throws Exception
        {
            for (int i = 0; i < mClasses.size(); ++i)
            {
                final Object mbean = mClasses.get(i).newInstance();

                // create a unique object name (Object can change it in preRegister).
                ObjectName objectName = new ObjectName(
                        ":server=" + serverName +
                        ",classname=" + mClasses.get(i).getName() +
                        ",id=" + i);

                objectName = server.registerMBean(mbean, objectName).getObjectName();
                println(serverName + ": registered MBean: " + objectName.toString());
            }
        }
    }


    private void doStop(String name)
    {
        final MBeanServer server = findMBeanServer(name);

        if (server != null)
        {
            removeMBeanServer(name);
            println("MBean server " + name + " stopped.");
        }
        else
        {
            println("MBeanServer " + name + " does not exist");
        }
    }


    private void doList()
    {
        for( final String name : getMBeanServerNames() )
        {
            println( name );
        }
    }


    private JMXConnectorServer startConnectorServer(
            MBeanServer server,
            final int port,
            final ObjectName objectName,
            Map<String, Object> env)
            throws MalformedURLException, IOException,
            InstanceAlreadyExistsException, InstanceNotFoundException, MalformedObjectNameException,
            MBeanRegistrationException, NotCompliantMBeanException
    {
        final JMXServiceURL url = new JMXServiceURL("jmxmp", null, port);

        final JMXConnectorServer connectorServer = (JMXConnectorServer) JMXConnectorServerFactory.newJMXConnectorServer(url, env, server);
        server.registerMBean(connectorServer, objectName);

        connectorServer.start();

        return (connectorServer);
    }


    private void loadSecureJMXMPConnector(
            int connectorPort,
            ObjectName objectName,
            String saslAlgorithm,
            File keyStoreFile,
            String keyStorePassword,
            String keyPassword,
            File authenticationFile,
            File authorizationFile)
            throws Exception
    {
        final MBeanServer server = getMBeanServer();
        final Map<String, Object> env = new HashMap<String, Object>();
        final boolean useTLS = keyStoreFile != null;

        final SaslServerSetup setup = new SaslServerSetup(env, useTLS);

        if (useTLS)
        {
            // Initialize the SSLSocketFactory
            TLSSetup.setupTLSForJMXMP(env, keyStoreFile, keyStorePassword, keyPassword);
        }

        setup.setupProfiles(saslAlgorithm);
        setup.setupAuthorization(authorizationFile);
        setup.setupAuthentication(authenticationFile);
        setup.setupSensible();

        final JMXConnectorServer connectorServer =
                startConnectorServer(server, connectorPort, objectName, env);

        println("Loaded and started secure connector: " + objectName);
    }
    private static Registry sRMIRegistry = null;
    private static int sRMIRegistryPort = 0;


    private static void createRMIRegistry(final int port) throws RemoteException
    {
        if (sRMIRegistry == null)
        {
            sRMIRegistry = LocateRegistry.createRegistry(port);
            sRMIRegistryPort = port;
        }
    }


    private static int getRMIRegistryPort()
            throws RemoteException
    {
        return (sRMIRegistryPort);
    }


    private void loadHTMLAdapter(int adapterPort, String optionalName)
            throws MalformedURLException, IOException,
            MBeanRegistrationException, MBeanException, ReflectionException,
            InstanceAlreadyExistsException, InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException
    {
        final MBeanServerConnection conn = getConnection();

        try
        {
            final ObjectName objectName = optionalName != null ? new ObjectName(optionalName) : new ObjectName("adapter:name=html-adapter,protocol=html,type=adapter,port=" + adapterPort);

            conn.createMBean("com.sun.jdmk.comm.HtmlAdaptorServer",
                    objectName,
                    null,
                    null,
                    null);

            final Integer port = new Integer(adapterPort);
            conn.setAttribute(objectName, new Attribute("Port", port));

            conn.invoke(objectName, "start", null, null);

            println("Loaded and started html adapter: " + objectName);
        }
        catch (MalformedObjectNameException e)
        {
            assert (false);
        }
        catch (NotCompliantMBeanException e)
        {
            assert (false);
        }
    }


    private boolean saslRequiresAclFile(String sasl)
    {
        final boolean userPasswordPairsRequired = sasl != null &&
                (sasl.equals("PLAIN") || sasl.equals("DIGEST-MD5"));

        return (userPasswordPairsRequired);
    }

    private static final class SecurityOptions
    {

        String sasl;
        String keyStoreFile;
        String aclFile;
        String authFile;
        String passwordFile;
        boolean prompt;


        public SecurityOptions()
        {
            keyStoreFile = null;
            aclFile = null;
            authFile = null;
            passwordFile = null;
            prompt = false;
            sasl = null;
        }
    }


    private SecurityOptions getJMXMPSecurityOptions()
            throws IllegalOptionException
    {
        final SecurityOptions options = new SecurityOptions();
        options.keyStoreFile = getString(KEYSTORE_FILE_OPTION.getShortName(), null);
        options.aclFile = getString(ACL_FILE_OPTION.getShortName(), null);
        options.authFile = getString(AUTH_FILE_OPTION.getShortName(), null);
        options.passwordFile = getString(PASSWORD_FILE_OPTION.getShortName(), null);
        options.prompt = getBoolean(PROMPT_OPTION.getShortName(), Boolean.FALSE).booleanValue();
        options.sasl = getString(SASL_OPTION.getShortName(), null);

        return (options);
    }


    private SecurityOptions getRMISecurityOptions()
            throws IllegalOptionException
    {
        final SecurityOptions options = new SecurityOptions();
        options.keyStoreFile = getString(KEYSTORE_FILE_OPTION.getShortName(), null);
        options.aclFile = getString(ACL_FILE_OPTION.getShortName(), null);
        options.passwordFile = getString(PASSWORD_FILE_OPTION.getShortName(), null);
        options.prompt = getBoolean(PROMPT_OPTION.getShortName(), Boolean.FALSE).booleanValue();

        return (options);
    }


    private void handleLoadJMXMP()
            throws Exception
    {
        final String cmd = getSubCmdNameAsInvoked();
        final String[] operands = getOperands();
        final String firstOperand = operands.length != 0 ? operands[ 0] : null;

        final Integer defaultPort = new Integer(54300);
        final SecurityOptions options = getJMXMPSecurityOptions();

        final int port = getInteger(PORT_OPTION.getShortName(), defaultPort).intValue();

        final ObjectName objectName = firstOperand != null ? new ObjectName(firstOperand) : new ObjectName("connector:name=jmxmp-connector-server,type=connector,port=" + port);

        if (saslRequiresAclFile(options.sasl) && options.aclFile == null)
        {
            throw new CmdException(cmd, "Use of SASL requires " + ACL_FILE_OPTION.getLongName());
        }

        if (options.keyStoreFile == null && options.aclFile == null &&
                options.authFile == null && options.sasl == null)
        {
            establishProxy();
            startConnectorServer(getMBeanServer(), port, objectName, null);
            //loadJMXMPConnector( port, objectName );
            println("WARNING: no security is being applied to jmxmp connector");
        }
        else
        {
            if (options.keyStoreFile != null)
            {
                final String keyStorePassword = getPasswordForUser("keystore", options.passwordFile, options.prompt);
                final String keyPassword = getPasswordForUser("key", options.passwordFile, options.prompt);

                loadSecureJMXMPConnector(port, objectName, options.sasl,
                        new File(options.keyStoreFile),
                        keyStorePassword, keyPassword, new File(options.aclFile),
                        options.authFile == null ? null : new File(options.authFile));
            }
            else
            {
                loadSecureJMXMPConnector(port, objectName, options.sasl, null, null, null,
                        options.aclFile == null ? null : new File(options.aclFile),
                        options.authFile == null ? null : new File(options.authFile));
            }
        }
    }


    private char[] toCharArray(final String s)
    {
        return (s == null ? null : s.toCharArray());
    }


    private void initEnvForRMI(
            final Map<String, Object> env,
            final SecurityOptions options)
            throws CmdException, IOException
    {
        if (options.keyStoreFile != null)
        {
            final File keystoreFile = new File(options.keyStoreFile);
            if (!keystoreFile.exists())
            {
                throw new FileNotFoundException("Keystore not found: " + quote(keystoreFile.toString()));
            }

            final char[] keyStorePassword =
                    toCharArray(getPasswordForUser("keystore", options.passwordFile, options.prompt));
            char[] keyPassword = keyStorePassword;
            RMISSLServerSocketFactory serverFactory = null;

            // if no password file, see if keystore password works for key
            if (options.passwordFile == null)
            {
                try
                {
                    serverFactory =
                            new RMISSLServerSocketFactory(keystoreFile, keyStorePassword, keyPassword);
                }
                catch (Exception e)
                {
                    keyPassword = toCharArray(getPasswordForUser("key", options.passwordFile, options.prompt));
                }
            }

            if (serverFactory == null)
            {
                serverFactory = new RMISSLServerSocketFactory(keystoreFile, keyStorePassword, keyPassword);
            }

            final RMISSLClientSocketFactory clientFactory =
                    new RMISSLClientSocketFactory();

            env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, clientFactory);
            env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, serverFactory);

            if (options.aclFile != null)
            {
                env.put("jmx.remote.x.password.file", options.aclFile);
            }
        }
    }


    private void loadRMIConnector(
            String protocol, // rmi or rmi-iiop
            int rmiRegistryPort,
            String optionalName,
            SecurityOptions options)
            throws IOException, MalformedURLException,
            MalformedObjectNameException, CmdException, InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException,
            InstanceNotFoundException, RemoteException
    {
        final MBeanServer server = findMBeanServer(getDefaultConnectionName());

        final HashMap<String, Object> map = new HashMap<String, Object>();

        if (sRMIRegistry == null)
        {
            createRMIRegistry(rmiRegistryPort);
        }
        else
        {
            throw new IllegalArgumentException("Can't create more than one RMI registry");
        }

        initEnvForRMI(map, options);

        final JMXServiceURL url = new JMXServiceURL(
                "service:jmx:rmi:///jndi/" + protocol + "://localhost:" + rmiRegistryPort + "/server");

        final RMIConnectorServer cs = (RMIConnectorServer) JMXConnectorServerFactory.newJMXConnectorServer(url, map, server);

        final ObjectName objectName = optionalName != null ? new ObjectName(optionalName) : new ObjectName("connector:name=rmi-connector-server,type=connector,rmi-registry-port=" + rmiRegistryPort);

        server.registerMBean(cs, objectName);

        try
        {
            cs.start();
            println("Started RMI connector server as MBean: " + objectName);
        }
        catch (IOException e)
        {
            server.unregisterMBean(objectName);
            println("RMI connector server failed to start.");
            throw e;
        }

    }


    private void handleLoadRMI()
            throws Exception
    {
        final MBeanServer server = findMBeanServer(getDefaultConnectionName());
        if (server == null)
        {
            throw new CmdException(getSubCmdNameAsInvoked(), "Not connected to a local MBeanServer");
        }

        final String cmd = getSubCmdNameAsInvoked();
        final String[] operands = getOperands();
        final String firstOperand = operands.length != 0 ? operands[ 0] : null;

        final SecurityOptions options = getRMISecurityOptions();

        final Integer defaultRMIRegistryPort = new Integer(54301);
        final int rmiRegistryPort =
                getInteger(RMI_REGISTRY_PORT_OPTION.getShortName(), defaultRMIRegistryPort).intValue();

        establishProxy();
        loadRMIConnector("rmi", rmiRegistryPort, firstOperand, options);

    }


    protected void executeInternal()
            throws Exception
    {
        final String cmd = getSubCmdNameAsInvoked();
        final String[] operands = getOperands();

        assert (operands != null);

        final String firstOperand = operands.length != 0 ? operands[ 0] : null;

        if (cmd.equals(CMD_NAME))
        {
            throw new IllegalUsageException(cmd);
        }
        else if (cmd.equals(LIST_NAME) && operands.length == 0)
        {
            doList();
        }
        else if (cmd.equals(START_NAME) && operands.length == 1)
        {
            final String initClassnames = getString(INIT_OPTION.getShortName(), null);
            final String mbeanClassnames = getString(MBEANS_OPTION.getShortName(), null);

            try
            {
                doStart(operands[ 0],
                        new StringifiedList(initClassnames).toArray(),
                        new StringifiedList(mbeanClassnames).toArray());
            }
            catch (Exception e)
            {
                doStop(operands[ 0]);
                throw e;
            }
        }
        else if (cmd.equals(STOP_NAME) && operands.length == 1)
        {
            doStop(operands[ 0]);
        }
        else if (cmd.equals(LOAD_JMXMP_CONNECTOR_NAME))
        {
            handleLoadJMXMP();
        }
        else if (cmd.equals(LOAD_RMI_CONNECTOR_NAME))
        {
            handleLoadRMI();
        }
        else if (cmd.equals(LOAD_HTML_ADAPTER_NAME))
        {
            establishProxy();
            final Integer defaultValue = new Integer(54322);

            loadHTMLAdapter(getInteger(PORT_OPTION.getShortName(), defaultValue).intValue(), firstOperand);
        }
        else
        {
            throw new IllegalUsageException(cmd);
        }
    }
}



