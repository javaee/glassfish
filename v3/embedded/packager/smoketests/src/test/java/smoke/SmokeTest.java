/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package smoke;

import org.glassfish.embed.AppServer;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class SmokeTest {
    private static AppServer myGF;
    
    @BeforeClass
    public static void setUpClass() {
        try {
        System.out.println("Starting AppServer on port 9999");
        myGF = new AppServer(9999);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("Stopping AppServer...");
        myGF.stop();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    
    
    
    @Test
    public void hello() {
        System.out.println("HELLO WORLD!!!!");
    }

}
