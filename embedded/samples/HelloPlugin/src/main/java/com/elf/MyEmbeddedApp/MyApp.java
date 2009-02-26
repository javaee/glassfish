package com.elf.MyEmbeddedApp;

import java.io.*;
import java.io.File;
import java.net.*;
import java.util.logging.*;
import org.glassfish.embed.EmbeddedInfo;
import org.glassfish.embed.Server;

    
/**
 * Hello world!
 *
 */
public class MyApp {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        MyApp app = new MyApp();
    }

    private MyApp() {
        try {
            EmbeddedInfo info = new EmbeddedInfo();
            info.setHttpPort(8080);
            System.out.println("Starting AppServer on port 8080");
            myGF = new Server(info);
            File simpleWar = new File("simple.war");
            File JSPAppWar = new File("JSPApp.war");
            System.out.println("Exists: " + simpleWar.exists());
            System.out.println("Located simple.war");
            System.out.println("Exists: " + JSPAppWar.exists());
            System.out.println("Located JSPApp.war");
            
            /*
            App simple = myGF.deploy(simpleWar);
            System.out.println("Deployed Simple.  Test with: http://localhost:9999/simple");
            */

            myGF.start();
             myGF.getDeployer().deploy(JSPAppWar);
             myGF.getDeployer().deploy(simpleWar);
            System.out.println("Deployed JSP.  Test with: http://localhost:8080/JSPApp/");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void checkApp() {
        try {
            URL url = new URL("http://localhost:12345/JSPApp");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
            in.close();
        }
        catch (Exception ex) {
            Logger.getLogger(MyApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private Server myGF;

	/*
    private void foo() throws EmbeddedException {
        Engine[] engines = myGF.getEngines();
        Engine engine = engines[0];
        Container[] vss = engine.findChildren();

        for(Container c : vss) {
            VirtualServer vs = (VirtualServer) c;
            Container[] webModules = c.findChildren();

            for(int i = 0; i < webModules.length; i++) {
                Container module = webModules[i];
                WebModule webModule = (WebModule)module;
                System.out.println("MODULE#" + i + ": "+ webModule.getClass().getName() + ",  " + webModule.getName());
                System.out.println("Length of module name= " + webModule.getName().length());

                Container[] cc = webModule.findChildren();

                for(Container cx : cc) {
                    System.out.println("SERVLET: " + cx.getClass().getName() + ",  " + cx.getName());
                }
            }
        }
    }
	*/
}
