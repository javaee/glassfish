/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.launcher;

import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class GFLauncherTest {

    public GFLauncherTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ClassLoader cl = GFLauncherTest.class.getClassLoader();
        
        File asenv = new File(cl.getResource("config/asenv.bat").toURI());
        installDir = asenv.getParentFile().getParentFile();
        domainsDir = new File(installDir, "domains");
        assertTrue("domain1 -- domain.xml is missing!!", 
                new File(domainsDir, "domain1/config/domain.xml").exists());
        assertTrue("domain2 -- domain.xml is missing!!", 
                new File(domainsDir, "domain2/config/domain.xml").exists());
        assertTrue("domain3 -- domain.xml is missing!!", 
                new File(domainsDir, "domain3/config/domain.xml").exists());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws GFLauncherException {
        launcher = GFLauncherFactory.getInstance(GFLauncherFactory.ServerType.domain);
        info = launcher.getInfo();
        info.setInstallDir(installDir);
        launcher.setMode(GFLauncher.LaunchType.fake);
    }

    @After
    public void tearDown() {
    }


    /**
     * First Test -- Fake Launch the default domain in the default domain dir
     * Since we have more than 1 domain in there -- it should fail!
     */
    @Test(expected=GFLauncherException.class)    
    public void test1() throws GFLauncherException {
        launcher.launch();
    }
    /**
     * Let's fake-launch domain1
     */
    @Test
    public void test2() throws GFLauncherException {
        info.setDomainName("domain1");
        launcher.launch();
        List<String> cmdline = launcher.getCommandLine();

        /* Too noisy, todo figure out how to get it into the test report
        System.out.println("COMMANDLINE:");
        for(String s : cmdline) {
            System.out.println(s);
        }
         */
    }
    /**
     * Let's fake-launch domain2
     */
    @Test
    public void test3() throws GFLauncherException {
        info.setDomainName("domain2");
        launcher.launch();
        List<String> cmdline = launcher.getCommandLine();

        /*
        System.out.println("COMMANDLINE:");
        for(String s : cmdline) {
            System.out.println(s);
        }
         */
    }
    /**
     * Let's fake-launch a domain that doesn't exist
     * it has an XML error in it.
     */
    @Test(expected=GFLauncherException.class)
    public void test4() throws GFLauncherException {
        info.setDomainName("NoSuchDomain");
        launcher.launch();
        List<String> cmdline = launcher.getCommandLine();

        System.out.println("COMMANDLINE:");
        for(String s : cmdline) {
            System.out.println(s);
        }
    }
    /**
     * Let's fake-launch baddomain
     * it has an XML error in it.
     */
    @Test(expected=GFLauncherException.class)
    public void test5() throws GFLauncherException {
        info.setDomainName("baddomain");
        launcher.launch();
        List<String> cmdline = launcher.getCommandLine();

        System.out.println("COMMANDLINE:");
        for(String s : cmdline) {
            System.out.println(s);
        }
    }

    private static File domain1, domain2, domain3, domain4, domain5;
    private static File installDir;
    private static File domainsDir;
    private GFLauncher launcher;
    private GFLauncherInfo info;
}