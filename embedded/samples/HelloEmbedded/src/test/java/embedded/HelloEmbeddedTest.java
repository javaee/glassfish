package embedded;



/**
 *
 * @author bnevins
 */


import java.io.*;
import java.net.*;
import org.glassfish.embed.EmbeddedInfo;
import org.glassfish.embed.Server;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Hello world!
 *
 */
public class HelloEmbeddedTest {
    
    @Test
    public void hello() {
        try {
            int port = 7777;
            EmbeddedInfo info = new EmbeddedInfo();
            info.setHttpPort(port);
            System.out.println("Starting Embedded GlassFish on port " + port);
            File simpleWar = new File("simple.war");

            myGF = new Server(info);
            myGF.start();
            myGF.getDeployer().deploy(simpleWar);
            System.out.println("Deployed Simple.  Test with: http://localhost:7777/simple");
            checkApp();
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("error running Embedded GlassFish");
        }
    }

    private void checkApp() {
        String urlString = "http://localhost:7777/simple";
        try {
            URL url = new URL(urlString);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;

            System.out.println("Accessing web app via this URL: " + urlString);
            System.out.println("Output below with leading stars\n");
            
            while ((inputLine = in.readLine()) != null) {
                System.out.println("****** " + inputLine);
            }
            in.close();
        }
        catch (Exception ex) {
            System.out.println("Error hitting: " + urlString);
            fail("Error  hitting: " + urlString);
        }
    }
    private Server myGF;
}
