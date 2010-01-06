/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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








