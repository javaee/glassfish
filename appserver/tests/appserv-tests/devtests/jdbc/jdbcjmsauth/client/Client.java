/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
