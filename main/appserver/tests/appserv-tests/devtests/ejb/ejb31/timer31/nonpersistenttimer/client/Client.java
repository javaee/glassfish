package com.sun.s1asdev.timer31.nonpersistenttimer.client;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.ejb.*;
//import javax.jms.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.ejb31.timer.nonpersistenttimer.StatefulWrapper;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    @EJB private static StatefulWrapper wrapper;

    public static void main(String args[]) { 
        boolean doJms = false; // TODO (args.length == 1) && (args[0].equalsIgnoreCase("jms"));

        stat.addDescription("ejb31-timer-nonpersistenttimer");


        System.out.println("Doing foo timer test for ejbs/Foo_CMT");
        boolean result = wrapper.doFooTest("ejbs/Foo_CMT", doJms);
        System.out.println("Foo: ejbs/Foo_CMT" + ((result)? " passed!!" : "failed!!"));
        stat.addStatus("Foo: ejbs/Foo_CMT", (result)? stat.PASS : stat.FAIL);

        System.out.println("Doing foo timer test for ejbs/Foo_UNSPECIFIED_TX");
        result = wrapper.doFooTest("ejbs/Foo_UNSPECIFIED_TX", doJms);
        System.out.println("Foo: ejbs/Foo_UNSPECIFIED_TX" + ((result)? " passed!!" : "failed!!"));
        stat.addStatus("Foo: ejbs/Foo_UNSPECIFIED_TX", (result)? stat.PASS : stat.FAIL);

        System.out.println("Doing foo timer test for ejbs/Foo_BMT");
        result = wrapper.doFooTest("ejbs/Foo_BMT", doJms);
        System.out.println("Foo: ejbs/Foo_BMT" + ((result)? " passed!!" : "failed!!"));
        stat.addStatus("Foo: ejbs/Foo_BMT", (result)? stat.PASS : stat.FAIL);


	/** TODO
        result = wrapper.doMessageDrivenTest("jms/TimerMDBQueue_CMT", doJms);
        System.out.println("Message-driven test jms/TimerMDBQueue_CMT" 
                + ((result)? " passed!!" : "failed!!"));
        stat.addStatus("Message-driven test: jms/TimerMDBQueue_CMT", 
                (result)? stat.PASS : stat.FAIL);

        result = wrapper.doMessageDrivenTest("jms/TimerMDBQueue_BMT", doJms);
        System.out.println("Message-driven test jms/TimerMDBQueue_BMT" 
                + ((result)? " passed!!" : "failed!!"));
        stat.addStatus("Message-driven test: jms/TimerMDBQueue_BMT", 
                (result)? stat.PASS : stat.FAIL);
	**/

        try {
	     wrapper.removeFoo();
        } catch(Exception e) {
            e.printStackTrace();
        }

        stat.printSummary("ejb31-timer-nonpersistenttimer");
    }

    // when running this class through the appclient infrastructure
    public Client() {
        try {
            context = new InitialContext();
        } catch(Exception e) {
            System.out.println("Client : new InitialContext() failed");
            e.printStackTrace();
            stat.addStatus("Client() ", stat.FAIL);
        }
    }

}
