/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/InspectCmd.java,v 1.12 2004/03/16 01:09:33 llc Exp $
 * $Revision: 1.12 $
 * $Date: 2004/03/16 01:09:33 $
 */
package com.sun.cli.jmxcmd.cmd;

import com.sun.cli.jmxcmd.support.InspectRequest;
import com.sun.cli.jmxcmd.support.InspectResult;

import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;


import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;


import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;

import javax.management.ObjectName;
import org.glassfish.admin.amx.util.jmx.MBeanInterfaceGenerator;

/**
Display all aspects of MBeanInfo
 */
public class JavaCmd extends JMXCmd
{
    boolean mEmitComments = false;


    public JavaCmd(final CmdEnv env)
    {
        super(env);
    }
    public static final String JAVA_NAME = "java";

    static final class JavaCmdHelp extends CmdHelpImpl
    {
        private final static String NAME = JAVA_NAME;
        private final static String SYNOPSIS = "display metadata as Java interface";
        private final static String TEXT =
            "One or more targets may be specified.  The output will be displayed for each resulting MBean. " +
            "The following options are available:\n" +
            "--docs          include javadoc\n" +
            "";


        public JavaCmdHelp()
        {
            super(getCmdInfos());
        }


        public String getSynopsis()
        {
            return formSynopsis(SYNOPSIS);
        }


        public String getText()
        {
            return TEXT;
        }
    }


    public CmdHelp getHelp()
    {
        return new JavaCmdHelp();
    }
    private final static OptionInfo DOCS_OPTION = new OptionInfoImpl("docs", "d");
    private static final OptionInfo[] OPTIONS_INFO_ARRAY =
    {
        DOCS_OPTION
    };
    private final static OptionsInfo OPTIONS_INFO = new OptionsInfoImpl(OPTIONS_INFO_ARRAY);
    private static final CmdInfo JAVA_INFO = new CmdInfoImpl(JAVA_NAME, OPTIONS_INFO, TARGETS_OPERAND_INFO);


    public static CmdInfos getCmdInfos()
    {
        return new CmdInfos(JAVA_INFO);
    }


    void displayResults(
        final InspectRequest request,
        final InspectResult[] results,
        boolean includeEmptyMatches)
    {
        //final MBeanGenerator generator = new MBeanGenerator();
        final MBeanInterfaceGenerator generator = new MBeanInterfaceGenerator();

        for (final InspectResult result : results)
        {
            if (result.mbeanInfo == null)
            {
                System.out.println("displayResults: null MbeanInfo");
                continue;
            }

            final ObjectName objectName = result.objectInstance.getObjectName();
            String type = objectName.getKeyProperty("type");
            if (type == null)
            {
                type = objectName.getKeyProperty("j2eeType");
                if (type == null)
                {
                    type = objectName.getKeyProperty("name");
                }
                if (type == null)
                {
                    type = "unknown";
                }
            }

            // final String intfName = type.replaceAll("-", "_");
            //final String intf = generator.generate(result.mbeanInfo, intfName, mEmitComments);
            final String intf = generator.generate(result.mbeanInfo, mEmitComments);
            println("/* Generated " + new java.util.Date() + " from " + objectName + " */");
            println(intf);
            println("");
        }
    }


    void handle_inspect(
        final String[] targets,
        final InspectRequest request)
        throws Exception
    {
        final InspectResult[] results = getProxy().mbeanInspect(request, targets);

        if (results.length == 0)
        {
            println("<nothing to inspect>");
        }
        else
        {
            final boolean includeEmptyMatches = false;
            displayResults(request, results, includeEmptyMatches);
        }

        envPut(JMXCmdEnvKeys.INSPECT_RESULT, results, false);
    }


    void handle_inspect(final String[] targets)
        throws Exception
    {
        final InspectRequest request = new InspectRequest();

        if (countOptions() != 0)
        {
            mEmitComments = getBoolean(DOCS_OPTION.getShortName(), Boolean.FALSE).booleanValue();
            
            request.includeSummary = true;
            request.includeDescription = false;
            request.constructors = true;
            request.attrs = null;
            request.operations = null;
            request.notifications = null;
        }

        handle_inspect(targets, request);
    }


    protected void executeInternal()
        throws Exception
    {
        final String cmd = getSubCmdNameAsInvoked();
        final String[] targets = getTargets();

        if (targets == null || targets.length == 0)
        {
            printError("No targets have been specified");
            return;
        }

        establishProxy();
        if (cmd.equalsIgnoreCase(JAVA_NAME))
        {
            handle_inspect(targets);
        }
    }
}

