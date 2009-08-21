/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/MBeanCmd.java,v 1.10 2004/07/22 22:23:30 llc Exp $
 * $Revision: 1.10 $
 * $Date: 2004/07/22 22:23:30 $
 */
package com.sun.cli.jmxcmd.cmd;

import java.io.IOException;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;

import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;

import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.IllegalUsageException;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;

import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

/*
Creates and registers an MBean.
 */
public class MBeanCmd extends JMXCmd
{
    public MBeanCmd(final CmdEnv env)
    {
        super(env);
    }

    static final class MBeanCmdHelp extends CmdHelpImpl
    {
        public MBeanCmdHelp()
        {
            super(getCmdInfos());
        }
        private final static String SYNOPSIS = "register and unregister mbeans";
        private final static String CREATE_SYNTAX =
            REGISTER_INFO.getSyntax() + "\n" +
            UNREGISTER_INFO.getSyntax() + "\n" +
            REGISTER_SERVICES_INFO.getSyntax();
        private final static String CREATE_TEXT =
            "Register/unregister MBeans and load JMX relation and timer services.\n" +
            "";


        public String getName()
        {
            return (NAME);
        }


        public String[] getNames()
        {
            return (MBeanCmd.getCmdInfos().getNames());
        }


        public String getSynopsis()
        {
            return (formSynopsis(SYNOPSIS));
        }


        public String getSyntax()
        {
            return (CREATE_SYNTAX);
        }


        public String getText()
        {
            return (CREATE_TEXT);
        }
    }


    public CmdHelp getHelp()
    {
        return (new MBeanCmdHelp());
    }
    private final static OptionInfo CLASS_OPTION = new OptionInfoImpl("class", "c", "classname", true);
    private final static OptionInfo ARGS_OPTION = new OptionInfoImpl("args", "a", "argument-list");
    static private final OptionInfo[] REGISTER_OPTIONS =
    {
        CLASS_OPTION,
        ARGS_OPTION,
    };
    public final static String NAME = "mbean";
    public final static String REGISTER_NAME = "register-mbean";
    public final static String UNREGISTER_NAME = "unregister-mbeans";
    public final static String REGISTER_SERVICES_NAME = "register-services";
    public final static String TIME_MBEAN_INFO_NAME = "time-mbean-info";
    private final static CmdInfo REGISTER_INFO =
        new CmdInfoImpl(REGISTER_NAME, new OptionsInfoImpl(REGISTER_OPTIONS),
        new OperandsInfoImpl("<object-name>", 1));
    private final static CmdInfo UNREGISTER_INFO =
        new CmdInfoImpl(UNREGISTER_NAME, new OperandsInfoImpl(TARGET_LIST_ARG, 1));
    private final static CmdInfo REGISTER_SERVICES_INFO =
        new CmdInfoImpl(REGISTER_SERVICES_NAME);
    private final static CmdInfo TIME_MBEAN_INFO_INFO =
        new CmdInfoImpl(TIME_MBEAN_INFO_NAME, TARGETS_OPERAND_INFO);


    public static CmdInfos getCmdInfos()
    {
        return (new CmdInfos(REGISTER_INFO, UNREGISTER_INFO, REGISTER_SERVICES_INFO,
            TIME_MBEAN_INFO_INFO));
    }


    private void registerServices()
        throws Exception
    {
        final MBeanServer server = getMBeanServer();

        ObjectName objectName = new ObjectName("system:name=RelationService");
        server.createMBean("javax.management.relation.RelationService",
            objectName,
            new Object[]
            {
                Boolean.TRUE
            },
            new String[]
            {
                boolean.class.getName()
            });
        println("Registered: " + objectName);

        objectName = new ObjectName("system:name=TimerService");
        server.createMBean("javax.management.timer.Timer",
            new ObjectName("system:name=TimerService"));
        println("Registered: " + objectName);
    }


    private final void timeMBeanInfo(final ObjectName[] targets)
        throws IOException
    {
        final MBeanServerConnection conn = getConnection();

        for (int i = 0; i < targets.length; ++i)
        {
            final ObjectName objectName = targets[i];

            try
            {
                final long start = now();

                final MBeanInfo info = conn.getMBeanInfo(objectName);

                final long elapsed = now() - start;
                println(objectName + " = " + elapsed + " ms");
            }
            catch (InstanceNotFoundException e)
            {
                println(objectName + " = NOT FOUND");
            }
            catch (JMException e)
            {
                println(objectName + " = EXCEPTION: " + e.getMessage());
            }
        }
    }


    protected void executeInternal()
        throws Exception
    {
        final String[] operands = getOperands();
        final String cmd = getSubCmdNameAsInvoked();

        if (cmd.equals(REGISTER_NAME))
        {
            final String name = operands[operands.length - 1];

            final String theClass = getString(CLASS_OPTION.getShortName(), null);
            if (theClass == null || theClass.length() == 0)
            {
                final String msg = "Illegal classname";
                printError(msg);
                throw new IllegalArgumentException(msg);
            }

            final String args = getString(ARGS_OPTION.getShortName(), null);

            establishProxy();
            getProxy().mbeanCreate(name, theClass, args);
        }
        else if (cmd.equals(UNREGISTER_NAME))
        {
            establishProxy();


            final CLISupportMBeanProxy proxy = getProxy();

            final ObjectName[] targets = resolveTargets(proxy, getTargets());
            for (int i = 0; i < targets.length; ++i)
            {
                try
                {
                    proxy.mbeanUnregister(targets[i].toString());
                    println("unregistered: " + targets[i]);
                }
                catch (Exception e)
                {
                    println("exception unregistering: " + targets[i] + " = \n" + e.getMessage());
                }
            }
        }
        else if (cmd.equals(REGISTER_SERVICES_NAME))
        {
            establishProxy();
            registerServices();
        }
        else if (cmd.equals(TIME_MBEAN_INFO_NAME))
        {
            establishProxy();
            final String[] targets = getTargets();
            final ObjectName[] objectNames = resolveTargets(getProxy(), targets);
            timeMBeanInfo(objectNames);
        }
        else
        {
            throw new IllegalUsageException(cmd, "unknown cmd");
        }
    }
}






