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

package com.sun.s1asdev.jdbc.jdbcjmsauth.ejb;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;

public class JmsAuthBean implements SessionBean {
    
    private SessionContext ctxt_;
    private QueueConnectionFactory qcf_;
    private QueueConnectionFactory qcf1_;

    public void ejbCreate() throws CreateException {
        try {
	    InitialContext ic = new InitialContext();
            qcf_ = (QueueConnectionFactory) 
	        ic.lookup("java:comp/env/jms/MyQueueConnectionFactory");
            qcf1_ = (QueueConnectionFactory) 
	        ic.lookup("java:comp/env/jms/MyQueueConnectionFactory_CM");
	} catch( Exception e ) {
	    e.printStackTrace();
	    CreateException ce = new CreateException( e.getMessage() );
	    ce.initCause( e );
	    throw ce;
	}
	
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void setSessionContext( SessionContext ctxt ) {
        ctxt_ = ctxt;
    }

    public boolean test1() {
        //application auth - getConnection w/ user/pass - must pass
	boolean passed = true;
        QueueConnection con = null;
	QueueSession session = null;

	try {
            con = qcf_.createQueueConnection("guest", "guest");	
	    session = con.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
	} catch( Exception e ) {
	    System.out.println("------------jms test 1--------------");
	    e.printStackTrace();
	    System.out.println("------------------------------------");
	    passed = false;
	} finally {
            try {
	        session.close();
                con.close();
                	
            } catch( Exception e ) {}
	}

	return passed;
    }

    public boolean test2() {
        //application auth - getConnection w/o user/pass - must fail
	boolean passed = true;
        QueueConnection con = null;
        QueueSession session = null;

	try {
            con = qcf_.createQueueConnection();	
	    session = con.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
	} catch( Exception e ) {
	    System.out.println("------------jms test 2--------------");
	    e.printStackTrace();
	    System.out.println("------------------------------------");
	    passed = false;
	} finally {
            try {
	        session.close();
                con.close();
            } catch( Exception e ) {}
	}

	return passed;
    }
    
    public boolean test3() {
        //application auth - getConnection w/ wrong user/pass - must fail
	boolean passed = false;
        QueueConnection con = null;
        QueueSession session = null;

	try {
            con = qcf_.createQueueConnection("xyz", "xyz");	
	    session = con.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
	} catch( Exception e ) {
	    System.out.println("------------jms test 3--------------");
	    e.printStackTrace();
	    System.out.println("------------------------------------");
	    passed = true;
	} finally {
            try {
	        session.close();
                con.close();
            } catch( Exception e ) {}
	}

	return passed;
    }

    public boolean test4() {
        //application auth - getConnection w/ correct user/pass 
	//and then wrong - must pass
	boolean passed = false;
        QueueConnection con = null;
        QueueSession session = null;

	try {
            con = qcf_.createQueueConnection("guest", "guest");	
	    session = con.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
	} catch( Exception e ) {
	    System.out.println("------------jms test 4--------------");
	    e.printStackTrace();
	    System.out.println("------------------------------------");
	    return passed;
	} finally {
            try {
	        session.close();
                con.close();
            } catch( Exception e ) {}
	}
        
	try {
	    con = qcf_.createQueueConnection("xyz", "xyz" );
	    session = con.createQueueSession( true, Session.AUTO_ACKNOWLEDGE );
	} catch( Exception e ) {
	    System.out.println("------------jms test 4--------------");
	    e.printStackTrace();
	    System.out.println("------------------------------------");
	    passed = true;
	} finally {
	    try {
	        session.close();
		con.close();
	    } catch( Exception e ) {}	
	}
	
	return passed;
    }

    public boolean test5() {
        //container auth - getConnection w/o user/pass - must pass
	boolean passed = true;
        QueueConnection con = null;
	QueueSession session = null;

	try {
            con = qcf1_.createQueueConnection();	
	    session = con.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
	} catch( Exception e ) {
	    System.out.println("------------jms test 5--------------");
	    e.printStackTrace();
	    System.out.println("------------------------------------");
	    passed = false;
	} finally {
            try {
	        session.close();
                con.close();
                	
            } catch( Exception e ) {}
	}

	return passed;
    }
    
    public boolean test6() {
        //container auth - getConnection w/ user/pass - must pass
	boolean passed = true;
        QueueConnection con = null;
	QueueSession session = null;

	try {
            con = qcf1_.createQueueConnection("guest","guest");	
	    session = con.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
	} catch( Exception e ) {
	    System.out.println("------------jms test 6--------------");
	    e.printStackTrace();
	    System.out.println("------------------------------------");
	    passed = false;
	} finally {
            try {
	        session.close();
                con.close();
                	
            } catch( Exception e ) {}
	}

	return passed;
    }

    public boolean test7() {
        //container auth - getConnection w/ wrong user/pass - must fail
	boolean passed = false;
        QueueConnection con = null;
	QueueSession session = null;

	try {
            con = qcf1_.createQueueConnection("xyz", "xyz");	
	    session = con.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
	} catch( Exception e ) {
	    System.out.println("------------jms test 7--------------");
	    e.printStackTrace();
	    System.out.println("------------------------------------");
	    passed = true;
	} finally {
            try {
	        session.close();
                con.close();
                	
            } catch( Exception e ) {}
	}

	return passed;
    }
}
