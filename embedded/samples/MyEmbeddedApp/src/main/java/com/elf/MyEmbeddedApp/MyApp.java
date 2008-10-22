package com.elf.MyEmbeddedApp;

import java.io.File;
import org.glassfish.embed.App;
import org.glassfish.embed.AppServer;

    
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
            System.out.println("Starting AppServer on port 9999");
            myGF = new AppServer(9999);
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
             App jsp = myGF.deploy(JSPAppWar);
            System.out.println("Deployed JSP.  Test with: http://localhost:9999/JSPApp/");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AppServer myGF;
}
