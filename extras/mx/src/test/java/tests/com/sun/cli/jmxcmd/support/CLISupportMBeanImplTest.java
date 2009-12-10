/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/support/CLISupportMBeanImplTest.java,v 1.4 2003/12/09 01:39:01 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2003/12/09 01:39:01 $
 */
package com.sun.cli.jmxcmd.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.lang.Number;


import javax.management.*;

import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;


import com.sun.cli.jmxcmd.test.mbeans.CLISupportTestee;
import com.sun.cli.jmxcmd.test.mbeans.CLISupportSimpleTestee;
import org.glassfish.admin.amx.util.ArrayConversion;
import org.glassfish.admin.amx.util.ClassUtil;

public final class CLISupportMBeanImplTest extends junit.framework.TestCase
{

    private static void debug(final Object o)
    {
        System.out.println(SmartStringifier.toString(o));
    }
    MBeanServer mServer;
    CLISupportMBeanProxy mProxy;
    private final static String ALIAS_BASE = "test-alias-";
    private final static String CLI_TEST_ALIAS_NAME = ALIAS_BASE + "generic-test";
    private final static String ALL_ALIAS = "all";
    private final static String SIMPLE_TESTEE_ALIAS = "StdTestee";
    private final static String SUPPORT_TESTEE_ALIAS = "SupportTestee";
    private final static String[] ALL_TARGETS = new String[]
    {
        ALL_ALIAS
    };
    private final static String[] SIMPLE_TESTEE_TARGET = new String[]
    {
        SIMPLE_TESTEE_ALIAS
    };
    private final static String[] SUPPORT_TESTEE_TARGET = new String[]
    {
        SUPPORT_TESTEE_ALIAS
    };


    public CLISupportMBeanImplTest()
    {
    }

    private class TestFailedException extends Exception
    {

        TestFailedException(String msg)
        {
            super(msg);
        }
    }


    public void testPropertiesOnly() throws Exception
    {
        final String operationName = "testPropertiesOnly";
        final String argString = "foo=bar";
        final InvokeResult[] results = (InvokeResult[]) mProxy.mbeanInvoke(operationName, argString, SUPPORT_TESTEE_TARGET);

        final InvokeResult result = results[ 0];

        if (result.getResultType() != InvokeResult.SUCCESS)
        {
            throw new TestFailedException("invocation failed: " + operationName + "(" + argString + ")");
        }

    }
    private final static String ARGS_11 =
            "hello,true,x,99,999,9999,999999,99.9999,999.999999,12345678910,12345678910.12345678910";


    public void test11ObjectArgs() throws Exception
    {
        invokeSimpleTestee("test11ObjectArgs", ARGS_11);
    }


    public void test11MixedArgs() throws Exception
    {
        invokeSimpleTestee("test11MixedArgs", ARGS_11);
    }


    private Object doTest(final String operationName, final String arg)
            throws Exception
    {
        final InvokeResult invokeResult = invokeSimpleTestee(operationName, arg);

        if (invokeResult.getResultType() != InvokeResult.SUCCESS)
        {
            fail("invocation failed for " + operationName + "(" + arg + ")");
        }

        return (invokeResult.mResult);
    }


    private void doStringTest(final String arg, final String expected)
            throws Exception
    {
        //debug( "doStringTest: " + arg + " = " + expected );
        final String result = (String) doTest("testString", arg);
        assertEquals(expected, result);
    }


    private void doObjectTest(final String arg, final Object expected)
            throws Exception
    {
        final Object result = doTest("testObject", arg);
        assertEquals(expected, result);
    }


    private void doIntegerTest(final String arg, final Object expected)
            throws Exception
    {
        final Integer result = (Integer) doTest("testInteger", arg);
        assertEquals(expected, result);
    }


    private void checkEqualArray(final Object[] expected, final Object[] actual)
    {
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; ++i)
        {
            if (expected[i] == null)
            {
                assertEquals(null, actual[i]);
            }
            else
            {
                assertEquals(expected[i].getClass(), actual[i].getClass());

                if (ClassUtil.objectIsArray(expected[i]))
                {
                    if (ClassUtil.objectIsPrimitiveArray(expected[i]))
                    {
                        final Object[] e = ArrayConversion.toAppropriateType(expected[i]);
                        final Object[] a = ArrayConversion.toAppropriateType(actual[i]);

                        checkEqualArray(e, a);
                    }
                    else
                    {
                        checkEqualArray((Object[]) expected[i], (Object[]) actual[i]);
                    }
                }
                else
                {
                    assertEquals(expected[i], actual[i]);
                }
            }
        }
    }


    private void doObjectArrayTest(final String arg, final Object[] expected)
            throws Exception
    {
        final Object[] result = (Object[]) doTest("testObjectArray", arg);
        checkEqualArray(expected, result);
    }
    static final char QUOTE = '\"';


    /**
    The '=' character can be misinterpreted as a name value pair.
     */
    public void testStringContainingEquals() throws Exception
    {
        // test that escaping it works
        doStringTest("a\\=b", "a\\=b");

        // test that quoting it works
        final String testStr = "a=b";
        doStringTest(QUOTE + testStr + QUOTE, testStr);
    }


    public void testEmptyQuotedString() throws Exception
    {
        doStringTest("\"\"", "");
    }


    public void testEmptyQuotedStringWithCast() throws Exception
    {
        doStringTest("(String)\"\"", "");
    }


    public void testEmptyStringWithCast() throws Exception
    {
        doStringTest("(String)", "");
    }


    public void testEscapedString() throws Exception
    {
        final String result = "\"\n\r\t\"";

        doStringTest("\\\"\\n\\r\\t\\\"", result);
    }


    public void testNumericStringNoCast() throws Exception
    {
        doStringTest("10", "10");
    }


    public void testStringContaining_word_null() throws Exception
    {
        doStringTest("\"null\"", "null");
    }


    public void testNullStringWithoutCast() throws Exception
    {
        doStringTest("null", null);
    }


    public void testNullStringWithCast() throws Exception
    {
        doStringTest("(String)null", null);
    }


    public void testNullObjectWithCast() throws Exception
    {
        doObjectTest("(Object)null", null);
    }


    public void testNullObjectWithoutCast() throws Exception
    {
        doObjectTest("null", null);
    }


    public void testStringAsObject() throws Exception
    {
        doObjectTest("(Object)hello", "hello");
    }


    public void testNullIntegerWithCast() throws Exception
    {
        doIntegerTest("(Integer)null", null);
    }


    public void testInteger() throws Exception
    {
        doIntegerTest("(Integer)10", new Integer(10));
        doIntegerTest("-10", new Integer(-10));
    }


    public void testNullIntegerWithoutCast() throws Exception
    {
        doIntegerTest("null", null);
    }


    public void testEmptyArray() throws Exception
    {
        doObjectArrayTest("{}", new Object[0]);
    }


    public void testEmptyObjectArrayWithCast() throws Exception
    {
        doObjectArrayTest("(Object){}", new Object[0]);
        doObjectArrayTest("(String){}", new String[0]);
        doObjectArrayTest("(Character){}", new Character[0]);
        doObjectArrayTest("(Boolean){}", new Boolean[0]);
        doObjectArrayTest("(Byte){}", new Byte[0]);
        doObjectArrayTest("(Short){}", new Short[0]);
        doObjectArrayTest("(Integer){}", new Integer[0]);
        doObjectArrayTest("(Long){}", new Long[0]);
        doObjectArrayTest("(Float){}", new Float[0]);
        doObjectArrayTest("(Double){}", new Double[0]);
        doObjectArrayTest("(BigInteger){}", new BigInteger[0]);
        doObjectArrayTest("(BigDecimal){}", new BigDecimal[0]);
        doObjectArrayTest("(Number){}", new Number[0]);
    }


    public void testArrayWithEmptyElements()
            throws Exception
    {
        final String s = new String("");

        doObjectArrayTest("(String){,,,}", new String[]
                {
                    s, s, s, s
                });
        doObjectArrayTest("{,,,}", new Object[]
                {
                    s, s, s, s
                });
    }


    public void testArrayWithNullElements()
            throws Exception
    {
        doObjectArrayTest("{null,null}", new Object[]
                {
                    null, null
                });

        doObjectArrayTest("{(String)null,(Object)null}", new Object[]
                {
                    null, null
                });
    }


    public void testArrayWithCastAndElementsWithCast()
            throws Exception
    {
        doObjectArrayTest("(Number){(BigInteger)0,(BigDecimal)10.0}",
                new Number[]
                {
                    new BigInteger("0"), new BigDecimal("10.0")
                });

        // now see that incompatible casts are rejected
        final String input = "(Number){(String)hello}";

        final InvokeResult invokeResult = invokeSimpleTestee("testObjectArray", input);
        if (invokeResult.getResultType() == InvokeResult.SUCCESS)
        {
            fail("expected this construct to fail: " + input);
        }
    }


    public void testMixedArrayWithCasts()
            throws Exception
    {
        doObjectArrayTest(
                "{(Character)c,(Boolean)true,(Byte)99," +
                "(Short)-999,(Integer)0,(Long)99999," +
                "(BigInteger)99999999999999999999999999," +
                "(BigDecimal)123456789123456789.123456789123456789," +
                "\"hello\",(String)hello,hello,}",
                new Object[]
                {
                    new Character('c'),
                    new Boolean("true"),
                    new Byte((byte) 99),
                    new Short((short) -999),
                    new Integer(0),
                    new Long(99999),
                    new BigInteger("99999999999999999999999999"),
                    new BigDecimal("123456789123456789.123456789123456789"),
                    "hello",
                    "hello",
                    "hello",
                    "",
                });
    }


    public void testNullIllegalForSimpleType() throws Exception
    {
        try
        {
            final InvokeResult result = invokeSimpleTestee("test_int", "null");
            if (result.getResultType() == InvokeResult.SUCCESS)
            {
                fail("expected failure trying to pass 'null' for an int");
            }
        }
        catch (Exception e)
        {
        }
    }


    public void testCaseSensitivity()
            throws Exception
    {
        InvokeResult result;

        result = invokeSimpleTestee("testcasesensitivity1", null);
        assertEquals("testcasesensitivity1", result.mResult);

        result = invokeSimpleTestee("testCASESENSITIVITY1", null);
        assertEquals("testCASESENSITIVITY1", result.mResult);

        result = invokeSimpleTestee("testCaseSensitivity1", null);
        assertEquals("testCaseSensitivity1", result.mResult);

        try
        {
            result = invokeSimpleTestee("testcaseSensitivity1", null);
            if (result.getResultType() == InvokeResult.SUCCESS)
            {
                fail("expected ambiguous match to prevent execution of method: " + result.mResult);
            }
        }
        catch (Exception e)
        {
            // good, expected this
        }
    }


    public void testCaseInsensitivity()
            throws Exception
    {
        final InvokeResult result = invokeSimpleTestee("testcasesensitivity2", null);
        assertEquals("testCaseSensitivity2", result.mResult);
    }


    /*
    public void
    testURL()
    throws Exception
    {
    final URL	input	= new URL( "http://www.sun.com?foo=bar&bar=foo" );
    final InvokeResult	result	= invokeSimpleTestee( "testURL", input.toString() );
    if ( result.mResult == null )
    {
    result.getThrowable().printStackTrace();
    }
    assertEquals( input, result.mResult );
    }


    public void
    testURI()
    throws Exception
    {
    final URI	input	= new URI( "service:jmx:jmxmp://localhost:" );
    final InvokeResult	result	= invokeSimpleTestee( "testURI", input.toString() );
    assertEquals( input, result.mResult );
    }
     */
    /*

    these are not yet implemented

    public void
    testProperties1Arg( ) throws Exception
    {
    final String	operationName	= "testProperties1Arg";
    final String	argString	= "p1=p1,foo=bar";
    final InvokeResult []	results	= (InvokeResult [])
    mProxy.mbeanInvoke( operationName, argString, SUPPORT_TESTEE_TARGET );

    final InvokeResult	result	= results[ 0 ];

    if ( result.getResultType() != InvokeResult.SUCCESS )
    {
    result.mThrowable.printStackTrace();
    fail( "invocation failed: " + operationName + "(" + argString + ")");
    }

    }
    public void
    testProperties2Args( ) throws Exception
    {
    final String	operationName	= "testProperties2Args";
    final String	argString		= "p1=p1,p3=3,foo=bar";
    final InvokeResult []	results	= (InvokeResult [])
    mProxy.mbeanInvoke( operationName, argString, SUPPORT_TESTEE_TARGET );

    final InvokeResult	result	= results[ 0 ];

    if ( result.getResultType() != InvokeResult.SUCCESS )
    {
    fail( "invocation failed: " + operationName + "(" + argString + ")");
    }
    }
     */
    private InvokeResult[] invoke(String operationName, String args, String[] targets)
            throws Exception
    {
        final InvokeResult[] results = mProxy.mbeanInvoke(operationName, args, targets);

        return (results);
    }


    private InvokeResult invokeSupportTestee(String operationName, String args)
            throws Exception
    {
        final InvokeResult[] results = invoke(operationName, args, SUPPORT_TESTEE_TARGET);
        assert (results.length == 1);

        return (results[ 0]);
    }


    private InvokeResult invokeSimpleTestee(String operationName, String args)
            throws Exception
    {
        final InvokeResult[] results = invoke(operationName, args, SIMPLE_TESTEE_TARGET);
        assert (results.length == 1);

        return (results[ 0]);
    }


    public void testNamedInvoke() throws Exception
    {
        invokeSupportTestee("testNamed", "p1=hello");

        invokeSupportTestee("testNamed", "p1=hello,p2=there");

        invokeSupportTestee("testNamed", "p1=hello,p2=there,p3=!!!");

        invokeSupportTestee("testNamed", "p1=hello,p2=there,p3=!!!,p4=foobar");
    }

//-------------------------------------------------------------------------------------------------

    private String makeArgList(final String[] args)
    {
        final int numArgs = args.length;
        String result = null;

        if (numArgs != 0)
        {
            final StringBuffer buf = new StringBuffer();

            for (int i = 0; i < numArgs; ++i)
            {
                buf.append(args[i]);
                buf.append(",");
            }
            // strip trailing ","
            buf.setLength(buf.length() - 1);

            result = new String(buf);
        }

        return (result);
    }


    private String getCastType(String type)
            throws ClassNotFoundException
    {
        String result = type;

        if (ClassUtil.classnameIsArray(result))
        {
            final Class theClass = ClassUtil.getClassFromName(result);

            final Class elementClass = ClassUtil.getInnerArrayElementClass(theClass);

            result = elementClass.getName();
        }

        return (result);
    }


    private InvokeResult.ResultType testOperationGenerically(
            final boolean namedArgs,
            final ObjectName targetName,
            final MBeanOperationInfo operationInfo)
            throws Exception
    {
        final MBeanParameterInfo[] paramInfos = operationInfo.getSignature();
        final int numParams = paramInfos.length;

        final String[] strings = new String[numParams];
        final String operationName = operationInfo.getName();

        // create an object of the correct type for each parameter.
        // The actual value is not important.
        for (int i = 0; i < numParams; ++i)
        {
            final MBeanParameterInfo paramInfo = paramInfos[i];
            final String paramType = paramInfos[i].getType();
            final Class theClass = ClassUtil.getClassFromName(paramType);

            final Object paramObject = ClassUtil.InstantiateDefault(theClass);
            final String paramString = SmartStringifier.toString(paramObject);
            final String castString = "(" + getCastType(paramType) + ")";

            final String paramName = namedArgs ? (paramInfo.getName() + '=') : "";

            strings[i] = paramName + castString + paramString;
        }

        // convert the arguments to strings
        final String argString = makeArgList(strings);

        final String[] args = new String[]
        {
            targetName.toString()
        };

        final InvokeResult[] results = (InvokeResult[]) mProxy.mbeanInvoke(operationName, argString, args);
        final InvokeResult result = results[ 0];

        if (result.getResultType() == InvokeResult.SUCCESS)
        {
            // p( "SUCCESS: " + operationName + "(" + SmartStringifier.toString( paramInfos ) + ")");
        }
        else
        {
            final String paramInfosString = SmartStringifier.toString(paramInfos);

            result.mThrowable.printStackTrace();
        }

        return (result.getResultType());
    }
    static private final Class[] GENERICALLY_TESTABLE_CLASSES =
    {
        boolean.class,
        char.class,
        byte.class, short.class, int.class, long.class,
        float.class, double.class,
        Boolean.class,
        Character.class,
        Byte.class, Short.class, Integer.class, Long.class,
        Float.class,
        Double.class,
        Number.class,
        String.class,
        Object.class,
        java.math.BigDecimal.class,
        java.math.BigInteger.class,
        java.net.URL.class,
        java.net.URI.class
    };


    private boolean isGenericallyTestableClass(final Class theClass)
            throws ClassNotFoundException
    {
        boolean isTestable = false;

        Class testClass = theClass;
        if (ClassUtil.classIsArray(theClass))
        {
            // we can test all arrays of supported types
            testClass = ClassUtil.getInnerArrayElementClass(theClass);
        }

        final Class[] classes = GENERICALLY_TESTABLE_CLASSES;
        final int numClasses = classes.length;
        for (int i = 0; i < numClasses; ++i)
        {
            if (testClass == classes[i])
            {
                isTestable = true;
                break;
            }
        }

        if (!isTestable)
        {
            assert (testClass == java.util.Properties.class);
        }

        return (isTestable);
    }


    private boolean isGenericallyTestable(final MBeanOperationInfo operationInfo)
            throws ClassNotFoundException
    {
        boolean isTestable = true;

        final MBeanParameterInfo[] paramInfos = operationInfo.getSignature();
        final int numParams = paramInfos.length;
        for (int i = 0; i < numParams; ++i)
        {
            final Class theClass = ClassUtil.getClassFromName(paramInfos[i].getType());

            if (!isGenericallyTestableClass(theClass))
            {
                isTestable = false;
                break;
            }
        }

        return (isTestable);
    }


    private void testGeneric(boolean namedTest, ObjectName objectName) throws Exception
    {
        final MBeanInfo info = mServer.getMBeanInfo(objectName);
        final MBeanOperationInfo[] opInfos = info.getOperations();

        int notTestedCount = 0;
        for (int i = 0; i < opInfos.length; ++i)
        {
            try
            {
                if (isGenericallyTestable(opInfos[i]))
                {
                    final InvokeResult.ResultType resultType =
                            testOperationGenerically(namedTest, objectName, opInfos[i]);

                    if (resultType != InvokeResult.SUCCESS)
                    {
                        fail("invocation failure on: " + SmartStringifier.toString(opInfos[i]));
                    }
                }
                else
                {
                    ++notTestedCount;
                }
            }
            catch (Exception e)
            {
                fail("FAILURE: " + SmartStringifier.toString(opInfos[i]));
            }
        }
    }


    public void testGenericOrdered()
            throws Exception
    {
        final ObjectName[] allObjects = mProxy.mbeanFind(SUPPORT_TESTEE_ALIAS);
        assert (allObjects.length == 1);

        testGeneric(false, allObjects[ 0]);
    }


    public void testGenericNamed()
            throws Exception
    {
        final ObjectName[] allObjects = mProxy.mbeanFind(SUPPORT_TESTEE_ALIAS);
        assert (allObjects.length == 1);

        testGeneric(false, allObjects[ 0]);
    }


    public void testMBeanFind() throws Exception
    {
        final ObjectName[] results = mProxy.mbeanFind(ALL_TARGETS);

        assertEquals(3, results.length);
    }


    public void testMBeanInspect() throws Exception
    {
        final InspectRequest request = new InspectRequest();

        final InspectResult[] results = mProxy.mbeanInspect(request, ALL_TARGETS);

        assertEquals(3, results.length);
    }


    public void testMBeanGet() throws Exception
    {
        final ResultsForGetSet[] results = mProxy.mbeanGet("*", SIMPLE_TESTEE_TARGET);

        assertEquals(1, results.length);
        assertEquals(5, results[ 0].getAttributes().size());
    }


    public void testMBeanSet() throws Exception
    {
        final ResultsForGetSet[] results = mProxy.mbeanSet("NotifMillis=1000", SIMPLE_TESTEE_TARGET);

        assertEquals(1, results.length);
        assertEquals(1, results[ 0].getAttributes().size());

        final AttributeList attrs = mProxy.mbeanGet("NotifMillis", SIMPLE_TESTEE_TARGET)[ 0].getAttributes();
        final Attribute expected = new Attribute("NotifMillis", new Long(1000));
        assertEquals(expected, attrs.get(0));
    }


    public void testMBeanCreateDelete() throws Exception
    {
        final String name = "test:name=testtemp";
        final String className = "com.sun.cli.jmxcmd.test.mbeans.CLISupportSimpleTestee";

        mProxy.mbeanCreate(name, className, null);

        ObjectName[] results = mProxy.mbeanFind(new String[]
                {
                    name
                });
        assertEquals(1, results.length);

        mProxy.mbeanUnregister(name);
        results = mProxy.mbeanFind(new String[]
                {
                    name
                });
        assertEquals(0, results.length);


    }


    private void deleteTestAliases() throws Exception
    {
        assert (mProxy != null);

        final String[] aliases = mProxy.listAliases(false);
        assert (aliases != null);

        for (int i = 0; i < aliases.length; ++i)
        {
            final String name = aliases[i];

            if (name.startsWith(ALIAS_BASE))
            {
                mProxy.deleteAlias(name);
            }
        }
    }


    /*
    Unlike tests for the AliasMgr, this test checks for recursive resolution of aliases
    and resolution to actual objects.
     */
    public void testAliases() throws Exception
    {
        deleteTestAliases();

        // create an alias for each MBean
        final ObjectName[] names = mProxy.mbeanFind(new String[]
                {
                    StandardAliases.ALL_ALIAS
                });
        final int numNames = names.length;

        // create  test alias for each existing MBean
        for (int i = 0; i < numNames; ++i)
        {
            final String aliasName = ALIAS_BASE + (i + 1);
            mProxy.createAlias(aliasName, names[i].toString());
        }

        // now verify that each of them resolves correctly
        for (int i = 0; i < numNames; ++i)
        {
            final String aliasName = ALIAS_BASE + (i + 1);

            final String aliasValue = mProxy.getAliasValue(aliasName);
            if (aliasValue == null)
            {
                fail("can't resolve alias after creating it: " + aliasName);
            }

            if (!names[i].toString().equals(aliasValue))
            {
                fail("alias does not resolve to value it was created with: " + aliasName);
            }
        }

        // create an alias consisting of all aliases
        final String ALL_ALIASES_NAME = ALIAS_BASE + "all";
        final String[] aliases = mProxy.listAliases(false);
        final String allAliases = ArrayStringifier.stringify(aliases, " ");
        mProxy.createAlias(ALL_ALIASES_NAME, allAliases);

        // create a recursive alias
        String allAliasesName = ALL_ALIASES_NAME;
        for (int i = 0; i < 5; ++i)
        {
            mProxy.createAlias(allAliasesName + i, allAliasesName);
            allAliasesName = allAliasesName + i;
        }

        // verify that the alias to all of them produces the same set of names as we started with
        final ObjectName[] resolvedNames = mProxy.resolveTargets(new String[]
                {
                    allAliasesName
                });
        //p( "all aliases = " + ArrayStringifier.stringify( resolvedNames, "\n" ) );
        if (resolvedNames.length != numNames)
        {
            fail("alias resolution produce wrong number of results");
        }

        deleteTestAliases();
    }


    private void verifySetup(CLISupportMBeanProxy proxy) throws Exception
    {
        // must be at least one MBean
        final ObjectName[] all = proxy.resolveTargets(ALL_TARGETS);
        assert (all.length != 0);

        // verify that the AliasMgr and CLI are available.
        final String[] aliases = proxy.listAliases(false);
        assert (aliases.length != 0);

        // verify that required aliases are in place
        assert (proxy.getAliasValue(ALL_ALIAS) != null);
        assert (proxy.getAliasValue(SIMPLE_TESTEE_ALIAS) != null);
        assert (proxy.getAliasValue(SUPPORT_TESTEE_ALIAS) != null);


    }


    private MBeanServer createAgent()
    {
        return (MBeanServerFactory.createMBeanServer("Test"));
    }


    private void registerMBean(MBeanServer conn, Object mbean, String name)
            throws Exception
    {
        conn.registerMBean(mbean, new ObjectName(name));
    }
    private final static CLISupportTestee CLI_SUPPORT_TESTEE = new CLISupportTestee();


    public void setUp() throws Exception
    {
        mServer = createAgent();

        // CLI_SUPPORT_TESTEE is very expensive to create, so we'll always reuse the same one
        registerMBean(mServer, CLI_SUPPORT_TESTEE, CLISupportStrings.CLI_SUPPORT_TESTEE_TARGET);

        registerMBean(mServer, new CLISupportSimpleTestee(),
                CLISupportStrings.CLI_SIMPLE_TESTEE_TARGET);

        final AliasMgr aliasMgr = new AliasMgrImpl(new AliasMgrHashMapImpl());
        aliasMgr.createAlias(ALL_ALIAS, "*");
        aliasMgr.createAlias(SIMPLE_TESTEE_ALIAS, CLISupportStrings.CLI_SIMPLE_TESTEE_TARGET);
        aliasMgr.createAlias(SUPPORT_TESTEE_ALIAS, CLISupportStrings.CLI_SUPPORT_TESTEE_TARGET);

        final CLISupportMBeanImpl cliSupport = new CLISupportMBeanImpl(mServer, aliasMgr);

        mProxy = new CLISupportMBeanProxy(aliasMgr, cliSupport);

        verifySetup(mProxy);
    }


    public void tearDown()
            throws Exception
    {
        mProxy.mbeanUnregister(mProxy.getAliasValue(SUPPORT_TESTEE_ALIAS));
        mProxy.mbeanUnregister(mProxy.getAliasValue(SIMPLE_TESTEE_ALIAS));
        mProxy = null;
        mServer = null;
    }
};

