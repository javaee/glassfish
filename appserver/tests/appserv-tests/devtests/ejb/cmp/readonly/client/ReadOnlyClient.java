/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.s1asdev.ejb.cmp.readonly.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import com.sun.s1asdev.ejb.cmp.readonly.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.ejb.EJBException;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

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
            stat.addDescription("Testing Cmp CMPReadOnlyClient app.");
            test11();

            test01();                                  
            test05();

            test12();
            stat.printSummary("CMPReadOnlyClient");
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
            stat.addDescription("Cmp CMPReadOnlyClient thread test");            
            test12();
            stat.printSummary("CMPReadOnlyClient");
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
                stat.addStatus("CMPReadOnlyClient Student   ", stat.FAIL);
            //} catch (CreateException cEx) {
            } catch (java.rmi.RemoteException cEx) {
                stat.addStatus("CMPReadOnlyClient Student   ", stat.PASS);
            } catch (Exception ex) {
                stat.addStatus("CMPReadOnlyClient Student   ", stat.FAIL);
                System.err.println("Caught an exception!");
                ex.printStackTrace();
                //Expected 
            }
        } catch (Throwable th) {
            System.err.println("Caught an unexpected exception!");
            th.printStackTrace();
            stat.addStatus("CMPReadOnlyClient Student   ", stat.FAIL);
        }
    }

    private void test02() {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleStudent");
            StudentHome sHome = 
                (StudentHome) PortableRemoteObject.narrow(objref, 
                                                          StudentHome.class);

            try {
                Student denise = sHome.create("823", "Denise Smith");
                stat.addStatus("CMPReadOnlyClient Student   ", stat.FAIL);
            //} catch (CreateException cEx) {
            } catch (java.rmi.RemoteException cEx) {
                stat.addStatus("CMPReadOnlyClient Student   ", stat.PASS);
            } catch (Exception ex) {
                stat.addStatus("CMPReadOnlyClient Student   ", stat.FAIL);
                System.err.println("Caught an exception!");
                ex.printStackTrace();
                //Expected 
            }
        } catch (Throwable th) {
            System.err.println("Caught an unexpected exception!");
            th.printStackTrace();
            stat.addStatus("CMPReadOnlyClient Student   ", stat.FAIL);
        }
    }

    private void test05() {
        try {
            com.sun.appserv.ejb.ReadOnlyBeanNotifier
                notifier = com.sun.appserv.ejb.ReadOnlyBeanHelper.
                getReadOnlyBeanNotifier("java:comp/env/ejb/SimpleStudent");
            if (notifier != null) {
                stat.addStatus("CMPReadOnlyClient appserv.ClientNotifier ", stat.PASS);
            } else {
                stat.addStatus("CMPReadOnlyClient appserv.ClientNotifier ", stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus("CMPReadOnlyClient appserv.ClientNotifier ", stat.FAIL);
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

            StudentHome mutableStudentHome = 
                (StudentHome) PortableRemoteObject.narrow(objref, 
                                                    StudentHome.class);

            Student newStudent = mutableStudentHome.create("999", "Joe Schmo");
            Student newStudent2 = mutableStudentHome.create("998", "Jill Schmo");
            readOnlyStudentHome.testFind("999");
            readOnlyStudentHome.testFind("998");
            
            readOnlyStudentHome.testLocalFind("999");
            readOnlyStudentHome.testLocalFind("998");

            readOnlyStudentHome.findBar("999");
            readOnlyStudentHome.findBar("998");
            readOnlyStudentHome.findBar("999");
            readOnlyStudentHome.findBar("998");

            Student readOnlyStudent = 
                readOnlyStudentHome.findByPrimaryKey("999");
            Student readOnlyStudent2 = 
                readOnlyStudentHome.findByPrimaryKey("998");

            readOnlyStudentHome.findByRemoteStudent(readOnlyStudent);
            readOnlyStudentHome.findByRemoteStudent(readOnlyStudent2);
            readOnlyStudentHome.findByRemoteStudent(readOnlyStudent);
            readOnlyStudentHome.findByRemoteStudent(readOnlyStudent2);

            readOnlyStudentHome.findFoo();
            readOnlyStudentHome.findFoo();

            readOnlyStudentHome.testLocalCreate("999");
            readOnlyStudentHome.testLocalRemove("999");

            try {
                readOnlyStudentHome.remove("999");
                throw new EJBException("read-only studentHome remove operation" +
                    " on cmp bean should have thrown exception");
            } catch(RemoteException re) {
                System.out.println("Successfully caught exception on " +
                                   "CMP read-only Home bean remove");
            }

            try {
                readOnlyStudent.remove();
                throw new EJBException("read-only student remove operation" +
                    " on cmp bean should have thrown exception");
            } catch(RemoteException re) {
                System.out.println("Successfully caught exception on " +
                                   "CMP read-only bean remove");
            }


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
                 
                stat.addStatus("CMPReadOnlyClient ReadMostly  ", stat.PASS);
            } else {
                stat.addStatus("CMPReadOnlyClient ReadMostly  ", stat.FAIL);
            }

            newStudent.remove();

        } catch (Exception ex) {
            stat.addStatus("CMPReadOnlyClient ReadMostly  ", stat.FAIL);
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

            StudentHome mutableStudentHome = 
                (StudentHome) PortableRemoteObject.narrow(objref, 
                                                    StudentHome.class);
          
            Student[] newStudents = new Student[numPKs_];
            Student[] readOnlyStudents = new Student[numPKs_];
            Thread[] readers = new Thread[numThreads_ * numPKs_];
            Thread[] writers = new Thread[numPKs_];

            for(int i = 0; i < numPKs_; i++) {
                newStudents[i] = 
                    mutableStudentHome.create("ReaderThread" + i, "ABC" + i);
                System.out.println("Creating student " + 
                                   newStudents[i].getPrimaryKey());
            }

            System.out.println("Calling nonFindByPK finder");
            readOnlyStudentHome.findFoo();

            for(int i = 0; i < numPKs_; i++) {
                readOnlyStudents[i] = 
                    readOnlyStudentHome.findByPrimaryKey("ReaderThread" + i);
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
            
           
            stat.addStatus("CMPReadOnlyClient ReaderThreads  ", stat.PASS);

            System.out.println("Removing all students");
            for(int i = 0; i < newStudents.length; i++) {
                newStudents[i].remove();
            }

        } catch (Exception ex) {
            stat.addStatus("CMPReadOnlyClient ReaderThreads  ", stat.FAIL);
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
