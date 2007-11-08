/**
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.ejb.stateless.converter.ejb;

import java.util.Collection;
import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.TimerService;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.EJBException;
import java.math.*;

/**
 * A simple stateless bean for the Converter application. This bean implements all
 * business method as declared by the remote interface, <code>Converter</code>.
 *
 * @see Converter
 * @see ConverterHome
 */
public class ConverterBean implements SessionBean, TimedObject {
    
    SessionContext sessionContext_;

    BigDecimal yenRate = new BigDecimal("121.6000");
    BigDecimal euroRate = new BigDecimal("0.0077");

    /**
     * Returns the yen value for a given dollar amount.
     * @param dollars dollar amount to be converted to yen.
     */
    public BigDecimal dollarToYen(BigDecimal dollars) {


        // add some tests to ensure that timer service is working.
        // Don't want to add an entirely new test yet since the total
        // number of tests is getting large.  This set of operations will
        // ensure that the timer persistent store is configured properly,
        // in addition to exercising some of the common timer functions.
        testTimerService();        

        BigDecimal result = dollars.multiply(yenRate);
        return result.setScale(2,BigDecimal.ROUND_UP);
    }

    /**
     * Returns the euro value for a given yen amount.
     * @param yen yen amount to be converted to euro.
     */
    public BigDecimal yenToEuro(BigDecimal yen) {
        BigDecimal result = yen.multiply(euroRate);
        return result.setScale(2,BigDecimal.ROUND_UP);
    }

    /**
     * Required by EJB spec.
     */
    public ConverterBean() {}

    /**
     * Creates a bean. Required by EJB spec.
     * @exception throws CreateException.
     */
    public void ejbCreate() {}

    /**
     * Removes the bean. Required by EJB spec.
     */
    public void ejbRemove() {}

    /**
     * Loads the state of the bean from secondary storage. Required by EJB spec.
     */
    public void ejbActivate() {}
    
    /**
     * Keeps the state of the bean to secondary storage. Required by EJB spec.
     */
    public void ejbPassivate() {}

    /**
     * Sets the session context. Required by EJB spec.
     * @param ctx A SessionContext object.
     */
   public void setSessionContext(SessionContext sc) {
       sessionContext_ = sc;
   }

    private void testTimerService() throws EJBException {

        String info = "testTimerService";

        TimerService timerService;
        Timer timer;
        try {
            sessionContext_.getUserTransaction().begin();
            timerService = sessionContext_.getTimerService();
            timer = timerService.createTimer(10000000, info);
            sessionContext_.getUserTransaction().commit();
            System.out.println("Successfully created ejb timer");
        } catch(Exception e) {
            try {
                sessionContext_.getUserTransaction().rollback();
            } catch(Exception re) {}
            e.printStackTrace();
            throw new EJBException(e);
        }

        try {
            sessionContext_.getUserTransaction().begin();
            Collection timers = timerService.getTimers();
            Timer timer2 = (Timer) timers.iterator().next();
            if( !timer.equals(timer2) ) {
                throw new EJBException("incorrect timer found " + timer2);
            }
            timer2.cancel();
            sessionContext_.getUserTransaction().commit();
            System.out.println("Successfully cancelled timer");
        } catch(Exception e) {
            try {
                sessionContext_.getUserTransaction().rollback();
            } catch(Exception re) {}
            e.printStackTrace();
            throw new EJBException(e);
        }
        
        try {
            sessionContext_.getUserTransaction().begin();
            Collection timers = timerService.getTimers();
            if( timers.size() > 0 ) {
                throw new EJBException("Incorrect number of timers = " +
                                       timers.size());
            }
            sessionContext_.getUserTransaction().commit();
            System.out.println("Successfully queried timers");
        } catch(Exception e) {
            try {
                sessionContext_.getUserTransaction().rollback();
            } catch(Exception re) {}
            e.printStackTrace();
            throw new EJBException(e);
        }

    }

    public void ejbTimeout(Timer t) {}

} // ConverterBean
