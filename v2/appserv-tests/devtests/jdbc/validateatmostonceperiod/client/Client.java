package com.sun.s1asdev.jdbc.validateatmostonceperiod.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.validateatmostonceperiod.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.validateatmostonceperiod.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new
                SimpleReporterAdapter();
        String testSuite = "ValidateAtmostOncePeriod ";
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome convalBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP convalBMP = convalBMPHome.create();
	stat.addDescription("JDBC Validate Atmost Once Period Tests");

        if (args != null && args.length > 0) {
            String param = args[0];

            switch (Integer.parseInt(param)) {
                case 1: {
                    if (convalBMP.test1()) {
                        stat.addStatus(testSuite + "test1 ", stat.PASS);
                    } else {
                        stat.addStatus(testSuite + "test1 ", stat.FAIL);
                    }
                    break;
                }
                case 2: {
                    if (convalBMP.test1()) {
                        stat.addStatus(testSuite + "test2 ", stat.PASS);
                        System.out.println("test-2 returned true as validation is enabled ");
                    } else {
                        stat.addStatus(testSuite + "test2 ", stat.FAIL);
                    }
                    break;
                }
                case 3: {
                    try {
                        if(convalBMP.test1()){
                            stat.addStatus(testSuite + "test3 ", stat.FAIL);
                        }else{
                            stat.addStatus(testSuite + "test3 ", stat.PASS);
                            System.out.println("test-3 returned false as validation-atmost-once skipped validation");
                        }
                    }
                    catch (Exception e) {
                        stat.addStatus(testSuite + "test3 ", stat.PASS);
                    }
                    break;
                }
            }
            stat.printSummary();
        }
    }
}
