package com.sun.s1asdev.deployment.ejb30.ear.security.client;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import com.sun.s1asdev.deployment.ejb30.ear.security.*;

public class Client {
    private String host;
    private int port;

    public static void main (String[] args) {
        System.out.println("deployment-ejb30-ear-security");
        Client client = new Client(args);
        client.doTest();
    }  
    
    public Client (String[] args) {
        host = (args.length > 0) ? args[0] : "localhost";
        port = (args.length > 1) ? Integer.parseInt(args[1]) : 8080;
    }
    
    private static @EJB Sless sless;
    private static @EJB Sful sful;

    public void doTest() {

        try {

            System.out.println("invoking stateless");
            try {
                System.out.println(sless.hello());
                System.exit(-1);
            } catch(Exception ex) {
                System.out.println("Expected failure from sless.hello()");
            }

            sless.goodMorning();

            try {
                sless.goodBye();
                System.exit(-1);
            } catch(EJBException ex) {
                System.out.println("Expected failure from sless.goodBye()");
            }

            System.out.println("invoking stateful");
            System.out.println(sful.hello());
            System.out.println(sful.goodAfternoon());
            try {
                sful.goodNight();
                System.exit(-1);
            } catch(EJBException ex) {
                System.out.println("Expected failure from sful.goodNight()");
            }

            System.out.println("invoking servlet");
            int count = goGet(host, port, "/deployment-ejb30-ear-security/servlet");
            if (count != 2) {
                System.out.println("Servlet does not return expected result.");
                System.exit(-1);
            }

            System.out.println("test complete");

        } catch(Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }

    	return;
    }

    private static int goGet(String host, int port, String contextPath)
            throws Exception {
        Socket s = new Socket(host, port);

        OutputStream os = s.getOutputStream();
        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("Authorization: Basic ajJlZTpqMmVl\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        int count = 0;
        int lineNum = 0;
        while ((line = bis.readLine()) != null) {
            System.out.println(lineNum + ": " + line);
            int index = line.indexOf("=");
            if (index != -1) {
                String info = line.substring(index + 1);  
                if (info.startsWith("hello")) {
                    count++;
                }
            }
            lineNum++;
        }

        return count;
    }
}
