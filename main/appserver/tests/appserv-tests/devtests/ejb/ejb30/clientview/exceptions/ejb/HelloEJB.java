/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.clientview.exceptions;

import javax.ejb.*;
import javax.naming.*;
import java.util.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import javax.annotation.Resource;
import javax.transaction.TransactionRolledbackException;
import static javax.transaction.Status.*;
import javax.transaction.TransactionRequiredException;
import javax.transaction.UserTransaction;
import javax.annotation.PostConstruct;

@Stateful
@Remote({Hello.class})
@TransactionManagement(TransactionManagementType.BEAN)
public class HelloEJB implements Hello  {

    private static final int ITERATIONS = 1;

    private SessionContext context;

    public @Resource void setSc(SessionContext sc) {
        System.out.println("In HelloEJB:setSc");
        context = sc;
    }

    @EJB(name="sfulBusiness") public SfulBusiness sfulBusiness;
    @EJB(name="slessBusiness") public SlessBusiness slessBusiness;

    @EJB(name="slessRemoteBusiness2") protected SlessRemoteBusiness2 slessRemoteBusiness2;    
    @EJB(name="sfulRemoteBusiness2") public SfulRemoteBusiness2 sfulRemoteBusiness2;

    @EJB public SlessRemoteBusiness slessRemoteBusiness;
    @EJB public SfulRemoteBusiness sfulRemoteBusiness;

    UserTransaction ut;

    @PostConstruct
    public void create() {

	try {
            ut = context.getUserTransaction();
            
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    private void setup() {

        try {

            sfulBusiness = (SfulBusiness) context.lookup("sfulBusiness");
            slessBusiness = (SlessBusiness)  context.lookup("slessBusiness");

            sfulRemoteBusiness = (SfulRemoteBusiness) context.lookup("com.sun.s1asdev.ejb.ejb30.clientview.exceptions.HelloEJB/sfulRemoteBusiness");
            slessRemoteBusiness = (SlessRemoteBusiness)  context.lookup("com.sun.s1asdev.ejb.ejb30.clientview.exceptions.HelloEJB/slessRemoteBusiness");
            
            sfulRemoteBusiness2 = (SfulRemoteBusiness2) context.lookup("sfulRemoteBusiness2");
            slessRemoteBusiness2 = (SlessRemoteBusiness2)  context.lookup("slessRemoteBusiness2");
            
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void runAccessDeniedExceptionTest() {
        setup();

        int numEx = 0;

        try {
            sfulBusiness.denied();
        } catch(EJBAccessException eae) {
            // this might be thrown by container but application
            // can't depend on receiving the sub-class exception
            System.out.println("Got specific ejb30 exception " + 
                               eae + " for sfulBusiness.denied()");
            // bean must still exist
            sfulBusiness.foo();
            numEx++;
        } catch(EJBException ele) {
            System.out.println("Got valid exception " + 
                               ele + " for sfulBusiness.denied()");
            // bean must still exist
            sfulBusiness.foo();
            numEx++;
        }

        try {
            sfulRemoteBusiness.denied();
        } catch(EJBException re) {
            System.out.println("Got expected exception " + 
                               re + " for sfulRemoteBusiness.denied()");

            // bean must still exist
            sfulRemoteBusiness.foo();
            numEx++;
        }

        try {
            sfulRemoteBusiness2.denied();
        } catch(RemoteException re) {
            System.out.println("Got expected exception " + 
                               re + " for sfulBusiness2.denied()");

            // bean must still exist
            sfulRemoteBusiness2.foo();
            numEx++;
        }

        try {
            slessBusiness.denied();
        } catch(EJBException ele) {
            System.out.println("Got expected exception " + 
                               ele + " for slessBusiness.denied()");

            numEx++;
        }

        try {
            slessRemoteBusiness.denied();
        } catch(EJBException re) {
            System.out.println("Got expected exception " + 
                               re + " for slessRemoteBusiness.denied()");
            numEx++;
        }

        try {
            slessRemoteBusiness2.denied();
        } catch(RemoteException re) {
            System.out.println("Got expected exception " + 
                               re + " for slessRemoteBusiness2.denied()");
            numEx++;
        }
        
        if( numEx != 6 ) {
            throw new RuntimeException("Didn't receive all expected " +
                                       "exceptions : " + numEx);
                                       
        } else {
            System.out.println("PASS : runAccessDeniedExceptionTest");
        }
    }

    public void runTxRolledbackTest() {

        setup();

        int numEx = 0;

        try {
            ut.begin();
            ut.setRollbackOnly();
            sfulBusiness.forceTransactionRolledbackException();
        } catch(EJBTransactionRolledbackException txre) {
            try { ut.rollback(); } catch(Exception t) {}
            numEx++;
        } catch(Exception e) {

            e.printStackTrace();
        }
        
        try {
            ut.begin();
            ut.setRollbackOnly();
            slessBusiness.forceTransactionRolledbackException();
        } catch(EJBTransactionRolledbackException txre) {
            try { ut.rollback(); } catch(Exception t) {}
            numEx++;
        } catch(Exception e) {}
        
        try {
            ut.begin();
            ut.setRollbackOnly();
            sfulRemoteBusiness.forceTransactionRolledbackException();
        } catch(EJBTransactionRolledbackException txre) {
            try { ut.rollback(); } catch(Exception t) {}
            numEx++;
        } catch(Exception e) {}
        
        try {
            ut.begin();
            ut.setRollbackOnly();
            slessRemoteBusiness.forceTransactionRolledbackException();
        } catch(EJBTransactionRolledbackException txre) {
            try { ut.rollback(); } catch(Exception t) {}
            numEx++;            
        } catch(Exception e) {}

        try {
            ut.begin();
            ut.setRollbackOnly();
            sfulRemoteBusiness2.forceTransactionRolledbackException();
        } catch(TransactionRolledbackException txre) {
            try { ut.rollback(); } catch(Exception t) {}
            numEx++;
        } catch(Exception e) {}
        
        try {
            ut.begin();
            ut.setRollbackOnly();
            slessRemoteBusiness2.forceTransactionRolledbackException();
        } catch(TransactionRolledbackException txre) {
            try { ut.rollback(); } catch(Exception t) {}
            numEx++;
        } catch(Exception e) {}
        
        if( numEx != 6 ) {
            throw new RuntimeException("Didn't receive all expected " +
                                       "exceptions : " + numEx);
                                       
        } else {
            System.out.println("PASS : runTxRolledbackTest");
        }

    }
    
    public void runTxRequiredTest() {

        setup();

        int numEx = 0;

        try {
            sfulBusiness.forceTransactionRequiredException();
        } catch(EJBTransactionRequiredException txre) {
            numEx++;
        } 
        
        try {
            slessBusiness.forceTransactionRequiredException();
        } catch(EJBTransactionRequiredException txre) {
            numEx++;
        }
        
        try {
            sfulRemoteBusiness.forceTransactionRequiredException();
        } catch(EJBTransactionRequiredException txre) {
            numEx++;
        } 
        
        try {
            slessRemoteBusiness.forceTransactionRequiredException();
        } catch(EJBTransactionRequiredException txre) {
            numEx++;            
        } 

        try {
            sfulRemoteBusiness2.forceTransactionRequiredException();
        } catch(TransactionRequiredException txre) {
            numEx++;
        } catch(RemoteException e) {}
        
        try {
            slessRemoteBusiness2.forceTransactionRequiredException();
        } catch(TransactionRequiredException txre) {
            numEx++;
        } catch(RemoteException e) {}
        
        if( numEx != 6 ) {
            throw new RuntimeException("Didn't receive all expected " +
                                       "exceptions : " + numEx);
                                       
        } else {
            System.out.println("PASS : runTxRequiredTest");
        }


    }

    public void runNoSuchObjectTest() {
        int numEx = 0;

        setup();

        sfulBusiness.remove();
        try {
            sfulBusiness.remove();
        } catch(NoSuchEJBException nsee) {
            numEx++;
        } 

        sfulRemoteBusiness.remove();        
        try {
            sfulRemoteBusiness.remove();
        } catch(NoSuchEJBException nsee) {
            numEx++;
        } 


        // sfulremotebusiness2
        try {
            sfulRemoteBusiness2.remove();
        } catch(RemoteException e) {
            throw new EJBException(e);
        }

        try {
            sfulRemoteBusiness2.remove();
        } catch(NoSuchObjectException nsoe) {
            numEx++;
        } catch(RemoteException e) {}
        
        if( numEx != 3 ) {
            throw new RuntimeException("Didn't receive all expected " +
                                       "exceptions : " + numEx);
                                       
        } else {
            System.out.println("PASS : runNoSuchObjectTest");
        }

    }

    public void runAppExceptionTest() {

        setup();

        int numEx = 0;

        try {
            sfulBusiness.throwRuntimeAppException();
        } catch(RuntimeAppException a) {
            // Make sure reference is still active by invoking on it again.
            // Sful instance shouldn't have been
            // discarded because this exception is an "application exception"
            try {
                sfulBusiness.throwRuntimeAppException();
            } catch(RuntimeAppException b) {
                numEx++;
            }
        } 
        
        try {
            slessBusiness.throwRuntimeAppException();
        } catch(RuntimeAppException txre) {
            numEx++;
        }
        
        try {
            sfulRemoteBusiness.throwRuntimeAppException();
        } catch(RuntimeAppException a) {
            // Make sure reference is still active by invoking on it again.
            // Sful instance shouldn't have been
            // discarded because this exception is an "application exception"
            try {
                sfulRemoteBusiness.throwRuntimeAppException();
            } catch(RuntimeAppException b) {
                numEx++;
            }
        } 
        
        try {
            slessRemoteBusiness.throwRuntimeAppException();
        } catch(RuntimeAppException txre) {
            numEx++;            
        } 

        try {
            sfulRemoteBusiness2.throwRuntimeAppException();
        } catch(RuntimeAppException a) {
            // Make sure reference is still active by invoking on it again.
            // Sful instance shouldn't have been
            // discarded because this exception is an "application exception"
            try {
                sfulRemoteBusiness2.throwRuntimeAppException();
            } catch(RuntimeAppException b) {
                numEx++;
            } catch(RemoteException re) {}
        } catch(RemoteException e) {}
        
        try {
            slessRemoteBusiness2.throwRuntimeAppException();
        } catch(RuntimeAppException txre) {
            numEx++;
        } catch(RemoteException e) {}
        
        if( numEx != 6 ) {
            throw new RuntimeException("Didn't receive all expected " +
                                       "exceptions : " + numEx);
                                       
        } else {
            System.out.println("PASS : runAppExceptionTest");
        }

    }

    public void runRollbackAppExceptionTest() {

        setup();

        int numEx = 0;

        try {
            ut.begin();
            sfulBusiness.throwRuntimeAppException();
        } catch(RuntimeAppException a) {
            if( checkStatus(ut, STATUS_ACTIVE) ) {
                numEx++;
            }
        } catch(Exception e) {
        } finally {
            try { ut.rollback(); } catch(Exception t) {}
        }

        try {
            ut.begin();
            sfulBusiness.throwRollbackAppException();
        } catch(RollbackAppException a) {
            if( checkStatus(ut, STATUS_MARKED_ROLLBACK) ) {
                numEx++;
            }
        } catch(Exception e) {
        } finally {
            try { ut.rollback(); } catch(Exception t) {}
        }

        try {
            ut.begin();
            slessBusiness.throwRuntimeAppException();
        } catch(RuntimeAppException txre) {
            if( checkStatus(ut, STATUS_ACTIVE) ) {
                numEx++;
            }
        } catch(Exception e) {
        } finally {
            try { ut.rollback(); } catch(Exception t) {}
        }

        try {
            ut.begin();
            slessBusiness.throwRollbackAppException();
        } catch(RollbackAppException a) {
            if( checkStatus(ut,STATUS_MARKED_ROLLBACK) ) {
                numEx++;
            }
        } catch(Exception e) {
        } finally {
            try { ut.rollback(); } catch(Exception t) {}
        }

        
        try {            
            ut.begin();
            sfulRemoteBusiness.throwRuntimeAppException();
        } catch(RuntimeAppException a) {
            if( checkStatus(ut, STATUS_ACTIVE) ) {
                numEx++;
            }
        } catch(Exception e) {
        } finally {
            try { ut.rollback(); } catch(Exception t) {}
        } 

        try {
            ut.begin();
            sfulRemoteBusiness.throwRollbackAppException();
        } catch(RollbackAppException a) {
            if( checkStatus(ut, STATUS_MARKED_ROLLBACK) ) {
                numEx++;
            }
        } catch(Exception e) {
        } finally {
            try { ut.rollback(); } catch(Exception t) {}
        }

        
        try {
            ut.begin();
            slessRemoteBusiness.throwRuntimeAppException();
        } catch(RuntimeAppException txre) {
            if( checkStatus(ut, STATUS_ACTIVE) ) {
                numEx++;
            }
        } catch(Exception e) {
        } finally {
            try { ut.rollback(); } catch(Exception t) {}
        } 

        try {
            ut.begin();
            slessRemoteBusiness.throwRollbackAppException();
        } catch(RollbackAppException a) {
            if( checkStatus(ut, STATUS_MARKED_ROLLBACK) ) {
                numEx++;
            }
        } catch(Exception e) {
        } finally {
            try { ut.rollback(); } catch(Exception t) {}
        }


        try {
            ut.begin();
            sfulRemoteBusiness2.throwRuntimeAppException();
        } catch(RuntimeAppException a) {
            if( checkStatus(ut, STATUS_ACTIVE) ) {
                numEx++;
            }
        } catch(RemoteException e) {
        } catch(Exception e) {
        } finally {
            try { ut.rollback(); } catch(Exception t) {}
        }

        try {
            ut.begin();
            sfulRemoteBusiness2.throwRollbackAppException();
        } catch(RollbackAppException a) {
            if( checkStatus(ut, STATUS_MARKED_ROLLBACK) ) {
                numEx++;
            }
        } catch(RemoteException e) {
        } catch(Exception e) {
        } finally {
            try { ut.rollback(); } catch(Exception t) {}
        }

        try {
            ut.begin();
            slessRemoteBusiness2.throwRuntimeAppException();
        } catch(RuntimeAppException a) {
            if( checkStatus(ut, STATUS_ACTIVE) ) {
                numEx++;
            }
        } catch(RemoteException e) {
        } catch(Exception e) {
        } finally {
            try { ut.rollback(); } catch(Exception t) {}
        }

        try {
            ut.begin();
            slessRemoteBusiness2.throwRollbackAppException();
        } catch(RollbackAppException a) {
            if( checkStatus(ut, STATUS_MARKED_ROLLBACK) ) {
                numEx++;
            }
        } catch(RemoteException e) {
        } catch(Exception e) {
        } finally {
            try { ut.rollback(); } catch(Exception t) {}
        }

        if( numEx != 12 ) {
            throw new RuntimeException("Didn't receive all expected " +
                                       "exceptions : " + numEx);
       
        } else {
            System.out.println("PASS : runRollbackAppExceptionTest");
        }
    }

    private boolean checkStatus(UserTransaction userTx, int statusToMatch) {

        try {
            return (userTx.getStatus() == statusToMatch);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

    }
}
