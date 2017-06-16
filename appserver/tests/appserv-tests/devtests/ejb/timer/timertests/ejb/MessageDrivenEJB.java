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
import javax.jms.*;
import javax.ejb.*;
import java.io.Serializable;

public class MessageDrivenEJB extends TimerStuffImpl 
    implements MessageDrivenBean, TimedObject, MessageListener {
    private MessageDrivenContext mdc;

    public MessageDrivenEJB(){
    }

    public void onMessage(Message message) {
        try {
            ObjectMessage objMsg = (ObjectMessage) message;
            TimerHandle th = (TimerHandle) objMsg.getObject();
            Timer t = th.getTimer();
            String info = (String) t.getInfo();

            boolean redelivered = message.getJMSRedelivered();
            System.out.println("Received message " + info + " , redelivered = " + redelivered);

            if (info.equals("test1") ) {
                System.out.println("In onMessage : Got th for timer = " + 
                                   t.getInfo());
                doTimerStuff("onMessage", true);
                getInfo(th);
                getNextTimeoutTest2(5, th);
                getTimeRemainingTest2(5, th);

                cancelTimer(th);
                
                createTimerAndCancel(10000000);
                
                TimerHandle t1 = createTimer(1000000, "messagedrivenejb");
                cancelTimer(t1);
                TimerHandle t2 = createTimer(10000, "messagedrivenejb");
            } else if( info.equals("test2") ) {
                if( redelivered ) {
                    cancelTimer(th);
                } else {
                    if( isBMT() ) {
                        cancelTimer(th);
                    } else {
                        cancelTimerAndRollback(th);                
                    }
                }
            } else if( info.equals("test3") ) {
                if( redelivered ) {
                    getInfo(th);
                    cancelTimer(th);
                } else {
                    cancelTimer(th);
                    createTimerAndRollback(10000000);
                }
            } else if( info.equals("test4") ) {
                cancelTimer(th);
                TimerHandle runtimeExTimer =
                    createTimer(1, "RuntimeException");
            } else if( info.equals("test5") ) {
                cancelTimer(th);
                createTimer(1, 1, "cancelTimer");
            } else if( info.equals("test6") ) {
                cancelTimer(th);
                TimerHandle ctar = createTimer(1, 1, "cancelTimerAndRollback");
                cancelTimer(ctar);
            } 
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void ejbTimeout(Timer t) {
        checkCallerSecurityAccess("ejbTimeout", false);

        try {
            System.out.println("In MessageDrivenEJB::ejbTimeout --> " 
                               + t.getInfo());
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

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
	this.mdc = mdc;
        setContext(mdc);
	System.out.println("In ejbtimer.MessageDrivenEJB::setMessageDrivenContext !!");
        checkCallerSecurityAccess("setMessageDrivenContext", false);

        getTimerService("setMessageDrivenContext", false);
        doTimerStuff("setMessageDrivenContext", false);
    }

    public void ejbCreate() throws RemoteException {
	System.out.println("In ejbtimer.MessageDrivenEJB::ejbCreate !!");
        setupJmsConnection();
        checkGetSetRollbackOnly("ejbCreate", false);
        checkCallerSecurityAccess("ejbCreate", false);
        getTimerService("ejbCreate", true);
        doTimerStuff("ejbCreate", false);
    }

    public void ejbRemove() {
	System.out.println("In ejbtimer.MessageDrivenEJB::ejbRemove !!");
        checkCallerSecurityAccess("ejbRemove", false);
        checkGetSetRollbackOnly("ejbRemove", false);
        getTimerService("ejbRemove", true);
        doTimerStuff("ejbRemove", false);
        cleanup();
    }

}
