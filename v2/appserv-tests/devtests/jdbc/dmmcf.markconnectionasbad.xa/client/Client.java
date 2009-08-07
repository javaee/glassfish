package com.sun.s1asdev.jdbc.markconnectionasbad.xa.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.markconnectionasbad.xa.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.markconnectionasbad.xa.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.Set;
import java.util.HashSet;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();
    public static final int MAX_POOL_SIZE = 5;


    public static void main(String[] args)
            throws Exception {

        Client client = new Client();
        client.runTest();
    }

    public void runTest() throws Exception {


	Set<Integer> localdsSet = new HashSet();
	Set<Integer> localdsAfterSet = new HashSet();
	int countLocalds = 0;

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
        stat.addDescription("Mark-Connection-As-Bad  ");


	localdsSet = simpleBMP.getFromLocalDS(MAX_POOL_SIZE);
	System.out.println("localds = " + localdsSet);
	
	//jdbc-local-pool
        simpleBMP.test1();

	localdsAfterSet = simpleBMP.getFromLocalDS(MAX_POOL_SIZE);
	System.out.println("localdsAfter = " + localdsAfterSet);

	countLocalds = compareAndGetCount(localdsSet, localdsAfterSet);
	if(MAX_POOL_SIZE-countLocalds == 5) {
            stat.addStatus(" Mark-Connection-As-Bad destroyedCount localds: ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad destroyedCount localds: ", stat.FAIL);
        }

        /*if (simpleBMP.test2() && getMonitorablePropertyOfConnectionPool("jdbc-unshareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 10) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - UnShareable - Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - UnShareable - Write] : ", stat.FAIL);
        }

        if (simpleBMP.test3() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 5) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - Shareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - Shareable - ReadOnly] : ", stat.FAIL);
        }

        if (simpleBMP.test4() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 10) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - Shareable - Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - Shareable - Write] : ", stat.FAIL);
        }

        if (simpleBMP.test5() && getMonitorablePropertyOfConnectionPool("jdbc-unshareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 15) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - ReadOnly] : ", stat.FAIL);
        }

        if (simpleBMP.test6() && getMonitorablePropertyOfConnectionPool("jdbc-unshareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 20) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - Write] : ", stat.FAIL);
        }

        if (simpleBMP.test7() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 11) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - Shareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - Shareable - ReadOnly] : ", stat.FAIL);
        }

        if (simpleBMP.test8() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 12) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - Shareable - Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - Shareable - Write] : ", stat.FAIL);
        }

        if (simpleBMP.test9() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 13 &&
                getMonitorablePropertyOfConnectionPool("jdbc-local-pool",
                        NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 1) {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Write-Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Write-Write] : ", stat.FAIL);
        }

        if (simpleBMP.test10() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 14 &&
                getMonitorablePropertyOfConnectionPool("jdbc-local-pool",
                        NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 2) {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Read-Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Read-Write] : ", stat.FAIL);
        }

         if (simpleBMP.test11() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG )==15 &&
                 getMonitorablePropertyOfConnectionPool("jdbc-local-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG )==3) {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Read-Read] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Read-Read] : ", stat.FAIL);
        }

         if (simpleBMP.test12() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG )==16 &&
                 getMonitorablePropertyOfConnectionPool("jdbc-local-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG )==4) {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Write-Read] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Write-Read] : ", stat.FAIL);
        }*/


        System.out.println(" Mark-Connection-As-Bad ");
        stat.printSummary();
    }

    public int compareAndGetCount(Set<Integer> beforeSet, Set<Integer> afterSet) {
        //denotes the count of hashcodes that matched in both sets.
        int contains = 0;	    
	if(!beforeSet.containsAll(afterSet)) {
            //if it does not contain all the elements of the after set
	    //find how many are absent from the beforeSet
            for(int afterInt : afterSet) {
                    if(beforeSet.contains(afterInt)) {
	                    contains++;
	                }
            }		    
	}
        return contains;
    }

}
