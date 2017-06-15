package com.sun.s1asdev.jdbc.markconnectionasbad.local.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.markconnectionasbad.local.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.markconnectionasbad.local.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();

    public static void main(String[] args)
            throws Exception {

        Client client = new Client();
        client.runTest();
    }

    public void runTest() throws Exception {
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
        stat.addDescription("Mark-Connection-As-Bad test");
        String con1 = simpleBMP.test1();
        String con2 = simpleBMP.test1();
        //System.out.println("Client : con-1 -> " + con1);
        //System.out.println("Client : con-2 -> " + con2);
        if (con1 != null && con2 != null && !con1.equals(con2)) {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - Shareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - Shareable - ReadOnly] : ", stat.FAIL);
        }

        con1 = simpleBMP.test2();
        con2 = simpleBMP.test2();
        //System.out.println("Client : con-1 -> " + con1);
        //System.out.println("Client : con-2 -> " + con2);

        if (con1 != null && con2 != null && !con1.equals(con2)) {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - Shareable Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - Shareable Write] : ", stat.FAIL);
        }

        if (simpleBMP.test3()) {
            stat.addStatus(" Mark-Connection-As-Bad [local - NoTx - Shareable ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - NoTx - Shareable ReadOnly] : ", stat.FAIL);
        }

        if (simpleBMP.test4()) {
            stat.addStatus(" Mark-Connection-As-Bad [local - NoTx - Shareable Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - NoTx - Shareable Write] : ", stat.FAIL);
        }

        if (simpleBMP.test5(1, true)) {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - UnShareable Write (1) ] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - UnShareable Write (1) ] : ", stat.FAIL);
        }

        if (simpleBMP.test5(2, false)) {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - UnShareable Write (2) ] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - UnShareable Write (2) ] : ", stat.FAIL);
        }


        if (simpleBMP.test5(5, false)) {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - UnShareable Write (3) ] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [local - Tx - UnShareable Write (3) ] : ", stat.FAIL);
        }

        System.out.println("Mark-Connection-As-Bad Status: ");
        stat.printSummary();
    }
}
