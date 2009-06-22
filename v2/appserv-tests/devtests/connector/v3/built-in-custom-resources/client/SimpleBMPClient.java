package com.sun.s1asdev.jdbc.CustomResourceFactories.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.CustomResourceFactories.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.CustomResourceFactories.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    public static void main(String[] args)
        throws Exception {

 	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "Custom Resource : ";

    InitialContext ic = new InitialContext();
    Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
    javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

    SimpleBMP simpleBMP = simpleBMPHome.create();

        String test = args[0];

        if(test.equalsIgnoreCase("javabean")){
            if ( simpleBMP.testJavaBean(args[1]) ) {
                stat.addStatus(testSuite+" Java Bean Factory : ", stat.PASS);
            } else {
                stat.addStatus(testSuite+" Java Bean Factory : ", stat.FAIL);
            }
        }else if(test.equalsIgnoreCase("primitivesandstring")){

            if ( simpleBMP.testPrimitives(args[1], args[2], args[3]) ) {
                stat.addStatus(testSuite+" Primitives And String Factory : ", stat.PASS);
            } else {
                stat.addStatus(testSuite+" Primitives And String Factory : ", stat.FAIL);
            }

        }else if(test.equalsIgnoreCase("properties")){
            Properties properties = new Properties();
            for (int i=1; i<args.length-1;i++){
                properties.put(args[i],args[i+1]);
                i++;
            }

            if ( simpleBMP.testProperties(properties, args[args.length-1])) {
                stat.addStatus(testSuite+" Properties : ", stat.PASS);
            } else {
                stat.addStatus(testSuite+" Properties : ", stat.FAIL);
            }
        }else if(test.equalsIgnoreCase("url")){
            if ( simpleBMP.testURL(args[1], args[2])) {
                stat.addStatus(testSuite+" URL : ", stat.PASS);
            } else {
                stat.addStatus(testSuite+" URL : ", stat.FAIL);
            }
        }



    stat.printSummary();
    }
}
