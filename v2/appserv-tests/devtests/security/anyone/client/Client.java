/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.anyone.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import javax.ejb.EJB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::Anyone test ";
    private static @EJB com.sun.s1asdev.security.anyone.ejb.Hello hello;
    private String host;
    private int port;

    public static void main(String[] args) {
        Client client = new Client(args);
        client.doTest();
    }

    public Client(String[] args) {
        host = (args.length > 0) ? args[0] : "localhost";
        port = (args.length > 1) ? Integer.parseInt(args[1]) : 8080;
    }

    public void doTest() {
        stat.addDescription("security-anyone");

        String description = null;
        System.out.println("Invoking ejb");
        try {
            description = testSuite + " ejb: hello";
            hello.hello("Sun");
            stat.addStatus(description, stat.PASS);  
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(description, stat.FAIL);
        }

        System.out.println("Invoking servlet");
        description = testSuite + " servlet";
        try {
            int count = goGet(host, port, "/security-anyone/servlet");
            if (count == 1) {
                stat.addStatus(description, stat.PASS);
            } else {
                System.out.println("Servlet does not return expected result.");
                stat.addStatus(description, stat.FAIL);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(description, stat.FAIL);
        }
 
        stat.printSummary("security-anyone");
    }

    private static int goGet(String host, int port, String contextPath)
            throws Exception {
        Socket s = new Socket(host, port);

        OutputStream os = s.getOutputStream();
        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("Authorization: Basic amF2YWVlOmphdmFlZQ==\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        int count = 0;
        int lineNum = 0;
        while ((line = bis.readLine()) != null) {
            System.out.println(lineNum + ": " + line);
            if (line.equals("Hello World")) {
                count++;
            }
            lineNum++;
        }

        return count;
    }
}
