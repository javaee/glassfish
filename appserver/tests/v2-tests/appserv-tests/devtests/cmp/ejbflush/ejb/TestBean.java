package test;

import javax.ejb.*;
import javax.naming.*;

/**
 * This SessionBean is used to test setting CMP field 'name' to a value 
 * that is too large for the column size that it is mapped to.
 * The test is expected to be executed with flush after business method 
 * set to true for setNameWithFlush() and to false for setName().
 * The test is executed for CMP1.1 bean (A1) and CMP2.x bean (A2).
 */ 
public class TestBean implements SessionBean {

    private  A1LocalHome a1Home = null;
    private  A2LocalHome a2Home = null;
    private  A1Local a1bean = null;
    private  A2Local a2bean = null;

    // SessionBean methods
 
    public void ejbCreate() throws CreateException {
        System.out.println("TestBean ejbCreate");
        try {
            a1Home = lookupA1();
            a2Home = lookupA2();
 
        } catch (NamingException ex) {
            throw new EJBException(ex.getMessage());
        }
    }    
 
    public void ejbActivate() {
        System.out.println("TestBean ejbActivate");
    }    

    public void ejbPassivate() {
            a1Home = null;
            a2Home = null;
    }

    public void ejbRemove() {

    }
    
    public void setSessionContext(SessionContext sc) {

    }

    private A1LocalHome lookupA1() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/A1Flush");
        return (A1LocalHome) objref;
    }    

    private A2LocalHome lookupA2() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/A2Flush");
        return (A2LocalHome) objref;
    }    

    /** 
     * Run test for CMP1.1 bean without flush after business
     * method. This method will fail at commit and the client
     * will get a RuntimeException.
     */
    public void testA1() throws CreateException {
        a1bean = a1Home.create("A1");
        a1bean.setName("A12345678901234567890"); 
    }

    /** 
     * Run test for CMP2.x bean without flush after business
     * method. This method will fail at commit and the client
     * will get a RuntimeException.
     */
    public void testA2() throws CreateException {
        a2bean = a2Home.create("A2");
        a2bean.setName("A12345678901234567890"); 
    }

    /** 
     * Run test for CMP1.1 bean with flush after business
     * method. This method will fail at flush and the client
     * will get our FlushException.
     */
    public void testA1WithFlush() throws CreateException, FlushException {
        a1bean = a1Home.create("B1");
        try {
            a1bean.setNameWithFlush("A12345678901234567890"); 
        } catch (EJBException e) {
            if (isExpectedException(e))
                throw new FlushException(e.toString());
            else
                throw e;
        }
    }

    /** 
     * Run test for CMP2.x bean with flush after business
     * method. This method will fail at flush and the client
     * will get our FlushException.
     */
    public void testA2WithFlush() throws CreateException, FlushException {
        a2bean = a2Home.create("B2");
        try {
            a2bean.setNameWithFlush("A12345678901234567890"); 
        } catch (EJBException e) {
            if (isExpectedException(e))
                throw new FlushException(e.toString());
            else
                throw e;
        }

    }

    private boolean isExpectedException (Exception e) {
        boolean expected = false;

        Throwable c = e.getCause();
        if (c != null && 
                (c instanceof com.sun.jdo.api.persistence.support.JDODataStoreException)) {

            String msg = c.getMessage();
            expected = (msg != null) && (msg.indexOf("JDO76400") > -1);
        }

        return expected;
    }
} 
