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

import com.sun.cli.jcmd.util.cmd.OptionDependency;
import com.sun.cli.jcmd.util.cmd.DisallowedOptionDependency;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;


import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;

import com.sun.cli.jmxcmd.support.InspectResultStringifier;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;

import org.glassfish.admin.amx.util.jmx.MBeanInterfaceGenerator;

/**
Display all aspects of MBeanInfo
 */
public class InspectCmd extends JMXCmd
{
    final boolean mShowJava;


    public InspectCmd(final CmdEnv env)
    {
        super(env);

        mShowJava = false;
    }

    static final class InspectCmdHelp extends CmdHelpImpl
    {
        private final static String INSPECT_NAME = "inspect";
        private final static String SYNOPSIS = "display attributes, operations, constructors, etc";
        private final static String INSPECT_TEXT =
            "One or more targets may be specified.  The output will be displayed for each resulting MBean. " +
            "The following options are available:\n" +
            "--all           display all available information\n" +
            "--no-description omit descriptions (avoids clutter if there are none)\n" +
            "--attributes    display the specified attributes (* = all attributes)\n" +
            "--operations    display the specified operations (* = all operations)\n" +
            "--constructors  display all available constructors\n" +
            "--notifications display the specified notifications (* = all notifications)\n" +
            "--include-empty display all targeted MBeans even if nothing matched\n" +
            "";


        public InspectCmdHelp()
        {
            super(getCmdInfos());
        }


        public String getSynopsis()
        {
            return (formSynopsis(SYNOPSIS));
        }


        public String getText()
        {
            return (INSPECT_TEXT);
        }
    }


    public CmdHelp getHelp()
    {
        return (new InspectCmdHelp());
    }
    private final static OptionInfo ALL_OPTION = new OptionInfoImpl("all", "A");
    private final static OptionInfo SUMMARY_OPTION = new OptionInfoImpl("summary", "s");
    private final static OptionInfo CONSTRUCTORS_OPTION = new OptionInfoImpl("constructors", "c");
    private final static OptionInfo ATTRIBUTES_OPTION = new OptionInfoImpl("attributes", "a", "name-expr-list");
    private final static OptionInfo OPERATIONS_OPTION = new OptionInfoImpl("operations", "o", "operation-expr-list");
    private final static OptionInfo NOTIFICATIONS_OPTION = new OptionInfoImpl("notifications", "n");
    private final static OptionInfo NODESCRIPTION_OPTION = new OptionInfoImpl("no-description", "d");
    private final static OptionInfo INCLUDE_EMPTY_OPTION = new OptionInfoImpl("include-empty", "i");
    private static final OptionInfo[] OPTIONS_INFO_ARRAY =
    {
        ALL_OPTION,
        SUMMARY_OPTION,
        CONSTRUCTORS_OPTION,
        ATTRIBUTES_OPTION,
        OPERATIONS_OPTION,
        NOTIFICATIONS_OPTION,
        NODESCRIPTION_OPTION,
        INCLUDE_EMPTY_OPTION,
    };


    static
    {
        OptionDependency d =
            new DisallowedOptionDependency(SUMMARY_OPTION, CONSTRUCTORS_OPTION,
            ATTRIBUTES_OPTION, OPERATIONS_OPTION, NOTIFICATIONS_OPTION);
        ALL_OPTION.addDependency(d);
    }
    private final static OptionsInfo OPTIONS_INFO = new OptionsInfoImpl(OPTIONS_INFO_ARRAY);
    public static final String INSPECT_NAME = "inspect";
    private static final CmdInfo INSPECT_INFO = new CmdInfoImpl(INSPECT_NAME, OPTIONS_INFO, TARGETS_OPERAND_INFO);


    public static CmdInfos getCmdInfos()
    {
        return (new CmdInfos(INSPECT_INFO));
    }


    void displayResults(
        final InspectRequest request,
        final InspectResult[] results,
        boolean includeEmptyMatches)
    {
        boolean summaryOnly =
            request.attrs == null &&
            request.operations == null &&
            request.constructors == false &&
            request.notifications == null;

        final MBeanInterfaceGenerator generator = new MBeanInterfaceGenerator();

        for (int i = 0; i < results.length; ++i)
        {
            final InspectResult result = results[i];

            boolean emptyResult = (request.attrs == null || result.attrInfo.length == 0) &&
                (request.operations == null || result.operationsInfo.length == 0) &&
                (request.constructors == false || result.constructorsInfo.length == 0) &&
                (request.notifications == null || result.notificationsInfo.length == 0);

            boolean display = false;
            if (includeEmptyMatches)
            {
                display = true;
            }
            else
            {
                display = !emptyResult;
            }

            if (display)
            {
                println(InspectResultStringifier.DEFAULT.stringify(result));
                println("");
                println("");
                if (mShowJava)
                {
                    final String intf = generator.generate(results[i].mbeanInfo, true);
                    println(intf);
                    println("");
                }
            }
        }
    }


    void handle_inspect(String[] targets, final InspectRequest request)
        throws Exception
    {
        final InspectResult[] results = getProxy().mbeanInspect(request, targets);

        final boolean summaryOnly = request.attrs == null &&
            request.operations == null &&
            request.constructors == false &&
            request.notifications == null;

        if (results.length == 0)
        {
            println("<nothing to inspect>");
        }
        else
        {
            final boolean includeEmptyMatches = getBoolean(
                INCLUDE_EMPTY_OPTION.getShortName(), Boolean.FALSE).booleanValue();

            displayResults(request, results, includeEmptyMatches || summaryOnly);
        }

        envPut(JMXCmdEnvKeys.INSPECT_RESULT, results, false);
    }


    void handle_inspect(final String[] targets)
        throws Exception
    {
        final InspectRequest request = new InspectRequest();

        request.includeDescription = getBoolean(NODESCRIPTION_OPTION.getShortName(), Boolean.TRUE).booleanValue();

        if (countOptions() == 0 ||
            getBoolean(ALL_OPTION.getShortName(), Boolean.FALSE).booleanValue())
        {
            // should already be setup to get everything
        }
        else
        {
            request.includeSummary = getBoolean(SUMMARY_OPTION.getShortName(), Boolean.FALSE).booleanValue();
            request.constructors = getBoolean(CONSTRUCTORS_OPTION.getShortName(), Boolean.FALSE).booleanValue();
            request.attrs = getString(ATTRIBUTES_OPTION.getShortName(), null);
            request.operations = getString(OPERATIONS_OPTION.getShortName(), null);
            request.notifications = getString(NOTIFICATIONS_OPTION.getShortName(), null);
        }

        handle_inspect(targets, request);
    }


    protected void executeInternal()
        throws Exception
    {
        final String cmd = getSubCmdNameAsInvoked();
        final String[] targets = getTargets();

        if (targets == null)
        {
            printError("No targets have been specified");
            return;
        }

        establishProxy();
        if (cmd.equalsIgnoreCase(INSPECT_NAME))
        {
            handle_inspect(targets);
        }
    }
}

