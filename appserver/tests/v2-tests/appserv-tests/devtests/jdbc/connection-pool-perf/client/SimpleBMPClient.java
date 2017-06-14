package com.sun.s1asdev.jdbc.statementwrapper.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.util.TimerTask;
import java.util.Timer;

import javax.naming.*;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


public class SimpleBMPClient {
 
    private static long count = 0;
    private static final Object lock = new Integer(10);
    private long totalIterations = 0;
    private static int no_of_get_connections = 5;

    public static final String NUM_CON_ACQUIRED_COUNT = "numconnacquired";
    public static final int JMX_PORT = 8686;
    public static final String HOST_NAME = "localhost";

    private static        SimpleReporterAdapter stat = new SimpleReporterAdapter();
    private static            String testSuite = "";


    public static void main(String[] args)
            throws Exception {
        int no_of_threads = 20;
        long duration = 5 * 60 * 1000;
        if (args != null && args.length > 0) {
            try {
                no_of_threads = Integer.parseInt(args[0]);
                System.out.println("Setting no of threads to : " + args[0]);
            } catch (Exception e) {
            }
        }

        if (args != null && args.length > 1) {
            try {
                duration = Integer.parseInt(args[1]) * 60 * 1000;
                System.out.println("Setting duration to  " + args[1] + " minutes");
            } catch (Exception e) {
            }
        }

        if (args != null && args.length > 2) {
            try {
                no_of_get_connections = Integer.parseInt(args[2]);
                System.out.println("Setting no-of-get-connections to  " + args[2]);
            } catch (Exception e) {
            }
        }
        final MyThread threads[] = new MyThread[no_of_threads];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new MyThread();
            threads[i].setNoOfGetConnections(no_of_get_connections);
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
//        System.out.println("Started at : " + 

        TimerTask task = new TimerTask() {
            public void run() {
                for (int i = 0; i < threads.length; i++) {
                    threads[i].setExit(true);
                }
              try{
                  Thread.currentThread().sleep(5000);
              }catch(Exception e){
              }
            try{
            long totalCount = count*no_of_get_connections;
//            System.out.println("TOTAL COUNT : " + count*no_of_get_connections);
            long monitoringCount = getMonitorablePropertyOfConnectionPool("ql-jdbc-pool") ;
            System.out.println("-----------------------------------------------------------------------------------");
            System.out.println("Total getConnection() : " + totalCount + ", monitoring-count : " + monitoringCount);
            System.out.println("-----------------------------------------------------------------------------------");
      
            if(totalCount == monitoringCount){
                stat.addStatus(testSuite + " Perf-test : ", stat.PASS);
            }else{
                stat.addStatus(testSuite + " Perf-test : ", stat.FAIL);
            }
            }catch(Exception e){
                e.printStackTrace();
            }
            stat.printSummary();
           
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, duration);
//        System.out.println("Total Count : " + count);
    }
 public static int getMonitorablePropertyOfConnectionPool(String poolName) throws Exception {

        final String urlStr = "service:jmx:rmi:///jndi/rmi://" + HOST_NAME + ":" + JMX_PORT + "/jmxrmi";
        final JMXServiceURL url = new JMXServiceURL(urlStr);

        final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
        final MBeanServerConnection connection = jmxConn.getMBeanServerConnection();

        ObjectName objectName =
                new ObjectName("amx:pp=/mon/server-mon[server],type=jdbc-connection-pool-mon,name=resources/" + poolName);

        javax.management.openmbean.CompositeDataSupport returnValue =
                (javax.management.openmbean.CompositeDataSupport)
                connection.getAttribute(objectName, NUM_CON_ACQUIRED_COUNT);

        return new Integer(returnValue.get("count").toString());
    }


    static class MyThread extends Thread {
        SimpleBMP simpleBMP = null;
        boolean exit = false;
        int no_of_get_connections = 5;

        public void setNoOfGetConnections(int no_of_get_connections) {
            this.no_of_get_connections = no_of_get_connections;
        }

        public void setExit(boolean exit) {
            this.exit = exit;
        }

        public MyThread() throws Exception {
            InitialContext ic = new InitialContext();
            Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
            SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                    javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);
            simpleBMP = simpleBMPHome.create();
        }

        public void runTest() throws Exception {

            long iterations = 0;
            long passed = 0;
            long failed = 0;
            while (!exit) {
                if (simpleBMP.test1(no_of_get_connections)) {
                //if (simpleBMP.test2()) {
                    passed++;
                } else {
                    failed++;
                }
                    synchronized(lock){
                        count++;
                    }
                iterations++;
            }

            //stat.addStatus(testSuite + " statementTest : THREAD :  " + Thread.currentThread().getName(), stat.PASS);
            //stat.addStatus(testSuite + " statementTest : PASS COUNT :  " + passed, stat.PASS);
            //stat.addStatus(testSuite + " statementTest : FAIL COUNT : " + failed, stat.PASS);
            //stat.addStatus(testSuite + " statementTest : TOTAL ITERATIONS : " + iterations, stat.PASS);
            //stat.printSummary();
        }

        public void run() {
            while (!exit) {
                try {
                    runTest();
                } catch (Exception e) {
                }
            }
        }
    }
}
