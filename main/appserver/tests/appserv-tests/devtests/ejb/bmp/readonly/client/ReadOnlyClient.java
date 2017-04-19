/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.s1asdev.ejb.bmp.readonly.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import com.sun.s1asdev.ejb.bmp.readonly.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.ejb.CreateException;

/**
 * A simple java client will: 
 * <ul>
 * <li>Locates the home interface of the enterprise bean
 * <li>Gets a reference to the remote interface
 * <li>Invokes business methods
 * </ul>
 */
public class ReadOnlyClient {

    private SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private int numIterations_ = 100;
    private int numThreads_ = 10;
    private int numPKs_ = 1;
    private long sleepTimeInMillis_ = 100;

    public static void main(String[] args) { 

        ReadOnlyClient client = new ReadOnlyClient(); 

        if( args.length == 4 ) {

            int numIterations = Integer.parseInt(args[0]);
            int numThreads = Integer.parseInt(args[1]);
            int numPKs = Integer.parseInt(args[2]);
            long sleepTimeInMillis = Long.parseLong(args[3]);
            client.runThreadTest(numIterations, numThreads, numPKs,
                                 sleepTimeInMillis);   
        } else {
            client.runTestClient();               
        }

        // run the tests
        
    }

    public void runTestClient() {       

        try{
            stat.addDescription("Testing Bmp ReadOnlyClient app.");
            test01();
            test02();
            test03();
            test04();
            test05();
            test07();
            test09();
            test10();
            test11();
            test12();
            stat.printSummary("ReadOnlyClient");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void runThreadTest(int numIterations, int numThreads, int numPKs,
                              long sleepTimeInMillis) {       

        numIterations_ = numIterations;
        numThreads_ = numThreads;
        numPKs_ = numPKs;
        sleepTimeInMillis_ = sleepTimeInMillis;

        try{
            stat.addDescription("Bmp ReadOnlyClient thread test");            
            test12();
            stat.printSummary("ReadOnlyClient");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }

    private void test01() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleStudent");
            StudentHome sHome = 
                (StudentHome) PortableRemoteObject.narrow(objref, 
                                                          StudentHome.class);

            try {
                Student denise = sHome.create("823", "Denise Smith");
                stat.addStatus("Bmp-ReadOnly Student   ", stat.FAIL);
            } catch (CreateException cEx) {
                stat.addStatus("Bmp-ReadOnly Student   ", stat.PASS);
            } catch (Exception ex) {
                stat.addStatus("Bmp-ReadOnly Student   ", stat.FAIL);
                System.err.println("Caught an exception!");
                ex.printStackTrace();
                //Expected 
            }
        } catch (Throwable th) {
            System.err.println("Caught an unexpected exception!");
            th.printStackTrace();
            stat.addStatus("Bmp-ReadOnly Student   ", stat.FAIL);
        }
    }

    private void test02() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleCourse");

            CourseHome cHome = 
                (CourseHome) PortableRemoteObject.narrow(objref, 
                                                         CourseHome.class);

            try {
                Course power = cHome.create("220", "Power J2EE Programming");
                stat.addStatus("Bmp-ReadOnly Course    ", stat.FAIL);
            } catch (CreateException cEx) {
                stat.addStatus("Bmp-ReadOnly Course    ", stat.PASS);
            } catch (Exception ex) {
                stat.addStatus("Bmp-ReadOnly Course    ", stat.FAIL);
                System.err.println("Caught an exception!");
                ex.printStackTrace();
                //Expected 
            }
        } catch (Throwable th) {
            System.err.println("Caught an unexpected exception!");
            th.printStackTrace();
            stat.addStatus("Bmp-ReadOnly Course    ", stat.FAIL);
        }
    }

    private void test03() {
        try {

            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleEnroller");

            EnrollerHome eHome = 
                (EnrollerHome) PortableRemoteObject.narrow(objref, 
                                                           EnrollerHome.class);

            Enroller enroller = eHome.create();
            for (int i=0; i<3; i++) {
                enroller.enroll("student"+i, "course"+i);
            }

            for (int i=0; i<5; i++) {
                enroller.enroll("student5", "course"+i);
            }

            stat.addStatus("Bmp-ReadOnly Enroller  ", stat.PASS);
        } catch (Exception ex) {
            stat.addStatus("Bmp-ReadOnly Enroller  ", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    } 

    private void test04() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleEnroller");

            EnrollerHome eHome = 
                (EnrollerHome) PortableRemoteObject.narrow(objref, 
                                                           EnrollerHome.class);

            Enroller enroller = eHome.create();
            boolean status = enroller.canGetReadOnlyBeanNotifier(true);
            if (status == true) {
                stat.addStatus("Bmp-ReadOnly SFSB-NewNotifier ", stat.PASS);
            } else {
                stat.addStatus("Bmp-ReadOnly SFSB-NewNotifier ", stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus("Bmp-ReadOnly SFSBNotifier ", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    }

    private void test05() {
        try {
            com.sun.appserv.ejb.ReadOnlyBeanNotifier
                notifier = com.sun.appserv.ejb.ReadOnlyBeanHelper.
                getReadOnlyBeanNotifier("java:comp/env/ejb/SimpleStudent");
            if (notifier != null) {
                stat.addStatus("Bmp-ReadOnly appserv.ClientNotifier ", stat.PASS);
            } else {
                stat.addStatus("Bmp-ReadOnly appserv.ClientNotifier ", stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus("Bmp-ReadOnly appserv.ClientNotifier ", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    }

    private void test07() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleEnroller");

            EnrollerHome eHome = 
                (EnrollerHome) PortableRemoteObject.narrow(objref, 
                                                           EnrollerHome.class);

            Enroller enroller = eHome.create();
            boolean status = enroller.testReadOnlyBeanStudentRefresh("student0", true);
            if (status == true) {
                stat.addStatus("Bmp-ReadOnly SFSB-NewStudentRefresh ", stat.PASS);
            } else {
                stat.addStatus("Bmp-ReadOnly SFSB-NewStudentRefresh ", stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus("Bmp-ReadOnly SFSBNotifierRefresh ", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    }

    private void test09() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleEnroller");

            EnrollerHome eHome = 
                (EnrollerHome) PortableRemoteObject.narrow(objref, 
                                                           EnrollerHome.class);

            Enroller enroller = eHome.create();
            boolean status = enroller.canGetReadOnlyBeanLocalNotifier(true);
            if (status == true) {
                stat.addStatus("Bmp-ReadOnly SFSBLocal-NewNotifier ", 
                               stat.PASS);
            } else {
                stat.addStatus("Bmp-ReadOnly SFSBLocal-NewNotifier ", 
                               stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus("Bmp-ReadOnly SFSBLocalNotifier ", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    }

    private void test10() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleEnroller");

            EnrollerHome eHome = 
                (EnrollerHome) PortableRemoteObject.narrow(objref, 
                                                           EnrollerHome.class);

            Enroller enroller = eHome.create();
            boolean status = enroller.testReadOnlyBeanLocalStudentRefresh("student0", 
                                                                  true);
            if (status == true) {
                stat.addStatus("Bmp-ReadOnly SFSBLocal-NewStudentRefresh ", 
                               stat.PASS);
            } else {
                stat.addStatus("Bmp-ReadOnly SFSBLocal-NewStudentRefresh ", 
                               stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus("Bmp-ReadOnly SFSBLocalNotifierRefresh ", 
                           stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    }

    private void test11() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleStudent");

            StudentHome readOnlyStudentHome = 
                (StudentHome) PortableRemoteObject.narrow(objref, 
                                                          StudentHome.class);
            objref = initial.lookup("java:comp/env/ejb/MutableStudent");

            StudentHomeMutable mutableStudentHome = 
                (StudentHomeMutable) PortableRemoteObject.narrow(objref, 
                                                    StudentHomeMutable.class);

            Student newStudent = mutableStudentHome.create("999", "Joe Schmo");
            Student readOnlyStudent = 
                readOnlyStudentHome.findByPrimaryKey("999");
            String name1  = newStudent.getName();
            String readOnlyName1 = readOnlyStudent.getName();
            String readOnlyName1Tx = readOnlyStudent.getNameTx();

            // update name AND notify read-only bean
            newStudent.setName("Josephina Schmo", true);
            String name2  = newStudent.getName();
            String readOnlyName2 = readOnlyStudent.getName();
            String readOnlyName2Tx = readOnlyStudent.getNameTx();

            // update name AND DON'T notify read-only bean
            newStudent.setName("Henrietta Schmo", false);
            String name3 = newStudent.getName();
            String readOnlyName3 = readOnlyStudent.getName();
            String readOnlyName3Tx = readOnlyStudent.getNameTx();

            System.out.println("name 1 = " + name1 + " , " + readOnlyName1);
            System.out.println("name 2 = " + name2 + " , " + readOnlyName2);
            System.out.println("name 3 = " + name3 + " , " + readOnlyName3);

            System.out.println("name1 tx= " + name1 + " , " + readOnlyName1Tx);
            System.out.println("name2 tx= " + name2 + " , " + readOnlyName2Tx);
            System.out.println("name3 tx= " + name3 + " , " + readOnlyName3Tx);

            // NOTE : name 3 *should* be different from readOnlyName3, but
            // we can't depend on it since it's possible for the container
            // to create a new instance of the bean and call ejbLoad on it.
            // So, only use name1 and name2 to decide if test passed.
            if (name1.equals(readOnlyName1) && name2.equals(readOnlyName2) &&
                name1.equals(readOnlyName1Tx) && 
                name2.equals(readOnlyName2Tx)) {
                 
                stat.addStatus("Bmp-ReadOnly ReadMostly  ", stat.PASS);
            } else {
                stat.addStatus("Bmp-ReadOnly ReadMostly  ", stat.FAIL);
            }

            newStudent.remove();

        } catch (Exception ex) {
            stat.addStatus("Bmp-ReadOnly ReadMostly  ", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        } 
    }

    private void test12() {
        try {
            System.out.println("Beginning test12()");
            System.out.println("Num Iterations = " + numIterations_);
            System.out.println("Num Threads = " + numThreads_);
            System.out.println("Num PKs = " + numPKs_);
            System.out.println("Sleep time in milliseconds = " + 
                               sleepTimeInMillis_);

            int numWriterIterations = numIterations_ / 100;
            long writerSleepTimeInMillis = sleepTimeInMillis_ * 100;

            System.out.println("Num writer iterations = " +
                               numWriterIterations);
            System.out.println("Writer sleep time in milliseconds = " + 
                               writerSleepTimeInMillis);

            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleStudent");

            StudentHome readOnlyStudentHome = 
                (StudentHome) PortableRemoteObject.narrow(objref, 
                                                          StudentHome.class);
            objref = initial.lookup("java:comp/env/ejb/MutableStudent");

            StudentHomeMutable mutableStudentHome = 
                (StudentHomeMutable) PortableRemoteObject.narrow(objref, 
                                                    StudentHomeMutable.class);
          
            Student[] newStudents = new Student[numPKs_];
            Student[] readOnlyStudents = new Student[numPKs_];
            Thread[] readers = new Thread[numThreads_ * numPKs_];
            Thread[] writers = new Thread[numPKs_];

            for(int i = 0; i < numPKs_; i++) {
                newStudents[i] = 
                    mutableStudentHome.create("ReaderThread" + i, "ABC" + i);
                readOnlyStudents[i] = 
                    readOnlyStudentHome.findByPrimaryKey("ReaderThread" + i);
                System.out.println("Creating student " + 
                                   readOnlyStudents[i].getPrimaryKey());
                writers[i] = new WriterThread(newStudents[i], 
                                              numWriterIterations,
                                              writerSleepTimeInMillis);
                for(int j = 0; j < numThreads_; j++) {
                    System.out.println("Creating thread " + j);
                    int threadIndex = i*numThreads_ + j;
                    System.out.println("Thread index = " + threadIndex);
                    readers[threadIndex] = new ReaderThread
                        (readOnlyStudents[i], numIterations_, 
                         sleepTimeInMillis_);
                }
            }
            
            for(int i = 0; i < readers.length; i++) {
                System.out.println("Starting reader thread " + i);
                readers[i].start();
            }    
                
            for(int i = 0; i < writers.length; i++) {
                System.out.println("Starting writer thread " + i);
                writers[i].start();
            }     
            
            System.out.println("Joining on " + readers.length + " readers");
            for(int i = 0; i < readers.length; i++) {
                readers[i].join();
            }
            System.out.println("Joining on " + writers.length + " writers");
            for(int i = 0; i < writers.length; i++) {
                writers[i].join();
            }
            
           
            stat.addStatus("Bmp-ReadOnly ReaderThreads  ", stat.PASS);

            System.out.println("Removing all students");
            for(int i = 0; i < newStudents.length; i++) {
                newStudents[i].remove();
            }

        } catch (Exception ex) {
            stat.addStatus("Bmp-ReadOnly ReaderThreads  ", stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        } 
    }

    private class ReaderThread extends Thread {

        Student readOnlyStudent_;              
        int numReads_;
        long sleepTimeInMillis_;
        String studentPK_;        

        ReaderThread(Student readOnlyStudent, int numReads, 
                     long sleepTimeInMillis) {
            readOnlyStudent_ = readOnlyStudent;           
            numReads_ = numReads;
            sleepTimeInMillis_ = sleepTimeInMillis;
        }

        public void run() {                           
            for( int i = 0; i < numReads_; i++) {
                try {
                    if( i == 0 ) {
                        studentPK_ = (String) readOnlyStudent_.getPrimaryKey();
                    }
                    String name = readOnlyStudent_.getName();
                    System.out.println("Got name = " + name + " for PK " +
                                       studentPK_);
                    Thread.sleep(sleepTimeInMillis_);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class WriterThread extends Thread {

        Student mutableStudent_;              
        int numWrites_;
        long sleepTimeInMillis_;
        String studentPK_;

        WriterThread(Student mutableStudent, int numWrites, 
                     long sleepTimeInMillis) {
            mutableStudent_ = mutableStudent;           
            numWrites_ = numWrites;
            sleepTimeInMillis_ = sleepTimeInMillis;
        }

        public void run() {                           
            for( int i = 0; i < numWrites_; i++) {                
                try {
                    if( i == 0 ) {
                        studentPK_ = (String) mutableStudent_.getPrimaryKey();
                    }
                    boolean refresh = ((i % 2) == 1);
                    String newName = studentPK_ + "__" + i;
                    mutableStudent_.setName(newName, refresh);
                    System.out.println("Set name " + newName + " for PK " +
                                       studentPK_ + " refresh = " + refresh);
                    Thread.sleep(sleepTimeInMillis_);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

} 
