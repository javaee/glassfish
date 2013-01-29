package com.acme.ejb32.timer.opallowed;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.naming.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Client {

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    InitialContext context;
    private static String jndiName =
            "java:global/ejb-timer-opallowedApp/ejb-timer-opallowed-ejb/SingletonTimeoutEJB!com.acme.ejb32.timer.opallowed.SingletonTimeout";

    // this injection will be available when appclient
    @EJB(lookup = "java:app/ejb-timer-opallowed-ejb/SingletonTimeoutEJB!com.acme.ejb32.timer.opallowed.SingletonTimeout")
    private static SingletonTimeout singletonTimeout;

    private SingletonTimeout lookupSingletonTimeout() {
        if (context == null)
            return null;
        try {
            return (SingletonTimeout) context.lookup(jndiName);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return null;
    }

    // neither jseClient nor appclient can invoke Timer/TimerHandle
    // currently this is blocked by JIRA19546
    private static void cancelTimer(SingletonTimeout bean, String info) {
        try {
            TimerHandle handle = bean.createTimer(info);
            //Timer t = handle.getTimer();
            //t.cancel();
            stat.addStatus("opallowed " + info + ": ", stat.FAIL);
        } catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus("opallowed " + info + ": ", stat.PASS);
        }
    }

    private String execute(String type, String httpPort) {
        String connection = "http://localhost:" + httpPort + "/ejb-timer-opallowed/VerifyServlet?" + type;

        System.out.println("invoking webclient servlet at " + connection);
        String result = null;

        try {
            URL url = new URL(connection);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println("Processing line: " + line);
                if (line.indexOf("RESULT: PASS") != -1) {
                    result = line;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result != null) {
            System.out.println(result);
        } else {
            System.out.println("FAILURE");
        }

        return result;
    }

    public static void main(String args[]) {
        stat.addDescription("ejb32-timer-opallowed");
        Client c = new Client();
        System.out.println("Waiting for timer enabled");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if ("jseClient".equals(args[0])) {
            SingletonTimeout singletonTimeout1 = c.lookupSingletonTimeout();
            cancelTimer(singletonTimeout1, "jseclient");
        } else if ("appclient".equals(args[0])) {
            cancelTimer(singletonTimeout, "appclient");
        } else if ("webapp".equals(args[0])) {
            // the Servlet will invoke both local and remote interface of the EJB.
            // for local interface, TimerHandle/Timer invocation is permitted.
            // for remote interface, TimerHandle/Timer invocation is forbidden.
            if ("RESULT: PASS".equals(c.execute("ejb", args[1]))) {
                stat.addStatus("opallowed webapp: ", stat.PASS);
            } else {
                stat.addStatus("opallowed webapp: ", stat.FAIL);
            }
        } else if ("pojo".equals(args[0])) {
            try {
                // the TimerHandle/Timer invocation from a helper class is permitted
                SingletonTimeout singleton = c.lookupSingletonTimeout();
                singleton.cancelFromHelper();
                stat.addStatus("opallowed pojo: ", stat.PASS);
            } catch (Throwable t) {
                t.printStackTrace();
                stat.addStatus("opallowed pojo: ", stat.FAIL);
            }
        } else if ("managedbean".equals(args[0])) {
            // the managedbean is injected to servlet
            // and the TimerHandle/Timer invocation from a managedbean is permitted
            if ("RESULT: PASS".equals(c.execute("managedbean", args[1]))) {
                stat.addStatus("opallowed managedbean: ", stat.PASS);
            } else {
                stat.addStatus("opallowed managedbean: ", stat.FAIL);
            }
        } else {
            // no other case
        }


        stat.printSummary("ejb32-timer-opallowed");
    }

    // when running this class through the appclient infrastructure
    public Client() {
        try {
            context = new InitialContext();
        } catch (Exception e) {
            System.out.println("Client : new InitialContext() failed");
            e.printStackTrace();
            stat.addStatus("Client() ", stat.FAIL);
        }
    }

}
