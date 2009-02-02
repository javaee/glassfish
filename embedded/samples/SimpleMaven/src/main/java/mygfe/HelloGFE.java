
package mygfe;

/**
 *
 * @author bnevins
 */


import java.io.*;
import java.net.*;
import java.util.logging.*;
import org.glassfish.embed.EmbeddedInfo;
import org.glassfish.embed.Server;


/**
 * Hello world!
 *
 */
public class HelloGFE {

    public static void main(String[] args) {
        HelloGFE app = new HelloGFE();
    }

    public HelloGFE() {
        try {
            int port = 7777;
            EmbeddedInfo info = new EmbeddedInfo();
            info.setHttpPort(port);
            System.out.println("Starting GFE on port " + port);
            File simpleWar = new File("simple.war");

            myGF = new Server(info);
            myGF.start();
            myGF.deploy(simpleWar);
            System.out.println("Deployed Simple.  Test with: http://localhost:7777/simple");
            checkApp();
        }
        catch (Exception e) {
            e.printStackTrace();
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
        }
    }

    private Server myGF;
}
