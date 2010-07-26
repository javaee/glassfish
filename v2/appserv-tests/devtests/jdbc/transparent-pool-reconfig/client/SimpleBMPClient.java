package com.sun.s1asdev.jdbc.transparent_pool_reconfig.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.transparent_pool_reconfig.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.transparent_pool_reconfig.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    static SimpleReporterAdapter stat = new SimpleReporterAdapter();
    static String testSuite = "transparent_pool_reconfig ";


    public static void main(String[] args)
            throws Exception {
        SimpleBMPClient sbc = new SimpleBMPClient();
        sbc.start();

        stat.printSummary();
    }


    public void start() {
        Executor exec = new Executor();
        exec.start();

        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            //
        }

        int totalClients = 10;
        Client clients[] = new Client[totalClients];
        for (int i = 0; i < totalClients; i++) {
            clients[i] = new Client("User", "APP_" + i, false);
        }

        for (int i = 0; i < totalClients; i++) {
            clients[i].start();
        }

        for (int i = 0; i < totalClients; i++) {
            try {
                clients[i].join();
            } catch (InterruptedException e) {
                //do nothing
            }
        }

        try {
            exec.join();
        } catch (Exception e) {

        }

        exec.testFailure();
        exec.reconfigure("User", "APP", false);
        exec.reconfigure("Password", "APP", false);
        try {
            Thread.currentThread().sleep(2000);
        } catch (Exception e) {

        }
        exec.testSuccess();
    }


    private class Client extends Thread {

        private String name;
        private String value;
        private boolean isAttribute;

        private SimpleBMP simpleBMP;


        public Client(String name, String value, boolean attribute) {
            this.name = name;
            this.value = value;
            this.isAttribute = attribute;

            try {
                InitialContext ic = new InitialContext();
                Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
                SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

                simpleBMP = simpleBMPHome.create();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            if (simpleBMP != null) {
                try {
                    System.out.println("Client : Name - " + name + " : Value - " + value);

                    if (isAttribute) {
                        simpleBMP.setAttribute(name, value);
                    } else {
                        simpleBMP.setProperty(name, value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void testFailure() {

        }
    }


    private class Executor extends Thread {

        SimpleBMP simpleBMP;

        public void run() {
            try {
                InitialContext ic = new InitialContext();
                Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
                SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

                simpleBMP = simpleBMPHome.create();
                stat.addDescription("JDBC Statement Timeout Tests");

                System.out.println("Client : calling acquireConnectionsTest()");

                if (simpleBMP.acquireConnectionsTest(false, 30000)) {
                    stat.addStatus(testSuite + " acquireConnectionsTest : ", SimpleReporterAdapter.PASS);
                } else {
                    stat.addStatus(testSuite + " acquireConnectionsTest : ", SimpleReporterAdapter.FAIL);
                }

                System.out.println("Client : completed acquireConnectionsTest()");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void reconfigure(String name, String value, boolean attribute) {
            try {
                if (attribute) {
                    simpleBMP.setAttribute(name, value);
                } else {
                    simpleBMP.setProperty(name, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void testFailure() {
            try {
                if (simpleBMP.acquireConnectionsTest(true, 0)) {
                    stat.addStatus(testSuite + " expect-failure-Test : ", SimpleReporterAdapter.PASS);
                } else {
                    stat.addStatus(testSuite + " expect-failure-Test : ", SimpleReporterAdapter.FAIL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void testSuccess() {
            try {
                if (simpleBMP.acquireConnectionsTest(false, 0)) {
                    stat.addStatus(testSuite + " expect-success-Test : ", SimpleReporterAdapter.PASS);
                } else {
                    stat.addStatus(testSuite + " expect-success-Test : ", SimpleReporterAdapter.FAIL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
