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

package com.sun.s1asdev.ejb.timer.timertests;

import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.*;


public class StatefulEJB extends TimerStuffImpl implements SessionBean, SessionSynchronization {
    private SessionContext sc;
    private TimerHandle timerHandle;
    public StatefulEJB(){
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
        setContext(sc);

        checkCallerSecurityAccess("setSessionContext", false);
        getTimerService("setSessionContext", false);
    }

    public void ejbCreate(TimerHandle th) throws RemoteException {
	System.out.println("In ejbtimer.Stateful::ejbCreate !!");

        timerHandle = th;

        checkGetSetRollbackOnly("ejbCreate", false);
        checkCallerSecurityAccess("ejbCreate", false);

        getTimerService("ejbCreate", false);

        try {
            Timer t = th.getTimer();
            throw new EJBException("shouldn't allow stateful ejbCreate to " +
                                   "access timer methods");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully caught exception when trying " +
                               "access timer methods in stateful ejbCreate");
        }
    }

    public void ejbRemove() throws RemoteException {
        checkCallerSecurityAccess("ejbRemove", false);
        checkGetSetRollbackOnly("ejbRemove", false);
        getTimerService("ejbRemove", false);
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void afterBegin() {  
        System.out.println("in afterBegin"); 
        try {
            Timer t = timerHandle.getTimer();
            t.getInfo();
            TimerHandle aHandle = t.getHandle();
            java.util.Date date = t.getNextTimeout();
            System.out.println("Successfully got timer in afterBegin");
        } catch(Exception e) {
            System.out.println("Error : got exception in afterBegin");
        }
    }

    public void beforeCompletion() {
        System.out.println("in beforeCompletion"); 
        try {
            Timer t = timerHandle.getTimer();
        } catch(NoSuchObjectLocalException nsole) {
            System.out.println("Successfull got NoSuchObjectLocalException " +
                               " in beforeCompletion");
        } catch(Exception e) {
            System.out.println("Error : got exception in beforeCompletion");
            e.printStackTrace();
        }
    }

    public void afterCompletion(boolean committed) {
        System.out.println("in afterCompletion. committed = " + committed); 
        try {
            Timer t = timerHandle.getTimer();
            System.out.println("Error : should have gotten exception in " +
                               "afterCompletion");
            Thread.currentThread().dumpStack();
        } catch(IllegalStateException ise) {
            System.out.println("got expected illegal state exception in " +
                               "afterCompletion");
        } catch(Exception e) {
            System.out.println("Error : got unexpected exception in " +
                               "beforeCompletion");
            e.printStackTrace();
        }

    }

}
