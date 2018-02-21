/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2018 Oracle and/or its affiliates. All rights reserved.
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
