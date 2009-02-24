/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.embed;

import com.sun.enterprise.universal.io.SmartFile;
import java.io.*;
import java.net.Socket;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */

public class DeployWebAppTest {

    public DeployWebAppTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception{
        System.out.println("\n*********  setUpClass in deploywebappTest\n");
        EmbeddedInfo info = new EmbeddedInfo();
        info.setServerName("server");
        myGF = Server.getServer(info.name);

        if(myGF == null)
            myGF = new Server(info);

        try {
            myGF.start();
        } catch (EmbeddedException e) {
            // already started by CommandExecutorTest
        }

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("\n*********  tearClass in deploywebapptest\n");

        myGF.stop();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    //@Test( expected=MiniXmlParserException.class)
    @Test
    public void garbageWar() throws Exception{
        File qqq = new File("xxxxxx");
        
        try {
            myGF.getDeployer().deploy(qqq);
        }
        catch(Exception e) {
            System.out.println("Caught an Exception as Expected: " + e);
            return;
        }
        fail("Did not get expected Exception from deploying garbage!");
    }

    @Test
    public void foo() throws Exception{
        File simpleWar = SmartFile.sanitize(new File("target/test-classes/simple.war"));
        assertTrue(simpleWar.exists());
        System.out.println("Located simple.war");
        
        try {
            myGF.getDeployer().deploy(simpleWar);
        }
        catch(Exception e) {
            System.out.println("Unexpected Exception: " + e);
            fail("Failure deploying SimpleWar");
        }
        
        try {
            Socket socket = new Socket("localhost", 4848);
            Socket socket2 = new Socket("localhost", 8080);
            assertTrue(socket.isConnected());
            assertTrue(socket2.isConnected());
            socket.close();
            socket2.close();
        } catch(Exception e) {
            fail(e.getLocalizedMessage());
        }

        System.out.println("Simple War deployed and undeployed OK...");

        myGF.setListings(true);

    }
    
    private static Server myGF;
}
