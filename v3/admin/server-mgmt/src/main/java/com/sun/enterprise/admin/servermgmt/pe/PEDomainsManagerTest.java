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

package com.sun.enterprise.admin.servermgmt.pe;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import junit.framework.TestResult;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import com.sun.enterprise.admin.servermgmt.*;

public class PEDomainsManagerTest extends TestCase
{
    public void testCreate()
    {
        assertNotNull("Couldnt create PEDomainManager", 
                    new PEDomainsManager());
    }

    public void testValidate() throws Exception
    {
        final Map domainConfig = getDomainConfig();
        final PEDomainsManager mgr = new PEDomainsManager();
        mgr.validate(getDomainName(), domainConfig);
    }

    public void testCreateFileLayout()
    {
        final Map domainConfig = getDomainConfig();
        final PEDomainsManager mgr = new PEDomainsManager();
        mgr.createFileLayout(getDomainName(), domainConfig);

        final String domainRoot = 
            (String)domainConfig.get(DomainConfig.K_DOMAINS_ROOT);
        final String domainName = getDomainName();
        assertTrue("Domain dir not created",  
            new File(domainRoot, domainName).exists());

        final File repositoryRoot = 
            getFileLayout().getRepositoryRoot(domainName, getInstanceName());
        assertTrue("Repository dir not created", repositoryRoot.exists());

        final File repositoryBackup = 
            getFileLayout().getRepositoryBackupRoot(domainName, 
                                                    getInstanceName());
        assertTrue("Repository backup dir not created", 
            repositoryBackup.exists());
    }

    public void testCreateDomainXml() throws Exception
    {
        final PEDomainsManager mgr = new PEDomainsManager();
        mgr.createDomainXml(getDomainName(), getDomainConfig());
        final PEFileLayout layout = getFileLayout();
        final String domainName = getDomainName();
        final File domainXml = layout.getDomainConfigFile(domainName, 
                                                          getInstanceName());
        final File domainXmlBackup = 
            layout.getDomainConfigBackupFile(domainName, getInstanceName());
        assertTrue("Domain xml not created", domainXml.exists());
        assertTrue("Domain xml backup not created", domainXmlBackup.exists());
//         assertTrue("Invalid domain xml", isValidXml(domainXml));
    }

    private boolean isValidXml(File xml)
    {
        boolean isValid = true;
        try
        {
            InputSource is = new InputSource(new FileInputStream(xml));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.parse(is);
        }
        catch (Exception e) { e.printStackTrace(); isValid = false; }
        return isValid;
    }

    public void testCreateScripts() throws Exception
    {
        final PEDomainsManager mgr = new PEDomainsManager();
        mgr.createScripts(getDomainName(), getDomainConfig());
        final PEFileLayout layout = getFileLayout();
        final File startServ = layout.getStartServ(getDomainName(), 
                                                   getInstanceName());
        final File stopServ = layout.getStopServ(getDomainName(), 
                                                 getInstanceName());
        assertTrue("startserv not created", startServ.exists());
        assertTrue("stopserv not created", stopServ.exists());
    }

    public void testCreateServerPolicyFile() throws Exception
    {
        final PEDomainsManager mgr = new PEDomainsManager();
        mgr.createServerPolicyFile(getDomainName(), getDomainConfig());
        assertTrue("Policy file not created", 
            getFileLayout().getPolicyFile(getDomainName(), 
                                          getInstanceName()).exists());
    }

    public void testCreateAccXml() throws Exception
    {
        final PEDomainsManager mgr = new PEDomainsManager();
        mgr.createAppClientContainerXml(getDomainName(), getDomainConfig());
        assertTrue("Acc Xml file not created", 
            getFileLayout().getAppClientContainerXml(getDomainName(), 
                                            getInstanceName()).exists());
    }

    public void testCreateDefaultWebXml() throws Exception
    {
        final PEDomainsManager mgr = new PEDomainsManager();
        mgr.createDefaultWebXml(getDomainName(), getDomainConfig());
        assertTrue("Default Web Xml file not created", 
            getFileLayout().getDefaultWebXml(getDomainName(), 
                                            getInstanceName()).exists());
    }

    private String getDomainName()
    {
        return "domain5";
    }

    private String getInstanceName()
    {
        return "server";
    }

    private PEFileLayout getFileLayout()
    {
        return new PEFileLayout(getInstallRoot(), getDomainRoot(),
            getDomainName());
    }

    private static String getInstallRoot()
    {
        File f = new File(System.getProperty("java.io.tmpdir"), 
                          System.getProperty("user.name", "install"));
        f.mkdir();
        f.deleteOnExit();
        return f.getAbsolutePath();
    }

    private String getDomainRoot()
    {
        File f = new File(getInstallRoot(), "domains");
        f.mkdir();
        f.deleteOnExit();
        return f.getAbsolutePath();
    }

    private Map getDomainConfig()
    {
        final Map domainConfig = new HashMap();
        domainConfig.put(DomainConfig.K_INSTALL_ROOT, getInstallRoot());
        domainConfig.put(DomainConfig.K_DOMAINS_ROOT, getDomainRoot());
        domainConfig.put(DomainConfig.K_HOST_NAME, "surya10");
        domainConfig.put(DomainConfig.K_ADMIN_PORT, Integer.valueOf(8888));
        domainConfig.put(DomainConfig.K_INSTANCE_PORT, Integer.valueOf(8889));
        domainConfig.put(DomainConfig.K_ORB_LISTENER_PORT, Integer.valueOf(1025));
        domainConfig.put(DomainConfig.K_JAVA_HOME, getDomainRoot());
        domainConfig.put(DomainConfig.K_JMS_PASSWORD, "admin1");
        domainConfig.put(DomainConfig.K_JMS_USER, "admin1");
        domainConfig.put(DomainConfig.K_JMS_PORT, Integer.valueOf(7677));
        domainConfig.put(DomainConfig.K_HTTP_SSL_PORT, Integer.valueOf(8181));
        domainConfig.put(DomainConfig.K_IIOP_SSL_PORT, Integer.valueOf(1060));
        domainConfig.put(DomainConfig.K_IIOP_MUTUALAUTH_PORT,
                         Integer.valueOf(1061));

        return domainConfig;
    }

    public PEDomainsManagerTest(String name) throws Exception {
        super(name);
        String templatesDir = System.getProperty("com.sun.aas.templatesDir");
        if (templatesDir == null)
        {
            throw new Exception("com.sun.aas.templatesDir is null");
        }
        if (!new File(templatesDir).exists())
        {
            throw new Exception(templatesDir + " Doesnot exist");
        }
        File dest = getFileLayout().getTemplatesDir();
        createClean(dest);
        copy(new File(templatesDir), dest);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    private void nyi() {
        fail("Not yet implemented");
    }

    public static junit.framework.Test suite(){
        TestSuite suite = new TestSuite(PEDomainsManagerTest.class);
        return suite;
    }

    public static void main(String args[]) throws Exception {
		final TestRunner runner= new TestRunner();
		final TestResult result = runner.doRun(PEDomainsManagerTest.suite(), false);
		System.exit(result.errorCount() + result.failureCount());
    }

    static void createClean(File f) throws IOException
    {
        f.delete();
        f.mkdirs();
        if (!f.exists())
        {
            throw new IOException("Could not create " + f.getAbsolutePath());
        }
    }

    static void copy(File templatesDir, File dest) throws IOException
    {
        File[] fa = templatesDir.listFiles();
        for (int i = 0; i < fa.length; i++)
        {
            copyFile(fa[i], dest);
        }
    }

    static void copyFile(File src, File destDir) throws IOException
    {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(
            new File(destDir, src.getName()));
        try
        {
            byte[] buf = new byte[1024];
            int len = 0;
            while (len != -1) 
            {
                len = inStream.read(buf, 0, buf.length);
                if (len == -1) { break; }
                outStream.write(buf, 0, len);
            }
        }
        finally
        {
            inStream.close();
            outStream.close();
        }
    }
}
