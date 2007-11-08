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
 * $Id: TargetTest.java,v 1.3 2005/12/25 03:43:14 tcfujii Exp $
 */

package com.sun.enterprise.admin.mbeans;

//jdk imports
import java.io.File;

//junit imports
import junit.framework.*;
import junit.textui.TestRunner;

//jmx imports
import javax.management.ObjectName;

//config imports
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

public class TargetTest extends TestCase
{
    public void testTarget() throws Exception
    {
        testTarget(getTargetData(null, TargetType.SERVER, getConfigRef(), 
                    false, "server", true));
        testTarget(getTargetData("server", TargetType.SERVER, getConfigRef(), 
                    false, "server", true));
        testTarget(getTargetData("domain", TargetType.DOMAIN, null, false, 
                    "domain", false));
        testTarget(getTargetData(getConfigRef(), TargetType.CONFIG, 
                    getConfigRef(), false, getConfigRef(), true));
    }

    public void testUnknownTarget()
    {
        try
        {
            Target target = TargetBuilder.INSTANCE.createTarget(
                "abcd", domainContext);
            Assert.assertTrue(false);
        }
        catch (Exception e) {}
        try
        {
            Target target = TargetBuilder.INSTANCE.createTarget(null, null);
            Assert.assertTrue(false);
        }
        catch (Exception e) {}
    }

    void testTarget(TargetData td) throws Exception
    {
        Target target = TargetBuilder.INSTANCE.createTarget(
                                    td.name, domainContext);
        Assert.assertTrue(target != null);
        Assert.assertEquals(td.type, target.getType());
        Assert.assertEquals(getTargetObjectName(td.target, td.type), 
            target.getTargetObjectName(new String[] { getDomainName() }));
        Assert.assertEquals(td.configRef ,target.getConfigRef());
        if (td.checkConfigTarget)
        {
            final ConfigTarget configTarget = target.getConfigTarget();
            Assert.assertTrue(configTarget != null);
            Assert.assertEquals(td.is1ToN, configTarget.is1ToN(target));
        }
    }

    public TargetTest(String name) throws Exception
    {
        super(name);
    }

    protected void setUp()
    {
        try
        {
            domainContext = getConfigContext();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    protected void tearDown()
    {
        domainContext = null;
    }

    public static junit.framework.Test suite()
    {
        TestSuite suite = new TestSuite(TargetTest.class);
        return suite;
    }

    private ConfigContext domainContext;
    private static File domainXml;

    public static void setDomainXml(File xml)
    {
        domainXml = xml;
    }

    public static void main(String args[]) throws Exception
    {
        final TestRunner runner= new TestRunner();
        setDomainXml(new File(args[0]));
        final TestResult result = runner.doRun(TargetTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }

    private ConfigContext getConfigContext() throws ConfigException
    {
        return ConfigFactory.createConfigContext(domainXml.getAbsolutePath());
    }

    private String getDomainName()
    {
        return "testdomain";
    }

    private String getTargetObjectName(String name, TargetType type) 
        throws Exception
    {
        String on = null;
        if (type.equals(TargetType.DOMAIN))
        {
            on = getDomainName() + ":type=domain,category=config";
        }
        else if (type.equals(TargetType.CONFIG))
        {
            on = getDomainName() + ":type=config,category=config,name=" + name;
        }
        else if (type.equals(TargetType.SERVER))
        {
            on = getDomainName() + ":type=server,category=config,name=" + name;
        }
        return on;
    }

    private String getConfigRef()
    {
        return "server-config";
    }

    private TargetData getTargetData(String     name, 
                                     TargetType type, 
                                     String     configRef, 
                                     boolean    is1ToN, 
                                     String     target, 
                                     boolean    checkConfigTarget)
    {
        TargetData td = new TargetData();
        td.name = name;
        td.type = type;
        td.configRef = configRef;
        td.is1ToN = is1ToN;
        td.target = target;
        td.checkConfigTarget = checkConfigTarget;
        return td;
    }

    private class TargetData
    {
        public String       name;
        public TargetType   type;
        public String       configRef;
        public boolean      is1ToN;
        public String       target;
        public boolean      checkConfigTarget;
    }
}
