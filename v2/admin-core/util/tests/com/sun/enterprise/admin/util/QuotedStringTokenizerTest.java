/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/*
 * $Id: QuotedStringTokenizerTest.java,v 1.2 2005/12/25 03:53:28 tcfujii Exp $
 */

package com.sun.enterprise.admin.util;

import java.util.StringTokenizer;
import junit.framework.*;
import junit.framework.Assert;

/**
 * Depends on junit.jar, admin-core/util.jsr
 */
public class QuotedStringTokenizerTest extends TestCase
{
    public void testCreate()
    {
        new QuotedStringTokenizer("");
        new QuotedStringTokenizer("a");
        try {
            new QuotedStringTokenizer(null);
            Assert.assertTrue(false);
        } catch (IllegalArgumentException iae) {}
    }

    void _countTokens(String s, int expCount)
    {
        QuotedStringTokenizer tok = new QuotedStringTokenizer(s, null);
        Assert.assertEquals(expCount, tok.countTokens());
    }

    public void testCountTokens()
    {
        _countTokens("", 0);
        _countTokens(" ", 0);
        _countTokens("  ", 0);
        _countTokens("a", 1);
        _countTokens(" a", 1);
        _countTokens(" a ", 1);
        _countTokens("  a ", 1);
        _countTokens("  ab ", 1);
        _countTokens("  a b ", 2);
    }

    public void testCountTokensWithQuotes()
    {
        _countTokens("\"a\"", 1);
        _countTokens("\"a b\"", 1);
        _countTokens(" \"a\" ", 1);
        _countTokens("\"", 1);
        _countTokens("\"\"", 1);
        _countTokens("\" \"", 1);
        _countTokens("\"a\"b", 1);
        _countTokens("\"a\" b", 2);
        _countTokens("\"a\" \"b\"", 2);
        _countTokens("\"a\"\"b\"", 1);
        _countTokens("\" ", 1);
    }

    public void testNextToken1()
    {
        QuotedStringTokenizer tok = new QuotedStringTokenizer("a");
        Assert.assertTrue(tok.hasMoreTokens());
        Assert.assertEquals("a", tok.nextToken());
        Assert.assertFalse(tok.hasMoreTokens());
        try {
            tok.nextToken();
            Assert.assertTrue(false);
        } catch (java.util.NoSuchElementException nsee) {}
    }

    public void testNextToken2()
    {
        QuotedStringTokenizer tok = new QuotedStringTokenizer("");
        Assert.assertFalse(tok.hasMoreTokens());
        try {
            tok.nextToken();
            Assert.assertTrue(false);
        } catch (java.util.NoSuchElementException nsee) {}
    }

    public void testNextToken3()
    {
        QuotedStringTokenizer tok = new QuotedStringTokenizer("a b c d e f");
        Assert.assertTrue(tok.countTokens() == 6);
        tok.nextToken();
        Assert.assertEquals("b", tok.nextToken());
        Assert.assertTrue(tok.hasMoreTokens());
        while (tok.hasMoreTokens())
            tok.nextToken();
        try {
            tok.nextToken();
            Assert.assertTrue(false);
        } catch (java.util.NoSuchElementException nsee) {}
    }

    public void testNextToken4()
    {
        QuotedStringTokenizer tok = new QuotedStringTokenizer("abcdef");
        Assert.assertTrue(tok.countTokens() == 1);
        Assert.assertEquals("abcdef", tok.nextToken());
        tok = new QuotedStringTokenizer("     abcdef       g      ");
        Assert.assertTrue(tok.countTokens() == 2);
        Assert.assertEquals("abcdef", tok.nextToken());
        Assert.assertEquals("g", tok.nextToken());
    }

    public void testNextToken5()
    {
        QuotedStringTokenizer tok = new QuotedStringTokenizer("abc \t d \t\t ef");
        Assert.assertEquals(3, tok.countTokens());
        Assert.assertEquals("abc", tok.nextToken());
        Assert.assertEquals("d", tok.nextToken());
        Assert.assertEquals("ef", tok.nextToken());
    }


    public void testNextTokenWithQuotes()
    {
        QuotedStringTokenizer mtok = new QuotedStringTokenizer("\"\"");
        Assert.assertEquals("\"\"", mtok.nextToken());
        Assert.assertFalse(mtok.hasMoreTokens());

        mtok = new QuotedStringTokenizer("\"");
        Assert.assertEquals("\"", mtok.nextToken());
        Assert.assertFalse(mtok.hasMoreTokens());

        //??
        mtok = new QuotedStringTokenizer("\" ");
        Assert.assertEquals("\" ", mtok.nextToken());
        Assert.assertFalse(mtok.hasMoreTokens());

        //??
        mtok = new QuotedStringTokenizer(" \" ");
        Assert.assertEquals("\" ", mtok.nextToken());
        Assert.assertFalse(mtok.hasMoreTokens());

        mtok = new QuotedStringTokenizer("\t\"\t");
        Assert.assertEquals("\"\t", mtok.nextToken());
        Assert.assertFalse(mtok.hasMoreTokens());

        mtok = new QuotedStringTokenizer("\"a\"");
        Assert.assertEquals("\"a\"", mtok.nextToken());
        Assert.assertFalse(mtok.hasMoreTokens());

        mtok = new QuotedStringTokenizer(" \"a\" ");
        Assert.assertEquals("\"a\"", mtok.nextToken());
        Assert.assertFalse(mtok.hasMoreTokens());

        mtok = new QuotedStringTokenizer("\"a");
        Assert.assertEquals("\"a", mtok.nextToken());
        Assert.assertFalse(mtok.hasMoreTokens());

        mtok = new QuotedStringTokenizer("\"a\" \"b\"");
        Assert.assertEquals("\"a\"", mtok.nextToken());
        Assert.assertEquals("\"b\"", mtok.nextToken());

        mtok = new QuotedStringTokenizer("\"a\"b");
        Assert.assertEquals("\"a\"b", mtok.nextToken());

        //??
        mtok = new QuotedStringTokenizer("\"a ");
        Assert.assertEquals("\"a ", mtok.nextToken());

        mtok = new QuotedStringTokenizer("\"a b\"");
        Assert.assertEquals(1, mtok.countTokens());
        Assert.assertEquals("\"a b\"", mtok.nextToken());
    }

    public void testWithQuoteAsDelimiter()
    {
        QuotedStringTokenizer mtok = new QuotedStringTokenizer("\"\"", "\" ");
        Assert.assertEquals(0, mtok.countTokens());

        mtok = new QuotedStringTokenizer("\"", "\" ");
        Assert.assertEquals(0, mtok.countTokens());

        mtok = new QuotedStringTokenizer("\" ", "\" ");
        Assert.assertEquals(0, mtok.countTokens());

        mtok = new QuotedStringTokenizer(" \" ", "\" ");
        Assert.assertEquals(0, mtok.countTokens());

        mtok = new QuotedStringTokenizer("\t\"\t", "\" ");
        Assert.assertEquals(2, mtok.countTokens());

        mtok = new QuotedStringTokenizer("\"a\"", "\" ");
        Assert.assertEquals("a", mtok.nextToken());
        Assert.assertFalse(mtok.hasMoreTokens());

        mtok = new QuotedStringTokenizer(" \"a\" ", "\" ");
        Assert.assertEquals("a", mtok.nextToken());
        Assert.assertFalse(mtok.hasMoreTokens());

        mtok = new QuotedStringTokenizer("\"a", "\" ");
        Assert.assertEquals("a", mtok.nextToken());
        Assert.assertFalse(mtok.hasMoreTokens());

        mtok = new QuotedStringTokenizer("\"a\" \"b\"", "\" ");
        Assert.assertEquals("a", mtok.nextToken());
        Assert.assertEquals("b", mtok.nextToken());

        mtok = new QuotedStringTokenizer("\"a\"b", "\" ");
        Assert.assertEquals("a", mtok.nextToken());
        Assert.assertEquals("b", mtok.nextToken());

        mtok = new QuotedStringTokenizer("ab", "\" ");
        Assert.assertEquals("ab", mtok.nextToken());
    }

    public void testIsDelimiter()
    {
        QuotedStringTokenizer tok = new QuotedStringTokenizer("");
        Assert.assertTrue(tok.isDelimiter('\t'));
    }

    public void testQuotesInQuotes()
    {
        QuotedStringTokenizer tok = new QuotedStringTokenizer("\"abc\"def g");
        Assert.assertEquals("\"abc\"def", tok.nextToken());
        Assert.assertEquals("g", tok.nextToken());

        tok = new QuotedStringTokenizer("\"abc\"def\"");
        Assert.assertEquals("\"abc\"def\"", tok.nextToken());

        tok = new QuotedStringTokenizer(" \"abc\"def\" ");
        Assert.assertEquals("\"abc\"def\" ", tok.nextToken());
    }

    public void testJavaStringTokenizer()
    {
        StringTokenizer tok = new StringTokenizer("\" ", " ");
        Assert.assertEquals("\"", tok.nextToken());
        tok = new StringTokenizer("a", "\"");
        Assert.assertEquals("a", tok.nextToken());
        tok = new StringTokenizer("\"\"", "\" ");
        Assert.assertEquals(0, tok.countTokens());
        tok = new StringTokenizer("\t\"\t", "\" ");
        Assert.assertEquals(2, tok.countTokens());
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite(QuotedStringTokenizerTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception
    {
        junit.textui.TestRunner.run(suite());
        //junit.swingui.TestRunner.run(suite());
    }
}
