package com.acme;

import admin.AdminBaseDevTest;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client extends AdminBaseDevTest {

    public static final String CLUSTER_NAME = "c1";
    public static final String INSTANCE1_NAME = "in1";
    public static final String INSTANCE2_NAME = "in2";
    public static final String XA_RESOURCE = "jdbc/mypool";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        if ("prepare".equals(args[0])) {
            (new Client()).prepare();
        } else if ("deploy".equals(args[0])) {
            (new Client()).deploy(args[1]);
        } else if ("clean".equals(args[0])) {
            (new Client()).clean();
        } else if ("undeploy".equals(args[0])) {
            (new Client()).undeploy(args[1]);
        } else if ("verify".equals(args[0])) {
            (new Client()).verify(args[1], args[2], args[3], args[0]);
        } else {
            System.out.println("Wrong target: " + args[0]);
        }
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for TimerService.getAllTimers()";
    }

    public void prepare() {
        try {
            asadmin("set-log-levels", "javax.enterprise.system.container.ejb=FINE");
            asadmin("create-cluster", CLUSTER_NAME);
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, CLUSTER_NAME+INSTANCE1_NAME);
            asadmin("create-local-instance", "--cluster", CLUSTER_NAME, CLUSTER_NAME+INSTANCE2_NAME);
            asadmin("start-cluster", CLUSTER_NAME);
            asadmin("create-resource-ref", "--target", CLUSTER_NAME, XA_RESOURCE);
            System.out.println("Started cluster.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deploy(String path) {
        try {
            asadmin("set-log-levels", "javax.enterprise.system.container.ejb=FINE", "--target", CLUSTER_NAME);
            asadmin("deploy", "--target", CLUSTER_NAME, path);
            System.out.println("Deployed " + path);
            asadmin("set-log-levels", "javax.enterprise.system.container.ejb=INFO", "--target", CLUSTER_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void verify(String appname, String port1, String port2, String operation) {
        stat.addDescription("ejb-ee-" + operation);

        String expect = "RESULT:8";
        String res = execute(appname, port1, expect);
        boolean success = expect.equals(res);
        stat.addStatus("ejb-ee-" + operation + " " + port1, ((success) ? stat.PASS : stat.FAIL));
        // programmatic timers will be created only on c1in1, 2 persistent and 2 non-persistent
        // therefore when invoking getAllTimers() from c1in2, only persistent timers can be seen
        expect = "RESULT:6";
        res = execute(appname, port2, expect);
        success = expect.equals(res);
        stat.addStatus("ejb-ee-" + operation + " " + port2, ((success) ? stat.PASS : stat.FAIL));
        stat.printSummary("ejb-ee-" + operation);

    }

    public void undeploy(String name) {
        try {
            asadmin("undeploy", "--target", CLUSTER_NAME, name);
            System.out.println("Undeployed " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clean() {
        try {
            asadmin("stop-cluster", CLUSTER_NAME);
            asadmin("delete-local-instance", CLUSTER_NAME+INSTANCE1_NAME);
            asadmin("delete-local-instance", CLUSTER_NAME+INSTANCE2_NAME);
            asadmin("delete-cluster", CLUSTER_NAME);
            System.out.println("Removed cluster " + CLUSTER_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   private String execute(String appname, String port, String expectedResult) {
        String connection = "http://localhost:" + port + "/" + appname + "/VerifyServlet?" + port;

        System.out.println("invoking webclient servlet at " + connection);
        String result=null;

        try {
            URL url = new URL(connection);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();

            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
  
            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println("Processing line: " + line);
                if(line.indexOf(expectedResult)!=-1){
                    result=line;
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

}
