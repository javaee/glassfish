/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/InvokeCmd.java,v 1.7 2004/04/26 07:29:38 llc Exp $
 * $Revision: 1.7 $
 * $Date: 2004/04/26 07:29:38 $
 */
package com.sun.cli.jmxcmd.cmd;

import com.sun.cli.jmxcmd.support.InvokeResult;
import org.glassfish.admin.amx.util.stringifier.*;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.IllegalUsageException;
import com.sun.cli.jcmd.framework.WrongNumberOfOperandsException;
import com.sun.cli.jcmd.framework.CmdNotFoundException;


import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;

import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;


/**
Provides ability to invoke MBean operations.
 */
public class InvokeCmd extends JMXCmd
{
    public InvokeCmd(final CmdEnv env)
    {
        super(env);
    }

    static final class InvokeCmdHelp extends CmdHelpImpl
    {
        private final static String SYNOPSIS = "invoke an MBean operation";
        private final static String INVOKE_SYNTAX = "" +
            "<operation>:[<value>[,<value>]*]" + " " + TARGET_LIST_ARG + "\n" +
            "<alias-name>.<operation>:[<value>[,<value>]*]" + "\n\n" +
            "<operation>(<name>=<value>[,<name>=<value>]*)" + " " + TARGET_LIST_ARG + "\n" +
            "<alias-name>.<operation>(<name>=<value>[,<name>=<value>]*)";
        private final static String INDENT = "    ";
        private final static String INVOKE_TEXT =
            "Examples:\n" +
            INDENT + "start: MyMBean\n" +
            INDENT + "MyMBean.start:\n" +
            INDENT + "MyMBean.start()\n" +
            INDENT + "start() MyMBean\n" +
            "\nNamed invocation:\n" +
            INDENT + "stop:timeout=30 MyMBean\n" +
            INDENT + "MyMBean.stop:timeout=30\n" +
            INDENT + "MyMBean.stop(timeout=30)\n" +
            INDENT + "stop(timeout=30) MyMBean\n" +
            "\n" +
            "The invoke operation is unusual in that no special command is entered. Instead, the MBean operation name is used " +
            "directly with a special syntax.\n\n" +
            "There are two forms of invocation--Ordered and Named.  Ordered invocation requires the parameters in " +
            "the correct order as a comma-separated list. " +
            "Named invocation relies on an operation's parameter names, which may or may not be " +
            "available for some MBeans.  You can use the 'inspect' command to see if parameter names are available for an MBean." +
            "\nNamed invocation also works for operations taking a Properties object; excess parameters are supplied to the " +
            "operation in the Properties object.\n\n" +
            "All forms of invocation use the MBeanInfo to determine the correct match for an operation. " +
            "For an operation to be available for invocation, it must restrict its use of data " +
            "types to the following:\n" +
            "   char, byte, short, int, long, float, double\n" +
            "   Character, Byte, Short, Integer, Long, Float, Double, Number, BigNumber\n" +
            "   String, Object, Properties\n" +
            "   [] all arrays of the above types\n\n" +
            "Type-casts may be used, but are rarely needed.  Use a type cast to force a number to a String or int to Integer, etc. " +
            "\n\nArrays are denoted using curly braces, and may be nested.  Examples of arrays:\n" +
            "   {1,2,3}\n" +
            "   {hello,there}\n" +
            "   {hello,1,there,2}\n" +
            "   {{1,2},{3,4}}\n" +
            "\nIf a type-cast is applied to an array, then all elements of that array must be compatible with it.  The following type-cast " +
            "forces the value to be converted to an array of String (which would match 'String []' in an operation):\n" +
            "   (String){1,2,3}\n" +
            "\nStrings may be quoted with the double-quote character \".  This is not required, but can be useful to force a value's type " +
            "to be a String.\n";


        public InvokeCmdHelp()
        {
            super(getCmdInfos());
        }


        public String getName()
        {
            return (InvokeCmd.FORMAL_NAME);
        }


        public String[] getNames()
        {
            return (new String[]
                {
                    getName(), "<mbean-operation>(...)"
                });
        }


        public String getSynopsis()
        {
            return (formSynopsis(SYNOPSIS));
        }


        public String getSyntax()
        {
            return (INVOKE_SYNTAX);
        }


        public String getText()
        {
            return (INVOKE_TEXT);
        }
    }


    public CmdHelp getHelp()
    {
        return (new InvokeCmdHelp());
    }
    public static String FORMAL_NAME = "invoke";
    private final static CmdInfo INVOKE_INFO =
        new CmdInfoImpl(FORMAL_NAME, TARGETS_OPERAND_INFO);


    public static CmdInfos getCmdInfos()
    {
        return (new CmdInfos(INVOKE_INFO));
    }


    protected int getNumRequiredOperands()
    {
        return (INVOKE_INFO.getOperandsInfo().getMinOperands());
    }

    private final class InvokeInfo
    {
        InvokeInfo()
        {
            operationName = null;
            targets = null;
            argList = null;
        }
        String operationName;
        String[] targets;
        String argList;


        public String toString()
        {
            return (operationName + "(" +
                (argList == null ? "" : argList) +
                ") on " + ArrayStringifier.stringify(targets, " "));
        }
    }


    protected OptionsInfo getOptionsInfo()
    {
        return (OptionsInfoImpl.NONE);
    }


    /*
    Allow several forms of invocation:

    // forms in which targets are operands
    doIt:[args] [targets]
    doIt([args]) [targets]

    // forms in which targets are specified as an alias
    alias.doIt:[args]
    alias.doIt([args])
     */
    InvokeInfo getInvokeInfo(final String cmdString)
        throws IllegalUsageException, WrongNumberOfOperandsException, CmdNotFoundException
    {
        InvokeInfo invokeInfo = new InvokeInfo();

        int colonIndex = cmdString.indexOf(":");
        int leftParenIndex = cmdString.indexOf('(');

        String operationPortion = null;

        /*
        first determine if it's the ":" form or the "(...)" form
        - if a colon preceeds a left paren, then its the xxx: form
        - if the last char is a right paren, and the left paren preceeds it, then its the paren form
         */
        if (colonIndex >= 1 && (colonIndex < leftParenIndex || leftParenIndex < 0))
        {
            invokeInfo.argList = cmdString.substring(colonIndex + 1, cmdString.length());
            operationPortion = cmdString.substring(0, colonIndex);
        }
        else if (leftParenIndex >= 1 && cmdString.charAt(cmdString.length() - 1) == ')')
        {
            invokeInfo.argList = cmdString.substring(leftParenIndex + 1, cmdString.length() - 1);
            operationPortion = cmdString.substring(0, leftParenIndex);

        }
        else
        {
            throw new CmdNotFoundException(cmdString, "command not found");
        }

        if (invokeInfo.argList.length() == 0)
        {
            invokeInfo.argList = null;
        }

        // now determine the targets
        int dotIndex = operationPortion.indexOf('.');
        if (dotIndex < 0)
        {
            invokeInfo.operationName = operationPortion;
            invokeInfo.targets = getTargets();
        }
        else if (dotIndex >= 1)
        {
            if (getOperands().length >= 1)
            {
                throw new IllegalUsageException("Can't supply operands when using alias invocation form");
            }

            invokeInfo.operationName = operationPortion.substring(dotIndex + 1, operationPortion.length());
            invokeInfo.targets = new String[]
                {
                    operationPortion.substring(0, dotIndex)
                };
        }
        else
        {
            throw new IllegalUsageException(cmdString);
        }

        return (invokeInfo);
    }


    protected void executeInternal()
        throws Exception
    {
        final String cmdString = getSubCmdNameAsInvoked();

        if (cmdString.equals(FORMAL_NAME))
        {
            throw new IllegalUsageException("invoke");
        }

        final InvokeInfo invokeInfo = getInvokeInfo(cmdString);

        println("invoking " + invokeInfo.toString());
        final InvokeResult[] results = invoke(invokeInfo.operationName, invokeInfo.argList, invokeInfo.targets);

        if (results.length == 0)
        {
            println("Invocation failed: no targets found");
        }
        else
        {
            displayResults(results);
        }

        envPut(JMXCmdEnvKeys.INVOKE_RESULT, results, false);
    }


    void displayResults(final InvokeResult[] results)
    {
        // to exert more control over formatting we'll loop manually
        // instead of new SmartStringifier( "\n", false ).stringify( results )
        for (int i = 0; i < results.length; ++i)
        {
            final InvokeResult result = results[i];

            final String s = new SmartStringifier("\n", false).stringify(result);
            println(s + "\n");
        }
    }


    InvokeResult[] invoke(String operationName, final String argList, final String[] targets)
        throws Exception
    {
        establishProxy();
        final InvokeResult[] results = getProxy().mbeanInvoke(operationName, argList, targets);

        return (results);
    }
}



