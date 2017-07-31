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
import javax.ejb.*;

public abstract class BarEJB 
    extends TimerStuffImpl 
    implements EntityBean, TimedObject 
{

    private EJBContext context_;

    //
    // CMP fields
    //

    // primary key

    public abstract Long getId();      
    public abstract void setId(Long timerId);

    public abstract String getValue2();
    public abstract void setValue2(String value2);

/*
    public abstract Timer getTimer();
    public abstract void setTimer(Timer t);
*/
    public BarEJB(){}

    public void ejbTimeout(Timer t) {

        checkCallerSecurityAccess("ejbTimeout", false);

        try {
            System.out.println("In BarEJB::ejbTimeout --> " + t.getInfo());
        } catch(Exception e) {
            System.out.println("got exception while calling getInfo");
            e.printStackTrace();
        }

        try {
            handleEjbTimeout(t);
        } catch(RuntimeException re) {
            throw re;
        } catch(Exception e) {
            System.out.println("handleEjbTimeout threw exception");
            e.printStackTrace();
        }

    }

    public BarPrimaryKey ejbCreate(Long id, String value2) 
        throws CreateException {
        String methodName = "ejbCreate";
        System.out.println("In BarEJB::ejbCreate");
//PG->        super.setupJmsConnection();
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, false);
        setId(id);
        setValue2(value2);
        return new BarPrimaryKey(id, value2);
    }

    public BarPrimaryKey ejbCreateWithTimer(Long id, String value2) 
        throws CreateException {
        String methodName = "ejbCreateWithTimer";
        System.out.println("In BarEJB::ejbCreateWithTimer");
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, false);
        setId(id);
        setValue2(value2);
        return new BarPrimaryKey(id, value2);
    }
    
    public void ejbPostCreate(Long id, String value2) throws CreateException {
        String methodName = "ejbPostCreate";
        System.out.println("In BarEJB::ejbPostCreate");
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, true);
    }

    public void ejbPostCreateWithTimer(Long id, String value2) throws CreateException {
        String methodName = "ejbPostCreateWithTimer";
        System.out.println("In BarEJB::ejbPostCreateWithTimer");
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, true);
        try {
            TimerService timerService = context_.getTimerService();
            Timer timer = timerService.createTimer
                (1, 1, methodName + id);
                                                         
        } catch(Exception e) {
            e.printStackTrace();
            throw new CreateException(e.getMessage());
        }
    }

    public void setEntityContext(EntityContext context) {
        context_ = context;
        setContext(context);
        String methodName = "setEntityContext";
        System.out.println("In BarEJB::setEntityContext");
        checkCallerSecurityAccess(methodName, false);
        checkGetSetRollbackOnly(methodName, false);
        getTimerService(methodName, false);
        doTimerStuff(methodName, false);
    }

    public void unsetEntityContext() {
        String methodName = "unsetEntityContext";
        System.out.println("In BarEJB::unsetEntityContext");
        checkCallerSecurityAccess(methodName, false);
        checkGetSetRollbackOnly(methodName, false);
        context_ = null;
    }

    public void ejbHomeNewTimerAndRemoveBean(Long id, String value2) 
        throws RemoteException {
        String methodName = "ejbHomeNewTimerAndRemoveBean";
        checkCallerSecurityAccess(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, false);
        try {
            BarHome home = (BarHome) context_.getEJBHome();
            Bar b = home.createWithTimer(id, value2);
            b.remove();
        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    public void ejbHomeNewTimerAndRemoveBeanAndRollback(Long id, String value2) 
        throws RemoteException {
        getTimerService("ejbHomeNewTimerAndRemoveBeanAndRollback", true);
        doTimerStuff("ejbHomeNewTimerAndRemoveBeanAndRollback", false);
        try {
            BarHome home = (BarHome) context_.getEJBHome();
            Bar b = home.createWithTimer(id, value2);
            b.remove();
            context_.setRollbackOnly();
        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    public void ejbHomeNixBeanAndRollback(Bar b) 
        throws RemoteException {
        getTimerService("ejbHomeNixBeanAndRollback", true);
        doTimerStuff("ejbHomeNixBeanAndRollback", false);
        try {
            BarHome home = (BarHome) context_.getEJBHome();
            home.remove(b.getPrimaryKey());
            context_.setRollbackOnly();
        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    public void ejbRemove() {
        String methodName = "ejbRemove";
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, true);
        cleanup();
    }
    
    public void ejbLoad() {
        String methodName = "ejbLoad";
        System.out.println("In BarEJB::ejbLoad");
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, true);
    }
    
    public void ejbStore() {
        String methodName = "ejbStore";
        System.out.println("In BarEJB::ejbStore");
        checkCallerSecurityAccess(methodName, true);
        checkGetSetRollbackOnly(methodName, true);
        getTimerService(methodName, true);
        doTimerStuff(methodName, true);
    }
    
    public void ejbPassivate() {
        String methodName = "ejbPassivate";
        checkCallerSecurityAccess(methodName, false);
        checkGetSetRollbackOnly(methodName, false);
        getTimerService(methodName, true);
        doTimerStuff(methodName, false);
    }
    
    public void ejbActivate() {
        String methodName = "ejbActivate";
        checkCallerSecurityAccess(methodName, false);
        checkGetSetRollbackOnly(methodName, false);
        getTimerService(methodName, true);
        doTimerStuff(methodName, false);
    }

}
