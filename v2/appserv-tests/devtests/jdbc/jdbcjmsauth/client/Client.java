package com.sun.s1asdev.jdbc.jdbcjmsauth.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.jdbc.jdbcjmsauth.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter();

    public static void main(String[] args)
        throws Exception {
        
        stat.addDescription("JDBC JMS Authentication tests");
        Client c = new Client();
	c.doJdbcTests();
	c.doJmsTests();

	stat.printSummary();
    }

    private void doJdbcTests() throws Exception {

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
            PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);
        
        SimpleBMP simpleBMP = simpleBMPHome.create();
        String testSuite = "JDBCJmsAuth-JDBC ";	
        System.out.println(" jdbc tests 3 & 7 will fail with Derby"); 	
	System.out.println("-----------------------------------------------");
	System.out.println("          JDBC Tests ");
	System.out.println("-----------------------------------------------");
	System.out.println("app auth : getting connection w/o user/pass");
	System.out.print("Should NOT get a connection : " );
        if ( simpleBMP.test1() ) {
	    stat.addStatus(testSuite+" test1 ", stat.PASS);
	    //System.out.println("PASS");
	} else {
	    stat.addStatus(testSuite+" test1 ", stat.FAIL);
	    //System.out.println("FAIL");
	}

	System.out.println("-----------------------------------------------");
	System.out.println("app auth : getting connection w/ user/pass");
	System.out.print("Should get a connection : " );
	if ( simpleBMP.test2() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite+" test2 ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+" test2 ", stat.FAIL);
	    //System.out.println("FAIL");
	}

	System.out.println("-----------------------------------------------");
	System.out.println("app auth : getting connection w/  wrong user/pass");
	System.out.print("Should NOT get a connection : " );
	if ( simpleBMP.test3() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite + " test3 ", stat.PASS);
	} else {
	    stat.addStatus(testSuite + " test3 ", stat.FAIL);
	    //System.out.println("FAIL");
	}

	System.out.println("-----------------------------------------------");
	System.out.println("app auth : getting connection w/ correct then wrong user/pass");
	System.out.print("Should get a connection then NOT get a connection : " );
	if ( simpleBMP.test4() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite + " test4 ", stat.PASS);
	} else {
	    stat.addStatus(testSuite + " test4 ", stat.FAIL);
	    //System.out.println("FAIL");
	}

	System.out.println("-----------------------------------------------");
	System.out.println("container auth : getting connection w/o user/pass");
	System.out.print("Should get a connection : " );
        if ( simpleBMP.test5() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite+" test5 ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+" test5 ", stat.FAIL);
	    //System.out.println("FAIL");
	}

	System.out.println("-----------------------------------------------");
	System.out.println("container auth : getting connection w/ user/pass");
	System.out.print("Should get a connection : " );
	if ( simpleBMP.test6() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite+" test6 ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+" test6 ", stat.FAIL);
	    //System.out.println("FAIL");
	}

	System.out.println("-----------------------------------------------");
	System.out.println("container auth : getting connection w/ wrong user/pass");
	System.out.print("Should NOT get a connection : " );
	if ( simpleBMP.test7() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite + " test7 ", stat.PASS);
	} else {
	    stat.addStatus(testSuite + " test7 ", stat.FAIL);
	    //System.out.println("FAIL");
	}
    }

    private void doJmsTests() throws Exception {
        InitialContext ic = new InitialContext();

	Object obj = ic.lookup("java:comp/env/ejb/JmsAuthHome");
	JmsAuthHome jmsAuthHome = (JmsAuthHome)
	    PortableRemoteObject.narrow( obj, JmsAuthHome.class );
	JmsAuth jmsAuth = jmsAuthHome.create();

	String testSuite = "JDBCJmsAuth-JMS";
        
	System.out.println("-----------------------------------------------");
	System.out.println("          JMS tests ");
	System.out.println("-----------------------------------------------");
	System.out.println("app auth : getting connection w/ user/pass");
	System.out.print("Should get a connection : " );
	if ( jmsAuth.test1() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite + " test1 ", stat.PASS );
	} else {
	    stat.addStatus(testSuite + " test1 ", stat.FAIL );
	    //System.out.println("FAIL");
	}

	System.out.println("-----------------------------------------------");
	System.out.println("app auth : getting connection w/o user/pass");
	System.out.print("Should get a connection : " );
       	if ( jmsAuth.test2() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite + " test2 ", stat.PASS );
	} else {
	    stat.addStatus(testSuite + " test2 ", stat.FAIL );
	    //System.out.println("FAIL");
	}

	System.out.println("-----------------------------------------------");
	System.out.println("app auth : getting connection w/  wrong user/pass");
	System.out.print("Should NOT get a connection : " );
	if ( jmsAuth.test3() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite + " test3 ", stat.PASS );
	} else {
	    stat.addStatus(testSuite + " test3 ", stat.FAIL );
	    //System.out.println("FAIL");
	}

	System.out.println("-----------------------------------------------");
	System.out.println("app auth : getting connection w/ correct then wrong user/pass");
	System.out.print("Should get a connection then NOT get a connection : " );
       	if ( jmsAuth.test4() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite + " test4 ", stat.PASS );
	} else {
	    stat.addStatus(testSuite + " test4 ", stat.FAIL );
	    //System.out.println("FAIL");
	}

	System.out.println("-----------------------------------------------");
	System.out.println("container auth : getting connection w/o user/pass");
	System.out.print("Should get a connection : " );
       	if ( jmsAuth.test5() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite + " test5 ", stat.PASS );
	} else {
	    stat.addStatus(testSuite + " test5 ", stat.FAIL );
	    //System.out.println("FAIL");
	}
	
	System.out.println("-----------------------------------------------");
	System.out.println("container auth : getting connection w/ user/pass");
	System.out.print("Should get a connection : " );
       	if ( jmsAuth.test6() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite + " test6 ", stat.PASS );
	} else {
	    stat.addStatus(testSuite + " test6 ", stat.FAIL );
	    //System.out.println("FAIL");
	}
/*	
	System.out.println("-----------------------------------------------");
	System.out.println("container auth : getting connection w/ wrong user/pass");
	System.out.print("Should NOT get a connection : " );
       	if ( jmsAuth.test7() ) {
	    //System.out.println("PASS");
	    stat.addStatus(testSuite + " jms-test7 ", stat.PASS );
	} else {
	    stat.addStatus(testSuite + " jms-test7 ", stat.FAIL );
	    //System.out.println("FAIL");
	}
*/        
	System.out.println("-----------------------------------------------");
    }
}
