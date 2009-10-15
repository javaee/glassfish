/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/support/AliasMgrTest.java,v 1.2 2003/11/21 22:15:42 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/21 22:15:42 $
 */
package com.sun.cli.jmxcmd.support;

/*
This MBean must be modified to store its aliases within domain.xml.  For now, it uses
an internal implementation.
 */
public final class AliasMgrTest extends junit.framework.TestCase
{

    public AliasMgrTest()
    {
    }


    public void attemptInvalidName(String name)
    {
        try
        {
            create().createAlias(name, "bad name");
            fail("expected alias to fail: \"" + name + "\"");
        }
        catch (Exception e)
        {
            // good, we expected to get here
        }
    }
    public final static String LEGAL_CHARS = "-_." +
            "0123456789" +
            "abcdefghijklmnopqrstuvwxyz" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public final static String ILLEGAL_CHARS =
            "!@#$%^&*()+=" +
            "~`{}[]:;\"\'<>,?/" +
            "\n\r\t";


    AliasMgrMBean create()
    {
        return (new AliasMgrImpl(new AliasMgrHashMapImpl()));
    }


    public void testEmptyName()
            throws Exception
    {
        attemptInvalidName("");
    }


    public void testIllegalChars()
    {
        for (int i = 0; i < ILLEGAL_CHARS.length(); ++i)
        {
            final char theChar = ILLEGAL_CHARS.charAt(i);

            attemptInvalidName("" + theChar);
            attemptInvalidName("x" + theChar + "y");
        }
    }


    public void testLegalChars()
            throws Exception
    {
        final AliasMgrMBean aliasMgr = create();

        aliasMgr.createAlias(LEGAL_CHARS, "test");

        for (int i = 0; i < LEGAL_CHARS.length(); ++i)
        {
            final char theChar = LEGAL_CHARS.charAt(i);

            aliasMgr.createAlias("" + theChar, "test");
        }
    }
    final String TEST = "test.";
    final String TEST_VALUE = "test value";


    public void testLifecycle()
            throws Exception
    {
        final AliasMgrMBean aliasMgr = create();

        aliasMgr.createAlias(TEST, TEST_VALUE);
        assertEquals(TEST_VALUE, aliasMgr.getAliasValue(TEST));

        aliasMgr.deleteAlias(TEST);
        assertEquals(null, aliasMgr.getAliasValue(TEST));
    }


    public void testList()
            throws Exception
    {
        final AliasMgrMBean aliasMgr = create();
        assertEquals(0, aliasMgr.listAliases(true).length);
        assertEquals(0, aliasMgr.getAliases().length);

        aliasMgr.createAlias(TEST, TEST_VALUE);
        assertEquals(1, aliasMgr.listAliases(true).length);
        assertEquals(1, aliasMgr.listAliases(false).length);
        assertEquals(1, aliasMgr.getAliases().length);

        String[] listedAliases = aliasMgr.listAliases(false);
        assertEquals(TEST, listedAliases[ 0]);

        listedAliases = aliasMgr.listAliases(true);
        assertEquals(TEST + "=" + TEST_VALUE, listedAliases[ 0]);
    }
}








