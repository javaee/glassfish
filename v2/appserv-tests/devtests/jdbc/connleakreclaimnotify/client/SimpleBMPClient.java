package com.sun.s1asdev.jdbc.statementwrapper.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {


    public static int NO_OF_THREADS = 20; 
    public int failedCount = 0;

    SimpleReporterAdapter stat = new SimpleReporterAdapter();
    String testSuite = "ConnectionLeakReclaim ";
    public SimpleBMPClient(){
	execute();
    }

    public static void main(String[] args) {
	new SimpleBMPClient();
    }

    private void execute() {
	stat.addDescription("Connection Leak Reclaim Notify Tests");
        WorkerThread workers[] = new WorkerThread[NO_OF_THREADS];
           for(int i=0; i< workers.length;i++){
               workers[i] = new WorkerThread();
           }

           for(int i=0; i<workers.length;i++){
               workers[i].start();
           }

           try{
               Thread.sleep(65000);
           }catch(Exception e){
               //ignore
           }

        for(int i=0; i<workers.length;i++){
            workers[i].setExit(true);
        }
	if(failedCount > 0) {
  	    stat.addStatus(testSuite + "test1 : ", stat.FAIL);
	} else {
  	    stat.addStatus(testSuite + "test1 : ", stat.PASS);
	}    
        stat.printSummary();
    }


    class WorkerThread extends Thread{

        private boolean exit = false;

        public WorkerThread(){
        }

        public void setExit(boolean exit){
            this.exit = exit;
        }

        public void run() {
            try{
                InitialContext ic = new InitialContext();
                Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
                SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

                SimpleBMP simpleBMP = simpleBMPHome.create();
		test(simpleBMP);
            }catch(Exception e){
                System.out.println("Thread : " + Thread.currentThread().getName() + "did not run ");
                e.printStackTrace();
            }

        }

        public boolean test(SimpleBMP bmp) throws RemoteException{
	    if( ! bmp.test1()) {
		SimpleBMPClient.this.failedCount++;
	    }
	    return true;
        }

    }
}
