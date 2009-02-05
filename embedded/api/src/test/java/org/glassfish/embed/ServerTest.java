/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import java.io.File;
import org.apache.catalina.Engine;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jennifer
 */
public class ServerTest {

    public ServerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        EmbeddedInfo info = new EmbeddedInfo();
        info.setServerName("server");
        server = Server.getServer(info.name);

        if(server == null)
            server = new Server(info);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            server.stop();
        } catch (EmbeddedException ee) {

        }
    }

    @Before
    public void setUp() throws EmbeddedException {
        try {
            server.stop();
        } catch (EmbeddedException ee) {

        }
    }

    @After
    public void tearDown() throws EmbeddedException {
        try {
            server.stop();
        } catch (EmbeddedException ee) {

        }
    }

    /* server mustBeStarted */

    /**
     * Test of deploy method, of class Server.
     */
    @Test
    public void testDeploy_File_Success() throws Exception {
        System.out.println("deploy success");
        File archive = SmartFile.sanitize(new File("target/test-classes/simple.war"));

        assertTrue(archive.exists());

        Application app = null;
        server.start();
        try {
            app = server.deploy(archive);
        }
        catch(Exception e) {
            System.out.println("Unexpected Exception: " + e);
            fail("test failed: testDeploy_File_Success");
        }

        assertNotNull(app);
        app.undeploy();
        server.stop();
    }

    @Test(expected=EmbeddedException.class)
    public void testDeploy_File_Fail() throws Exception {
        System.out.println("deploy fail");
        File archive = SmartFile.sanitize(new File("target/test-classes/simple.war"));
        assertTrue(archive.exists());

        Application app = null;
        try {
            app = server.deploy(archive);
        } catch(EmbeddedException ee) {
            System.out.println("Expected Exception: " + ee);
            throw ee;
        } catch (Exception e) {
            System.out.println("Unxpected Exception: " + e);
            fail("test failed: testDeploy_File_Fail");
        }
    }

    @Test
    public void testDeploy_RA_Success() throws Exception {
        File archive = SmartFile.sanitize(new File("target/test-classes/simple.war"));
        ArchiveFactory archiveFactory = server.getHabitat().getComponent(ArchiveFactory.class);
        ReadableArchive a = archiveFactory.openArchive(archive);
        assertTrue(archive.exists());

        ApplicationLifecycle appLife = server.getHabitat().getComponent(ApplicationLifecycle.class);
        ArchiveHandler h = appLife.getArchiveHandler(a);
        File appDir = new File(server.getFileSystem().getApplicationsDir(), a.getName());
        FileUtils.whack(appDir);
        appDir.mkdirs();
        h.expand(a, archiveFactory.createArchive(appDir));
        a.close();
        a = archiveFactory.openArchive(appDir);

        Application app = null;
        server.start();
        try {
            app = server.deploy(a);
        } catch(Exception e) {
            System.out.println("Unexpected Exception: " + e);
            fail("failed test: testDeploy_RA_Success");
        }

        assertNotNull(app);
        app.undeploy();
        server.stop();
    }

    @Test(expected=EmbeddedException.class)
    public void testDeploy_RA_Fail() throws Exception {
        File archive = SmartFile.sanitize(new File("target/test-classes/simple.war"));
        ArchiveFactory archiveFactory = server.getHabitat().getComponent(ArchiveFactory.class);
        ReadableArchive a = archiveFactory.openArchive(archive);

        Application app = null;
        try {
            app = server.deploy(a);
        } catch(EmbeddedException ee) {
            System.out.println("Expected Exception: " + ee);
            throw ee;
        } catch(Exception e) {
            System.out.println("Unexpected Exception: " + e);
            fail("failed test: testDeploy_RA_Fail");
        }
    }

    @Test
    public void testStop_Success() throws Exception {
        server.start();
        try {
            server.stop();
        } catch (Exception e) {
            System.out.println("Unxpected Exception: " + e);
            fail("failed test: testStop_Success");
        }
    }

    @Test(expected=EmbeddedException.class)
    public void testStop_Fail() throws Exception {
        try {
            server.stop();
        } catch(EmbeddedException ee) {
            System.out.println("Expected Exception: " + ee);
            throw ee;
        } catch (Exception e) {
            System.out.println("Unxpected Exception: " + e);
            fail("failed test: testStop_Fail");
        }
    }

    @Test
    public void testGetEngine_Success() throws Exception {
        System.out.println("getEngine Success");
        String expResult = "com.sun.appserv";
        String result = null;
        server.start();
        Engine engine = server.getEngine();
        result = engine.getName();
        assertEquals(expResult, result);
    }

    @Test(expected=EmbeddedException.class)
    public void testGetEngines_Fail() throws Exception {
        System.out.println("getEngines Fail");
        try {
            server.getEngine();
        } catch(EmbeddedException ee) {
            System.out.println("Expected Exception: " + ee);
            throw ee;
        } catch (Exception e) {
            System.out.println("Unxpected Exception: " + e);
            fail("failed test: testGetEngines_Fail");
        }
    }


    private static Server server;

}