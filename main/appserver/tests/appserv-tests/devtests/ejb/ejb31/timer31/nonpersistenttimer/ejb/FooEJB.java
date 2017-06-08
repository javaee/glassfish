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

package com.sun.s1asdev.ejb31.timer.nonpersistenttimer;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.*;


public class FooEJB extends TimerStuffImpl implements SessionBean, TimedObject {
    private SessionContext sc;

    public FooEJB(){
    }

    public void ejbTimeout(Timer t) {
        checkCallerSecurityAccess("ejbTimeout", false);

        try {
            System.out.println("In FooEJB::ejbTimeout --> " + t.getInfo());
            if (t.isPersistent())
                throw new RuntimeException("FooEJB::ejbTimeout -> " 
                       + t.getInfo() + " is PERSISTENT!!!");
        } catch(RuntimeException e) {
            System.out.println("got exception while calling getInfo");
            throw e;
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

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
        setContext(sc);

        System.out.println("In FooEJB::setSessionContext");

        checkCallerSecurityAccess("setSessionContext", false);

        getTimerService("setSessionContext", false);
        doTimerStuff("setSessionContext", false);
    }

    public void ejbCreate() throws EJBException {
	System.out.println("In ejbnonpersistenttimer.Foo::ejbCreate !!");
        setupJmsConnection();
        checkGetSetRollbackOnly("ejbCreate", false);
        checkCallerSecurityAccess("ejbCreate", false);

        getTimerService("ejbCreate", true);
        doTimerStuff("ejbCreate", false);
    }

    public void ejbRemove() throws EJBException {
        System.out.println("In FooEJB::ejbRemove");
        checkCallerSecurityAccess("ejbRemove", false);
        checkGetSetRollbackOnly("ejbRemove", false);
        getTimerService("ejbRemove", true);
        doTimerStuff("ejbRemove", false);
        cleanup();
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    

}
