/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.cli.jmxcmd.test.cmd;

import java.util.List;
import java.util.ArrayList;

import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.CmdException;
import com.sun.cli.jcmd.framework.IllegalUsageException;



import com.sun.cli.jcmd.util.cmd.ArgHelperTest;
import com.sun.cli.jcmd.util.cmd.OptionsInfoTest;
import com.sun.cli.jcmd.util.misc.TokenizerTest;
import com.sun.cli.jcmd.util.misc.CompareUtilTest;
import com.sun.cli.jcmd.util.misc.StringEscaperTest;
import com.sun.cli.jmxcmd.support.ArgParserTest;
import com.sun.cli.jmxcmd.support.AliasMgrTest;
import com.sun.cli.jmxcmd.support.CLISupportMBeanImplTest;
import org.glassfish.admin.amx.util.stringifier.IteratorStringifierTest;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;


import org.glassfish.admin.amx.util.jmx.ObjectNameQueryImplTest;

import junit.extensions.ActiveTestSuite;
import junit.framework.TestCase;
import org.glassfish.admin.amx.util.ClassUtil;
import org.glassfish.admin.amx.util.ListUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.TypeCast;

/**
Run the JUnit tests.
 */
public class RunTestsCmd extends com.sun.cli.jcmd.framework.CmdBase
{

    public RunTestsCmd(final CmdEnv env)
    {
        super(env);
    }
    @SuppressWarnings("unchecked")
    private final static List<Class<? extends TestCase>> TEST_CLASSES = TypeCast.asList(ListUtil.newList(
            new Class[]
            {
                StringEscaperTest.class,
                CompareUtilTest.class,
                TokenizerTest.class,
                IteratorStringifierTest.class,
                OptionsInfoTest.class,
                ArgHelperTest.class,
                ArgParserTest.class,
                AliasMgrTest.class,
                ObjectNameQueryImplTest.class,
                CLISupportMBeanImplTest.class
            }));

    static final class RunTestsCmdHelp extends CmdHelpImpl
    {

        public RunTestsCmdHelp()
        {
            super(getCmdInfos());
        }
        private final static String SYNOPSIS = "JUnit unit tests support";
        private final static String SYNTAX =
                "run-tests\n" +
                "";
        private final static String TEXT =
                "Run known tests, or a specific one\n\n" +
                RUN_ALL_TESTS_NAME + " - runs all tests compiled into this command\n" +
                RUN_TESTS_NAME + " - runs a test class extending junit.framework.TestCase\n" +
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
            return (TEXT);
        }
    }
    final static String NAME = "tests";
    final static String RUN_ALL_TESTS_NAME = "run-all-tests";
    final static String RUN_TESTS_NAME = "run-tests";


    public CmdHelp getHelp()
    {
        return (new RunTestsCmdHelp());
    }
    private final static CmdInfo RUN_ALL_TESTS_INFO = new CmdInfoImpl(RUN_ALL_TESTS_NAME);
    private final static CmdInfo RUN_TESTS_INFO =
            new CmdInfoImpl(RUN_TESTS_NAME, new OperandsInfoImpl("classname[ classname]*", 1));


    public static CmdInfos getCmdInfos()
    {
        return (new CmdInfos(RUN_ALL_TESTS_INFO, RUN_TESTS_INFO));
    }


    int testClass(Class<? extends TestCase> theClass)
    {
        System.out.println("*** testing " + theClass.getName() + " ***");
        // use 'ActiveTestSuite' to thread the tests
        final ActiveTestSuite suite = new ActiveTestSuite(theClass);
        junit.framework.TestResult result = junit.textui.TestRunner.run(suite);

        return (result.failureCount());
    }


    void runTests(final List<Class<? extends TestCase>> classes)
            throws CmdException
    {
        for (final Class<? extends TestCase> c : classes)
        {
            final int failureCount = testClass(c);
            if (failureCount != 0)
            {
                throw new CmdException(getSubCmdNameAsInvoked(), "Unit had failures: " + failureCount);
            }
        }
    }


    void runTests(String[] names)
            throws ClassNotFoundException, CmdException
    {
        final List<Class<? extends TestCase>> classes = new ArrayList<Class<? extends TestCase>>();

        for (int i = 0; i < names.length; ++i)
        {
            @SuppressWarnings("unchecked")
            final Class<? extends TestCase> c = (Class<? extends TestCase>) ClassUtil.getClassFromName(names[i]);
            classes.add(c);
            if (!TestCase.class.isAssignableFrom(c))
            {
                throw new CmdException(getSubCmdNameAsInvoked(), "class " +
                        StringUtil.quote(names[i]) + " does not extend junit.framework.TestCase");
            }
        }
        runTests(classes);
    }


    protected void executeInternal()
            throws Exception
    {
        final String[] operands = getOperands();
        final String cmd = getSubCmdNameAsInvoked();

        if (cmd.equals(RUN_ALL_TESTS_NAME))
        {
            runTests(TEST_CLASSES);
        }
        else if (cmd.equals(RUN_TESTS_NAME))
        {
            runTests(operands);
        }
        else
        {
            throw new IllegalUsageException(cmd);
        }
    }
}






