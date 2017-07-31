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

package com.sun.s1asdev.ejb.txprop.simple;

import java.util.Date;
import java.util.Collection;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.ejb.TimerService;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import javax.transaction.UserTransaction;
import com.sun.s1asdev.ejb.txprop.simple.HelloHome;
import com.sun.s1asdev.ejb.txprop.simple.Hello;

public class FooBean implements SessionBean {

    private SessionContext sc;

    public FooBean() {}

    public void ejbCreate() throws RemoteException {
	System.out.println("In FooBean::ejbCreate !!");

        // getting UserTransaction() is allowed
        System.out.println("Calling getUserTransaction()");
        UserTransaction ut = sc.getUserTransaction();

        try {
            // Calling ut methods is not allowed here.
            ut.getStatus();
        } catch(IllegalStateException ise) {
            System.out.println("Successfully caught illegal state ex when " +
                               "accessing UserTransaction methods in " +
                               "SLSB ejbCreate");
        } catch(Exception se) {
            throw new EJBException(se);
        }
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
    }

    public void callHello()  {
        System.out.println("in FooBean::callHello()");

        try {
            Context ic = new InitialContext();
                
            System.out.println("Looking up ejb ref ");
            // create EJB using factory from container 
            Object objref = ic.lookup("java:comp/env/ejb/hello");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");
                
            HelloHome  home = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);
                                                                     
            System.err.println("Narrowed home!!");
                
            Hello hr = home.create();
            System.err.println("Got the EJB!!");
                
            // invoke method on the EJB
            System.out.println("starting user tx");
            
            sc.getUserTransaction().begin();

            System.out.println("invoking ejb with user tx");

            hr.sayHello();

            System.out.println("successfully invoked ejb");

            System.out.println("committing tx");
            sc.getUserTransaction().commit();

        } catch(Exception e) {
            try {
                sc.getUserTransaction().rollback();
            } catch(Exception re) { re.printStackTrace(); }
            e.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(e);
            throw ise;
        }

        TimerService timerService = sc.getTimerService();
        
        try {
            timerService.createTimer(new Date(), 1000, null);
            throw new EJBException("CreateTimer call should have failed.");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got illegal state exception " +
                               "when attempting to create timer : " + 
                               ise.getMessage());
        }
        try {
            timerService.createTimer(new Date(), null);
            throw new EJBException("CreateTimer call should have failed.");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got illegal state exception " +
                               "when attempting to create timer : " + 
                               ise.getMessage());
        }
        try {
            timerService.createTimer(1000, 1000, null);
            throw new EJBException("CreateTimer call should have failed.");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got illegal state exception " +
                               "when attempting to create timer : " + 
                               ise.getMessage());
        }
        try {
            timerService.createTimer(1000, null);
            throw new EJBException("CreateTimer call should have failed.");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got illegal state exception " +
                               "when attempting to create timer : " + 
                               ise.getMessage());
        }

        Collection timers = timerService.getTimers();
        if( timers.size() > 0 ) {
            throw new EJBException("Wrong number of timers : " + 
                                   timers.size());
        } else {
            System.out.println("Successfully retrieved 0 timers");
        }
        
        Package p = this.getClass().getPackage();
        if( p == null ) {
            throw new EJBException("null package for " + 
                                   this.getClass().getName());
        } else {
            System.out.println("Package name = " + p);
        }
        
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
