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
 * $Id: MBeanExceptionFormatterTest.java,v 1.3 2005/12/25 03:43:13 tcfujii Exp $
 */

package com.sun.enterprise.admin.mbeans;

//junit imports
import junit.framework.*;
import junit.textui.TestRunner;

//JMX imports
import javax.management.MBeanException;

public class MBeanExceptionFormatterTest extends TestCase
{
    public void testMBeanException()
    {
        Exception targetEx = new Exception();
        MBeanException mbe = new MBeanException(targetEx);
        Assert.assertEquals(targetEx, mbe.getTargetException());
        Assert.assertEquals(null, mbe.getCause());
        Assert.assertEquals(null, mbe.getMessage());
        Assert.assertEquals(null, mbe.getTargetException().getMessage());

        targetEx = new Exception("actual message");
        mbe = new MBeanException(targetEx);
        mbe.initCause(targetEx);
        Assert.assertEquals(targetEx, mbe.getCause());
        Assert.assertEquals(null, mbe.getMessage());

        mbe = toMBeanException(null, null);
        Assert.assertEquals(null, mbe.getMessage());
        Assert.assertTrue(null != mbe.getCause());
        Assert.assertTrue(null != mbe.getTargetException());

        mbe = toMBeanException(null, "a");
        Assert.assertEquals("a", mbe.getMessage());

        Exception e = new Exception("b", new Exception("c", new Exception("d")));
        mbe = toMBeanException(e, "a");
        Assert.assertEquals("a(b(c(d)))", mbe.getMessage());
        Assert.assertEquals(e, mbe.getCause());
        Assert.assertEquals(e, mbe.getTargetException());

        mbe = toMBeanException(e, null);
        Assert.assertEquals("b(c(d))", mbe.getMessage());

        e = new Exception();
        mbe = toMBeanException(e, "a");
        Assert.assertEquals("a", mbe.getMessage());

        e = new Exception("b", null);
        mbe = toMBeanException(e, "a");
        Assert.assertEquals("a(b)", mbe.getMessage());

        e = new Exception("b", new Exception(null, new Exception("c")));
        mbe = toMBeanException(e, "a");
        Assert.assertEquals("a(b(c))", mbe.getMessage());
    }

    MBeanException toMBeanException(final Exception e, final String msg)
    {
        return MBeanExceptionFormatter.toMBeanException(e, msg);
    }

    public MBeanExceptionFormatterTest(String name) throws Exception
    {
        super(name);
    }

    protected void setUp()
    {
    }

    protected void tearDown()
    {
    }

    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite(MBeanExceptionFormatterTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception
    {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(
                MBeanExceptionFormatterTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}