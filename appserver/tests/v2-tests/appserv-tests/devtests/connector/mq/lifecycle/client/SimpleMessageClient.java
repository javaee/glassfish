
package com.sun.s1peqe.mq.cmt.excpt.client;

import javax.jms.*;
import javax.naming.*;
import java.sql.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleMessageClient {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        stat.addDescription("This is to test Lifecycle listener JMS lookup ");
        boolean passed = false;
        try {

	    Class.forName(args[0]);
	    String url = args[1];
	    java.sql.Connection con = DriverManager.getConnection(url,args[2],args[3]);
	    ResultSet rs = con.createStatement().executeQuery("select status from lifecycle_test1");
	    int count = 0;
	    while (rs.next()){
	        count = rs.getInt(1);
	    }
            rs.close();
	    con.close();
	    if (count == 0) {
	       throw new Exception("test failed because status : " + 0);
	    }
            passed = true;
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e.toString());
	    passed = false;
            stat.addStatus("simple lifecycle test1", stat.FAIL);
        } finally {
            if (passed) stat.addStatus("simple lifecycle test1", stat.PASS);
            stat.printSummary("simpleLifecycle");
            System.exit(0);
        } // finally
    } // main
} // class

