package com.sun.s1asdev.jdbc.maxconnectionusage.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.maxconnectionusage.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.maxconnectionusage.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.rmi.RemoteException;

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
        stat.addDescription("Max Connection Usage");

        if (simpleBMP.test1(false)) {
            stat.addStatus(" Max Connection Usage -  (local-TxNotSupported): ", stat.PASS);
        } else {
            stat.addStatus(" Max Connection Usage -  (local-TxNotSupported): ", stat.FAIL);
        }

        boolean useXA = false;
        boolean status = connectionSharingTest(simpleBMP, useXA, 21112);
        if (status) {
            stat.addStatus(" Max Connection Usage - (local-Tx-Sharing) : ", stat.PASS);
        } else {
            stat.addStatus(" Max Connection Usage - (local-Tx-Sharing) : ", stat.FAIL);
        }


        if (simpleBMP.test1(true)) {
            stat.addStatus(" Max Connection Usage -  (XA-TxNotSupported) : ", stat.PASS);
        } else {
            stat.addStatus(" Max Connection Usage -  (XA-TxNotSupported) : ", stat.FAIL);
        }

        //Commented as this test is not valid.
        //physical connection (ds.getConnection(conn)) will be different each time.
        
        /*useXA = true;
        status = connectionSharingTest(simpleBMP, useXA, 12221);

        if (status) {
            stat.addStatus(" Max Connection Usage - (XA-Tx-Sharing) : ", stat.PASS);
        } else {
            stat.addStatus(" Max Connection Usage - (XA-Tx-Sharing) : ", stat.FAIL);
        }*/

        stat.printSummary();
    }

    private boolean connectionSharingTest(SimpleBMP simpleBMP, boolean useXA, int value) throws RemoteException {
        String results[] = new String[10];
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                results[i] = simpleBMP.test2(useXA, value);
            } else {
                results[i] = simpleBMP.test3((i / 2) + 1, useXA, value);
            }
        }
        boolean status = true;
        String result = results[0];
        for (int i = 0; i < results.length; i++) {
            if (!results[i].equalsIgnoreCase(result)) {
                System.out.println("Result 0 : " + result);
                System.out.println("Result " + i + " : " + results[i]);
                status = false;
                break;
            }
        }

        String result2 = simpleBMP.test2(useXA, value);

        if (!result2.equalsIgnoreCase(result) && status ) {
            status = true;
        } else {
            System.out.println("Marking status as false during verification");
            System.out.println("is XA : " + useXA);
            System.out.println("Value : " + value);
            System.out.println("Result 1 : " + result);
            System.out.println("Result 2 : " + result2);
            status = false;
        }
        return status;
    }
}
