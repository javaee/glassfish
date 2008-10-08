/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package smoke;

import java.io.*;
import org.glassfish.embed.*;
import org.glassfish.embed.AppServer;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Important Note:
 *  Need a negative test case for bad port number -- or any initialization failure
 * of AppServer.  Currently no exception is thrown back!!!!!
 * @author bnevins
 */
public class SmokeTest {
    @BeforeClass
    public static void setUpClass() {
        // get the war files from the src/test/resources dir....
        simpleWar = new File(SmokeTest.class.getClassLoader().getResource("simple.war").getPath());
        System.out.println("simpleWar= " + simpleWar);
        assertTrue(simpleWar.exists());
        banner("Starting SmokeTests for GFE-All");
    }
    @AfterClass
    public static void tearDownClass() throws Exception {
        banner("Finished SmokeTests for GFE-All");
    }

    @Test
    public void deploySimpleWar() {
        try {
            File qqq = new File("target/test-classes/simple.war");
            
            if(!qqq.exists())
                qqq = new File("smoketests/target/test-classes/simple.war");
            
            assertTrue(qqq.exists());
            AppServer myGF = new AppServer(9999);
            App app = myGF.deploy(qqq);
            
            // TODO -- get output at port 9999
            
            myGF.stop();
        }
        catch(Exception e) {
            fail("Got an Exception: " + e);
        }
    }
    private static void banner(String s) {
        System.out.println("\n");
        System.out.println("**************************************************");
        System.out.println("****  " + s); 
        System.out.println("**************************************************");
        System.out.println("\n");
    }
    private static File simpleWar;
}



    
    /*
     public static void setUpClass() {
        try {
            banner("Starting SmokeTests for GFE-All");
        System.out.println("Starting AppServer on port 9999");
        myGF = new AppServer(999999);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("TearDownClass Here!");
        System.out.println("Stopping AppServer...");
        myGF.stop();
    }
    
    
    @Before
    public void setUp() throws Exception {
        banner("Setup Here");
    }

    @After
    public void tearDown() throws Exception {
        banner("Teardown Here");
    }
*/
    
    //@Test (expected=IllegalArgumentException.class)
    /*
    @Test
    public void badPortNumber() {
        try {
            
            // oops!  GF Core does a System.exit() on myGF.stop() !!!!
            // Need to fork a VM for EVERY test  -- or use a different file for negative tests...
            AppServer myGF = new AppServer(65999);
            banner("NEED to change core API --> this should be a FAT error!");
            myGF.stop();
        }
        catch(IllegalArgumentException e) {
        }
        
        catch(Exception e) {
        }
    }
    */
    
    /* 
     * Another negative test -- this triggeres a stop in the "GF Cloud" which
     * does a System.exit()
     *
    
    @Test
    public void badWar() throws Exception{
        File qqq = new File("xxxxxx");
        AppServer myGF = new AppServer(9999);
        App app = null;
        
        try {
            app = myGF.deploy(qqq);
        }
        catch(Exception e) {
            System.out.println("Caught an Exception as Expected: " + e);
            return;
        }
        fail("Did not get expected Exception from deploying garbage!");
    }
    */
    
    


