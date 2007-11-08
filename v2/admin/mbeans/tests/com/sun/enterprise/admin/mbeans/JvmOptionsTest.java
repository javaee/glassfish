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
 * $Id: JvmOptionsTest.java,v 1.3 2005/12/25 03:43:13 tcfujii Exp $
 */

package com.sun.enterprise.admin.mbeans;

import java.util.*;

//junit imports
import junit.framework.*;
import junit.textui.TestRunner;

/**
 * Depends on 
 * junit.jar, admin/mbeans.jar, admin-core/util.jar, appserv-commons.jar
 */
public class JvmOptionsTest extends TestCase
{
    public void testCreateNull() throws InvalidJvmOptionException
    {
        try
        {
            JvmOptions options = new JvmOptions(null);
            Assert.assertTrue(false);
        }
        catch (IllegalArgumentException iae)
        {
            //ok
        }
    }

    public void testZeroLength() throws InvalidJvmOptionException
    {
        JvmOptions options = new JvmOptions(new String[0]);
        Assert.assertTrue(Arrays.equals(new String[0], options.getJvmOptions()));
    }

    public void testCreateJvmOptions() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y", "-a=b"};
        JvmOptions options = new JvmOptions(sa);
        Assert.assertTrue(Arrays.equals(sa, options.getJvmOptions()));

        sa = new String[] {"-x=y -a=b", "-a=b"};
        options = new JvmOptions(sa);
        Assert.assertTrue(Arrays.equals(sa, options.getJvmOptions()));

        sa = new String[] {"-x=y -x=y"};
        options = new JvmOptions(sa);
        Assert.assertTrue(Arrays.equals(
            new String[] {"-x=y"}, options.getJvmOptions()));
    }

    public void testCreateOptionNull() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y", null};
        try
        {
            JvmOptions options = new JvmOptions(sa);
            Assert.assertTrue(false);
        }
        catch (IllegalArgumentException iae)
        {
            //ok
        }
    }

    public void testAddJvmOptionsNull() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y", "-a=b"};
        JvmOptions options = new JvmOptions(sa);
        try
        {
            options.addJvmOptions(null);
            Assert.assertTrue(false);
        }
        catch (IllegalArgumentException iae)
        {
            Assert.assertTrue(Arrays.equals(sa, options.getJvmOptions()));
        }
    }

    public void testAddNullJvmOption() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y", "-a=b"};
        JvmOptions options = new JvmOptions(sa);
        try
        {
            options.addJvmOptions(new String[] {null});
            Assert.assertTrue(false);
        }
        catch (IllegalArgumentException iae)
        {
            Assert.assertTrue(Arrays.equals(sa, options.getJvmOptions()));
        }
    }

    public void testAddJvmOptionsZeroLength() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y", "-a=b"};
        JvmOptions options = new JvmOptions(sa);
        options.addJvmOptions(new String[0]);
        Assert.assertTrue(Arrays.equals(sa, options.getJvmOptions()));
    }

    public void testAddJvmOptions() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y", "-a=b"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.addJvmOptions(new String[] {"-c=d"});
        Assert.assertTrue(invalid.length == 0);
        Assert.assertTrue(Arrays.equals(new String[] {"-x=y", "-a=b", "-c=d"}, 
            options.getJvmOptions()));
    }
    
    public void testAddDuplicateJvmOptions0() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.addJvmOptions(new String[] {"-x=y"});
        Assert.assertTrue(Arrays.equals(new String[] {"-x=y"}, invalid));
        Assert.assertTrue(Arrays.equals(new String[] {"-x=y"}, 
            options.getJvmOptions()));
    }

    public void testAddDuplicateJvmOptions1() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y", "-a=b"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.addJvmOptions(new String[] {"-a=b"});
        Assert.assertTrue(Arrays.equals(new String[] {"-a=b"}, invalid));
        Assert.assertTrue(Arrays.equals(new String[] {"-x=y", "-a=b"}, 
            options.getJvmOptions()));
    }

    public void testAddDuplicateJvmOptions2() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y -a=b"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.addJvmOptions(new String[] {"-a=b"});
        Assert.assertTrue(Arrays.equals(new String[] {"-a=b"}, invalid));
        Assert.assertTrue(Arrays.equals(new String[] {"-x=y -a=b"}, 
            options.getJvmOptions()));
    }

    public void testAddDuplicateJvmOptions3() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y -a=b"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.addJvmOptions(new String[] {"-a=b", "-c=d"});
        Assert.assertTrue(Arrays.equals(new String[] {"-a=b"}, invalid));
        Assert.assertTrue(Arrays.equals(new String[] {"-x=y -a=b", "-c=d"}, 
            options.getJvmOptions()));
    }

    public void testAddDuplicateJvmOptions4() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y -a=b"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.addJvmOptions(new String[] {"-a=b -c=d"});
        Assert.assertTrue(invalid.length == 0);
        Assert.assertTrue(Arrays.equals(new String[] {"-x=y -a=b", "-a=b -c=d"}, 
            options.getJvmOptions()));
    }

    public void testDeleteJvmOptionsNull() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y"};
        JvmOptions options = new JvmOptions(sa);
        try
        {
            options.deleteJvmOptions(null);
        }
        catch (IllegalArgumentException iae)
        {
            Assert.assertTrue(Arrays.equals(sa, options.getJvmOptions()));
        }
    }

    public void testDeleteNullJvmOption() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y"};
        JvmOptions options = new JvmOptions(sa);
        try
        {
            options.deleteJvmOptions(new String[]{null});
        }
        catch (IllegalArgumentException iae)
        {
            Assert.assertTrue(Arrays.equals(sa, options.getJvmOptions()));
        }
    }

    public void testDeleteJvmOptionsZeroLength() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y", "-a=b"};
        JvmOptions options = new JvmOptions(sa);
        options.deleteJvmOptions(new String[0]);
        Assert.assertTrue(Arrays.equals(sa, options.getJvmOptions()));
    }

    public void testDeleteJvmOptions() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.deleteJvmOptions(new String[]{"-x=y"});
        Assert.assertTrue(invalid.length == 0);
        Assert.assertTrue(Arrays.equals(
            new String[0], options.getJvmOptions()));
    }

    public void testDeleteJvmOptions1() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y", "-a=b"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.deleteJvmOptions(new String[]{"-x=y"});
        Assert.assertTrue(invalid.length == 0);
        Assert.assertTrue(Arrays.equals(
            new String[]{"-a=b"}, options.getJvmOptions()));
    }

    public void testDeleteJvmOptions2() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y -a=b"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.deleteJvmOptions(new String[]{"-x=y"});
        Assert.assertTrue(invalid.length == 0);
        Assert.assertTrue(Arrays.equals(
            new String[]{"-a=b"}, options.getJvmOptions()));
    }

    public void testDeleteJvmOptions3() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y -a=b -c=d"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.deleteJvmOptions(new String[]{"-x=y", "-c=d"});
        Assert.assertTrue(invalid.length == 0);
        Assert.assertTrue(Arrays.equals(
            new String[]{"-a=b"}, options.getJvmOptions()));
    }

    public void testDeleteJvmOptions4() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y -a=b", "-a=b -c=d"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.deleteJvmOptions(new String[]{"-x=y", "-c=d"});
        Assert.assertTrue(invalid.length == 0);
        Assert.assertTrue(Arrays.equals(
            new String[]{"-a=b"}, options.getJvmOptions()));
    }

    public void testDeleteJvmOptions5() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.deleteJvmOptions(new String[]{"-c=d"});
        Assert.assertTrue(Arrays.equals(invalid, new String[]{"-c=d"}));
        Assert.assertTrue(Arrays.equals(
            new String[]{"-x=y"}, options.getJvmOptions()));
    }

    public void testDeleteJvmOptions6() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y", "-a=b"};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.deleteJvmOptions(new String[]{"-a=b", "-c=d"});
        Assert.assertTrue(Arrays.equals(invalid, new String[]{"-c=d"}));
        Assert.assertTrue(Arrays.equals(
            new String[]{"-x=y"}, options.getJvmOptions()));
    }

    public void testDeleteJvmOptions7() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y "};
        JvmOptions options = new JvmOptions(sa);
        String[] invalid = options.deleteJvmOptions(new String[]{"-a=b"});
        Assert.assertTrue(Arrays.equals(invalid, new String[]{"-a=b"}));
        Assert.assertTrue(Arrays.equals(
            new String[]{"-x=y"}, options.getJvmOptions()));
    }

    public void testJvmOptions() throws InvalidJvmOptionException
    {
        String[] sa = new String[] {"-x=y"};
        JvmOptions options = new JvmOptions(sa);

        options.addJvmOptions(new String[]{"-a=b", "-c=d"});
        Assert.assertTrue(Arrays.equals(
            new String[]{"-x=y", "-a=b", "-c=d"}, options.getJvmOptions()));

        options.addJvmOptions(new String[]{"-a=b -c=d"});
        Assert.assertTrue(Arrays.equals(
            new String[]{"-x=y", "-a=b", "-c=d", "-a=b -c=d"}, options.getJvmOptions()));
        String[] invalid = options.deleteJvmOptions(new String[]{"-a=b"});
        Assert.assertTrue(invalid.length == 0);
    }

    public void testJvmOptionsElement() throws InvalidJvmOptionException
    {
        JvmOptionsElement e = new JvmOptionsElement("-x=y");
        Assert.assertFalse(e.hasNext());
        JvmOptionsElement next = new JvmOptionsElement("-a=b");
        e.setNext(next);
        Assert.assertTrue(e.hasNext());
        Assert.assertEquals(next, e.next());
        JvmOptionsElement next1 = new JvmOptionsElement("-c=d");
        next.setNext(next1);
        Assert.assertEquals(next1, e.next().next());
        JvmOptionsElement next2 = new JvmOptionsElement("-a=b -c=d");
        next1.setNext(next2);

        boolean b = e.deleteJvmOption("-a=b");
        Assert.assertTrue(b);
    }

    public void testCreateInValidJvmOptions()
    {
        try
        {
            JvmOptions options = new JvmOptions(new String[] {"option_with_no_dash"});
        }
        catch (InvalidJvmOptionException e)
        {
            //ok
        }
    }

    public void testAddInValidJvmOptions() throws InvalidJvmOptionException
    {
        JvmOptions options = new JvmOptions(new String[] {"-option_with_dash"});
        try
        {
            options.addJvmOptions(new String[] {"option_with_no_dash"});
        }
        catch (InvalidJvmOptionException e)
        {
            //ok
        }
    }

    public void testDeleteInValidJvmOptions() throws InvalidJvmOptionException
    {
        JvmOptions options = new JvmOptions(new String[] {"-option_with_dash"});
        options.deleteJvmOptions(new String[] {"option_with_no_dash"});
        Assert.assertTrue(Arrays.equals(
            new String[] {"-option_with_dash"}, options.getJvmOptions()));
    }

    public void testQuotedOptions() throws InvalidJvmOptionException
    {
        String[] options = new String[] {"-Dfile=\"a b\""};
        JvmOptions jvmOptions = new JvmOptions(options);
        Assert.assertTrue(Arrays.equals(options, jvmOptions.getJvmOptions()));
        jvmOptions.deleteJvmOptions(options);

        try {
            new JvmOptions(new String[] {"-Dfile=\""});
            Assert.assertTrue(false);
        } catch (InvalidJvmOptionException e) {}

        try {
            new JvmOptions(new String[] {"-Dx=\"a\" -Dy=\"b"});
            Assert.assertTrue(false);
        } catch (InvalidJvmOptionException e) {}

        try {
            new JvmOptions(new String[] {"-Dx=\"a\" -Dy=\"b"});
            Assert.assertTrue(false);
        } catch (InvalidJvmOptionException e) {}
    }

    public void testAppServerJvmOptions()
    {
        String[] options = new String[] {
            "-client",
            "-Djava.endorsed.dirs=${com.sun.aas.installRoot}/lib/endorsed",
            "-Djava.security.policy=${com.sun.aas.instanceRoot}/config/server.policy",
            "-Djava.security.auth.login.config=${com.sun.aas.instanceRoot}/config/login.conf",
            "-Dsun.rmi.dgc.server.gcInterval=3600000",
            "-Dcom.sun.web.console.appbase=/${com.sun.aas.installRoot}/lib/install/applications/com_sun_web_ui",
            "-Xmx512m",
            "-Djavax.net.ssl.keyStore=${com.sun.aas.instanceRoot}/config/keystore.jks",
            "-Djavax.net.ssl.trustStore=${com.sun.aas.instanceRoot}/config/cacerts.jks",
            "-Djava.ext.dirs=${com.sun.aas.javaRoot}/jre/lib/ext${path.separator}${com.sun.aas.instanceRoot}/lib/ext",
            "-Djdbc.drivers=com.pointbase.jdbc.jdbcUniversalDriver",
            "-Djavax.xml.transform.TransformerFactory=org.apache.xalan.xsltc.trax.TransformerFactoryImpl"
        };
        try
        {
            JvmOptions jvmOptions = new JvmOptions(options);
            Assert.assertTrue(Arrays.equals(options, jvmOptions.getJvmOptions()));
        }
        catch (InvalidJvmOptionException e)
        {
            Assert.assertTrue(false);
        }
    }

    private void printOptions(JvmOptions o)
    {
        String[] options = o.getJvmOptions();
        System.out.println("============================");
        for (int i = 0; i < options.length; i++)
        {
            System.out.println("Option[" + options[i] + "]");
        }
        System.out.println("============================");
    }

    public JvmOptionsTest(String name) throws Exception
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
        TestSuite suite = new TestSuite(JvmOptionsTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception
    {
        final TestRunner runner= new TestRunner();
        final TestResult result = runner.doRun(JvmOptionsTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}