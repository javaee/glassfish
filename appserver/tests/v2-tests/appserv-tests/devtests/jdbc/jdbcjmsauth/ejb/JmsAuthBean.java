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
